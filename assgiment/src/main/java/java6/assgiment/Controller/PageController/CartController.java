package java6.assgiment.Controller.PageController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import java6.assgiment.DAO.VoucherDAO;
import java6.assgiment.Entity.OrderDetail;
import java6.assgiment.Entity.Orders;
import java6.assgiment.Entity.Product;
import java6.assgiment.Entity.User;
import java6.assgiment.Entity.Voucher;
import java6.assgiment.Entity.Orders.OrderStatus;

@Controller
public class CartController {

    private static final double SHIPPING_FEE = 300.000; // Phí vận chuyển cố định 30đ

    @Autowired
    HttpSession session;

    @Autowired
    private ProductDAO productDAO;

    @Autowired
    private OrderDAO ordersDAO;

    @Autowired
    private OrderDetailDAO orderDetailDAO;

    @Autowired
    private VoucherDAO voucherDAO;

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
        double totalDiscount = cartOrder != null && cartOrder.getVoucher() != null ? calculateVoucherDiscount(totalPrice, cartOrder.getVoucher()) : 0;
        double totalPayment = totalPrice - totalDiscount + SHIPPING_FEE; // Cộng phí vận chuyển

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
        double voucherDiscount = cartOrder.getVoucher() != null ? calculateVoucherDiscount(totalPrice, cartOrder.getVoucher()) : 0;
        double totalPayment = totalPrice - voucherDiscount + SHIPPING_FEE; // Cộng phí vận chuyển

        List<Voucher> availableVouchers = voucherDAO.findAll().stream()
            .filter(v -> !v.getIsDeleted() &&
                         LocalDateTime.now().isAfter(v.getStartDate()) &&
                         LocalDateTime.now().isBefore(v.getEndDate()) &&
                         v.getQuantity() > 0)
            .collect(Collectors.toList());

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("voucherDiscount", voucherDiscount);
        model.addAttribute("totalPayment", totalPayment);
        model.addAttribute("user", loggedInUser);
        model.addAttribute("availableVouchers", availableVouchers);

        return "Dashboard/checkout";
    }

    @PostMapping("/apply-voucher")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> applyVoucher(@RequestBody Map<String, Object> request) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Vui lòng đăng nhập!"));
        }

        String voucherCode = (String) request.get("voucherCode");
        double totalPrice = Double.parseDouble(request.get("totalPrice").toString());
        Map<String, Object> response = new HashMap<>();

        Optional<Orders> cartOrderOpt = ordersDAO.findByUserAndStatusAndIsDeleted(loggedInUser, OrderStatus.PREPARING, false);
        if (!cartOrderOpt.isPresent()) {
            response.put("success", false);
            response.put("message", "Giỏ hàng không tồn tại!");
            return ResponseEntity.ok(response);
        }

        Orders cartOrder = cartOrderOpt.get();

        if (voucherCode == null || voucherCode.isEmpty()) {
            cartOrder.setVoucher(null); // Xóa voucher nếu không sử dụng
            ordersDAO.save(cartOrder);
            response.put("success", true);
            response.put("discount", 0.0);
            response.put("totalPayment", totalPrice + SHIPPING_FEE); // Cộng phí vận chuyển
            response.put("message", "Không sử dụng voucher.");
            return ResponseEntity.ok(response);
        }

        Optional<Voucher> voucherOpt = voucherDAO.findByCode(voucherCode);
        if (!voucherOpt.isPresent() || voucherOpt.get().getIsDeleted() ||
            LocalDateTime.now().isBefore(voucherOpt.get().getStartDate()) ||
            LocalDateTime.now().isAfter(voucherOpt.get().getEndDate()) ||
            voucherOpt.get().getQuantity() <= 0) {
            response.put("success", false);
            response.put("message", "Mã voucher không hợp lệ hoặc đã hết hạn!");
            return ResponseEntity.ok(response);
        }

        Voucher voucher = voucherOpt.get();
        double discount = calculateVoucherDiscount(totalPrice, voucher);
        if (discount == 0 && totalPrice < voucher.getMinOrderValue()) {
            response.put("success", false);
            response.put("message", "Đơn hàng không đủ giá trị tối thiểu để áp dụng voucher!");
            return ResponseEntity.ok(response);
        }

        cartOrder.setVoucher(voucher); // Áp dụng voucher vào đơn hàng
        ordersDAO.save(cartOrder);

        double totalPayment = totalPrice - discount + SHIPPING_FEE; // Cộng phí vận chuyển

        response.put("success", true);
        response.put("discount", discount);
        response.put("totalPayment", totalPayment);
        response.put("message", "Áp dụng voucher thành công!");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm-checkout")
    public String confirmCheckout(
            @RequestParam("shippingAddressLine") String shippingAddressLine,
            @RequestParam("shippingWard") String shippingWard,
            @RequestParam("shippingDistrict") String shippingDistrict,
            @RequestParam("shippingCity") String shippingCity,
            @RequestParam("paymentMethod") String paymentMethod,
            @RequestParam(value = "voucherCode", required = false) String voucherCode,
            Model model,
            RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        Optional<Orders> cartOrderOpt = ordersDAO.findByUserAndStatusAndIsDeleted(loggedInUser, OrderStatus.PREPARING, false);
        if (!cartOrderOpt.isPresent() || orderDetailDAO.findByOrderAndIsDeleted(cartOrderOpt.get(), false).isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Giỏ hàng trống!");
            return "redirect:/cart";
        }

        Orders cartOrder = cartOrderOpt.get();
        // Ensure orderName is set
        if (cartOrder.getOrderName() == null || cartOrder.getOrderName().isEmpty()) {
            cartOrder.setOrderName(String.format("Order-%s-%s", 
                loggedInUser.getUsername(),
                LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))));
        }

        List<OrderDetail> cartItems = orderDetailDAO.findByOrderAndIsDeleted(cartOrder, false);
        double totalPrice = cartItems.stream()
            .mapToDouble(item -> item.getPrice().doubleValue() * item.getQuantity())
            .sum();
        double voucherDiscount = 0;

        if (voucherCode != null && !voucherCode.isEmpty()) {
            Optional<Voucher> voucherOpt = voucherDAO.findByCode(voucherCode);
            if (voucherOpt.isPresent() && !voucherOpt.get().getIsDeleted() &&
                LocalDateTime.now().isAfter(voucherOpt.get().getStartDate()) &&
                LocalDateTime.now().isBefore(voucherOpt.get().getEndDate()) &&
                voucherOpt.get().getQuantity() > 0) {
                Voucher voucher = voucherOpt.get();
                voucherDiscount = calculateVoucherDiscount(totalPrice, voucher);
                if (voucherDiscount == 0 && totalPrice < voucher.getMinOrderValue()) {
                    redirectAttributes.addFlashAttribute("error", "Đơn hàng không đủ giá trị tối thiểu để áp dụng voucher!");
                    return "redirect:/checkout";
                }
                cartOrder.setVoucher(voucher);
                voucher.setQuantity(voucher.getQuantity() - 1);
                voucherDAO.save(voucher);
            } else {
                redirectAttributes.addFlashAttribute("error", "Mã voucher không hợp lệ hoặc đã hết hạn!");
                return "redirect:/checkout";
            }
        }

        double totalPayment = totalPrice - voucherDiscount + SHIPPING_FEE; // Cộng phí vận chuyển

        cartOrder.setOrderDate(LocalDateTime.now());
        cartOrder.setTotalAmount(totalPayment);
        cartOrder.setShippingAddressLine(shippingAddressLine);
        cartOrder.setShippingWard(shippingWard);
        cartOrder.setShippingDistrict(shippingDistrict);
        cartOrder.setShippingCity(shippingCity);
        cartOrder.setPaymentMethod(paymentMethod);
        cartOrder.setStatus(OrderStatus.SHIPPING); // Chuyển sang trạng thái SHIPPING
        ordersDAO.save(cartOrder);

        // Tạo đơn hàng mới để thay thế giỏ hàng hiện tại
        Orders newCartOrder = Orders.builder()
                .user(loggedInUser)
                .orderName(String.format("Order-%s-%s", 
                    loggedInUser.getUsername(),
                    LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))))
                .orderDate(LocalDateTime.now())
                .totalAmount(0.0)
                .paymentMethod("COD")
                .status(OrderStatus.PREPARING)
                .isDeleted(false)
                .build();
        ordersDAO.save(newCartOrder);

        model.addAttribute("message", "Đơn hàng của bạn đã được gửi, đang chờ giao hàng!");
        return "Dashboard/checkout-success";
    }

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
            String orderName = String.format("Order-%s-%s", 
                loggedInUser.getUsername(),
                LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

            cartOrder = Orders.builder()
                    .user(loggedInUser)
                    .orderName(orderName)
                    .orderDate(LocalDateTime.now())
                    .totalAmount(0.0)
                    .paymentMethod("COD")
                    .status(OrderStatus.PREPARING)
                    .isDeleted(false)
                    .build();
            ordersDAO.save(cartOrder);
        } else {
            cartOrder = cartOrderOpt.get();
            if (cartOrder.getOrderName() == null || cartOrder.getOrderName().isEmpty()) {
                String orderName = String.format("Order-%s-%s", 
                    loggedInUser.getUsername(),
                    LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
                cartOrder.setOrderName(orderName);
                ordersDAO.save(cartOrder);
            }
        }

        List<OrderDetail> cartItems = orderDetailDAO.findByOrderAndIsDeleted(cartOrder, false);
        boolean productExists = false;
        for (OrderDetail item : cartItems) {
            if (item.getProduct().getId().equals(productId)) {
                item.setQuantity(item.getQuantity() + 1);
                item.setProductName(product.getNameProduct());
                orderDetailDAO.save(item);
                productExists = true;
                break;
            }
        }

        if (!productExists) {
            OrderDetail newItem = OrderDetail.builder()
                    .order(cartOrder)
                    .product(product)
                    .productName(product.getNameProduct())
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
        order.setTotalAmount(total); // Lưu ý: Không cộng phí vận chuyển ở đây, sẽ cộng khi hiển thị
        ordersDAO.save(order);
    }

    // Hàm tiện ích tính giảm giá voucher
    private double calculateVoucherDiscount(double totalPrice, Voucher voucher) {
        if (voucher == null || totalPrice < voucher.getMinOrderValue()) {
            return 0;
        }

        double discount;
        if (voucher.getIsPercentage()) {
            discount = totalPrice * (voucher.getDiscountValue() / 100.0);
        } else {
            discount = voucher.getDiscountValue();
        }

        Double maxDiscount = voucher.getMaxDiscount();
        if (maxDiscount != null && discount > maxDiscount) {
            discount = maxDiscount;
        }

        return discount;
    }
}