package java6.assgiment.Controller.PageController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java6.assgiment.DAO.OrderDAO;
import java6.assgiment.DAO.OrderDetailDAO;
import java6.assgiment.DAO.ProductDAO;
import java6.assgiment.Entity.OrderDetail;
import java6.assgiment.Entity.Orders;
import java6.assgiment.Entity.Product;
import java6.assgiment.Entity.User;
import java6.assgiment.Entity.Orders.OrderStatus;

@Controller
public class CartController {

    @Autowired
    HttpSession session;

    @Autowired
    private ProductDAO productDAO;

    @Autowired
    private OrderDAO ordersDAO;

    @Autowired
    private OrderDetailDAO orderDetailDAO;

    // Hiển thị giỏ hàng từ database
    @GetMapping("/cart")
    public String cart(Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        Optional<Orders> cartOrderOpt = ordersDAO.findByUserAndStatusAndIsDeleted(loggedInUser, OrderStatus.PREPARING, false);
        Orders cartOrder = cartOrderOpt.orElse(null);

        List<OrderDetail> cartItems = cartOrder != null ? orderDetailDAO.findByOrderAndIsDeleted(cartOrder, false) : new ArrayList<>();

        int totalItems = cartItems.stream().mapToInt(OrderDetail::getQuantity).sum();

        Map<String, List<OrderDetail>> sellerGroups = cartItems.stream()
            .collect(Collectors.groupingBy(item -> "Seller_" + item.getProduct().getClassify()));

        double totalPrice = cartItems.stream()
            .mapToDouble(item -> item.getPrice().doubleValue() * item.getQuantity())
            .sum();
        double totalDiscount = 0;
        double totalPayment = totalPrice - totalDiscount;

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("sellerGroups", sellerGroups);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("totalDiscount", totalDiscount);
        model.addAttribute("totalPayment", totalPayment);
        model.addAttribute("promotions", new ArrayList<>());

        return "Dashboard/Cart";
    }

    // Xóa giỏ hàng (xóa mềm)
    @GetMapping("/clear-cart")
    public String clearCart(RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        Optional<Orders> cartOrderOpt = ordersDAO.findByUserAndStatusAndIsDeleted(loggedInUser, OrderStatus.PREPARING, false);
        if (cartOrderOpt.isPresent()) {
            Orders cartOrder = cartOrderOpt.get();
            cartOrder.setIsDeleted(true);
            ordersDAO.save(cartOrder);
            redirectAttributes.addFlashAttribute("message", "Đã xóa giỏ hàng!");
        }
        return "redirect:/cart";
    }

    // Cập nhật số lượng sản phẩm trong giỏ hàng
    @PostMapping("/update-cart")
    @ResponseBody
    public ResponseEntity<String> updateCart(@RequestBody Map<String, Object> payload) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return ResponseEntity.status(401).body("Vui lòng đăng nhập!");
        }

        Long productId = Long.parseLong(payload.get("productId").toString());
        int newQuantity = Integer.parseInt(payload.get("quantity").toString());

        Optional<Orders> cartOrderOpt = ordersDAO.findByUserAndStatusAndIsDeleted(loggedInUser, OrderStatus.PREPARING, false);
        if (!cartOrderOpt.isPresent()) {
            return ResponseEntity.badRequest().body("Giỏ hàng không tồn tại!");
        }

        Orders cartOrder = cartOrderOpt.get();
        List<OrderDetail> cartItems = orderDetailDAO.findByOrderAndIsDeleted(cartOrder, false);

        for (OrderDetail item : cartItems) {
            if (item.getProduct().getId().equals(productId)) {
                if (newQuantity > 0) {
                    item.setQuantity(newQuantity);
                    orderDetailDAO.save(item);
                } else {
                    item.setIsDeleted(true);
                    orderDetailDAO.save(item);
                }
                updateOrderTotal(cartOrder);
                return ResponseEntity.ok("Cart updated");
            }
        }
        return ResponseEntity.badRequest().body("Sản phẩm không có trong giỏ hàng!");
    }

    // Xóa sản phẩm khỏi giỏ hàng
    @GetMapping("/remove-from-cart")
    public String removeFromCart(@RequestParam("id") Long productId, RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        Optional<Orders> cartOrderOpt = ordersDAO.findByUserAndStatusAndIsDeleted(loggedInUser, OrderStatus.PREPARING, false);
        if (cartOrderOpt.isPresent()) {
            Orders cartOrder = cartOrderOpt.get();
            List<OrderDetail> cartItems = orderDetailDAO.findByOrderAndIsDeleted(cartOrder, false);
            for (OrderDetail item : cartItems) {
                if (item.getProduct().getId().equals(productId)) {
                    item.setIsDeleted(true);
                    orderDetailDAO.save(item);
                    updateOrderTotal(cartOrder);
                    redirectAttributes.addFlashAttribute("message", "Đã xóa sản phẩm khỏi giỏ hàng!");
                    break;
                }
            }
        }
        return "redirect:/cart";
    }

    // Hiển thị trang checkout
    @GetMapping("/checkout")
    public String showCheckout(Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        Optional<Orders> cartOrderOpt = ordersDAO.findByUserAndStatusAndIsDeleted(loggedInUser, OrderStatus.PREPARING, false);
        if (!cartOrderOpt.isPresent() || orderDetailDAO.findByOrderAndIsDeleted(cartOrderOpt.get(), false).isEmpty()) {
            return "redirect:/cart";
        }

        Orders cartOrder = cartOrderOpt.get();
        List<OrderDetail> cartItems = orderDetailDAO.findByOrderAndIsDeleted(cartOrder, false);

        double totalPrice = cartItems.stream()
            .mapToDouble(item -> item.getPrice().doubleValue() * item.getQuantity())
            .sum();
        double totalDiscount = 0;
        double totalPayment = totalPrice - totalDiscount;

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("totalDiscount", totalDiscount);
        model.addAttribute("totalPayment", totalPayment);
        model.addAttribute("user", loggedInUser);
        return "Dashboard/checkout";
    }

<<<<<<< HEAD
=======
    // Xác nhận thanh toán từ phía người dùng (xóa đơn hàng khỏi giỏ hàng, giữ PREPARING chờ admin duyệt)
>>>>>>> 5b7f43e002990d06e1b784d451983dd72f0b2a31
    @PostMapping("/confirm-checkout")
    public String confirmCheckout(
            @RequestParam("shippingAddressLine") String shippingAddressLine,
            @RequestParam("shippingWard") String shippingWard,
            @RequestParam("shippingDistrict") String shippingDistrict,
            @RequestParam("shippingCity") String shippingCity,
<<<<<<< HEAD
            @RequestParam("paymentMethod") String paymentMethod,
=======
>>>>>>> 5b7f43e002990d06e1b784d451983dd72f0b2a31
            Model model,
            RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }
<<<<<<< HEAD
    
=======

>>>>>>> 5b7f43e002990d06e1b784d451983dd72f0b2a31
        Optional<Orders> cartOrderOpt = ordersDAO.findByUserAndStatusAndIsDeleted(loggedInUser, OrderStatus.PREPARING, false);
        if (!cartOrderOpt.isPresent() || orderDetailDAO.findByOrderAndIsDeleted(cartOrderOpt.get(), false).isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Giỏ hàng trống!");
            return "redirect:/cart";
        }
<<<<<<< HEAD
    
        Orders cartOrder = cartOrderOpt.get();
=======

        Orders cartOrder = cartOrderOpt.get();
        // Cập nhật thông tin giao hàng
>>>>>>> 5b7f43e002990d06e1b784d451983dd72f0b2a31
        cartOrder.setOrderDate(LocalDateTime.now());
        cartOrder.setShippingAddressLine(shippingAddressLine);
        cartOrder.setShippingWard(shippingWard);
        cartOrder.setShippingDistrict(shippingDistrict);
        cartOrder.setShippingCity(shippingCity);
<<<<<<< HEAD
        cartOrder.setPaymentMethod(paymentMethod);
        ordersDAO.save(cartOrder);
    
        cartOrder.setIsDeleted(true);
        ordersDAO.save(cartOrder);
    
=======
        // Không thay đổi trạng thái, vẫn giữ PREPARING
        ordersDAO.save(cartOrder);

        // Xóa đơn hàng khỏi giỏ hàng (đánh dấu isDeleted = true)
        cartOrder.setIsDeleted(true);
        ordersDAO.save(cartOrder);

>>>>>>> 5b7f43e002990d06e1b784d451983dd72f0b2a31
        model.addAttribute("message", "Đơn hàng của bạn đã được gửi, đang chờ admin duyệt!");
        return "Dashboard/checkout-success";
    }

    // Thêm sản phẩm vào giỏ hàng
    @PostMapping("/add-to-cart")
    public String addToCart(
            @RequestParam("productId") Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "default") String sort,
            @RequestParam(defaultValue = "") String classify,
            @RequestParam(defaultValue = "") String search,
            RedirectAttributes redirectAttributes) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng");
            return "redirect:/login";
        }

        Optional<Product> productOpt = productDAO.findById(productId);
        if (!productOpt.isPresent() || productOpt.get().getIsDeleted()) {
            redirectAttributes.addFlashAttribute("error", "Sản phẩm không tồn tại hoặc đã bị xóa!");
            return "redirect:/product";
        }

        Product product = productOpt.get();
        if (product.getQuanlity() <= 0) {
            redirectAttributes.addFlashAttribute("error", "Sản phẩm đã hết hàng!");
            return "redirect:/product";
        }

        Optional<Orders> cartOrderOpt = ordersDAO.findByUserAndStatusAndIsDeleted(loggedInUser, OrderStatus.PREPARING, false);
        Orders cartOrder;
        if (!cartOrderOpt.isPresent()) {
            cartOrder = Orders.builder()
                .user(loggedInUser)
                .orderDate(LocalDateTime.now())
                .totalAmount(0.0)
                .paymentMethod("COD")
                .status(OrderStatus.PREPARING)
                .isDeleted(false)
                .build();
            ordersDAO.save(cartOrder);
        } else {
            cartOrder = cartOrderOpt.get();
        }

        List<OrderDetail> cartItems = orderDetailDAO.findByOrderAndIsDeleted(cartOrder, false);
        boolean productExists = false;
        for (OrderDetail item : cartItems) {
            if (item.getProduct().getId().equals(productId)) {
                item.setQuantity(item.getQuantity() + 1);
                orderDetailDAO.save(item);
                productExists = true;
                break;
            }
        }

        if (!productExists) {
            OrderDetail newItem = OrderDetail.builder()
                .order(cartOrder)
                .product(product)
                .quantity(1)
                .price(new java.math.BigDecimal(product.getPrice()))
                .isDeleted(false)
                .build();
            orderDetailDAO.save(newItem);
        }

        updateOrderTotal(cartOrder);

        product.setQuanlity(product.getQuanlity() - 1);
        productDAO.save(product);

        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addAttribute("sort", sort);
        redirectAttributes.addAttribute("classify", classify);
        redirectAttributes.addAttribute("search", search);
        redirectAttributes.addFlashAttribute("message", "Đã thêm sản phẩm vào giỏ hàng!");
        return "redirect:/product";
    }

    // Cập nhật tổng tiền đơn hàng
    private void updateOrderTotal(Orders order) {
        List<OrderDetail> items = orderDetailDAO.findByOrderAndIsDeleted(order, false);
        double total = items.stream()
            .mapToDouble(item -> item.getPrice().doubleValue() * item.getQuantity())
            .sum();
        order.setTotalAmount(total);
        ordersDAO.save(order);
    }
}