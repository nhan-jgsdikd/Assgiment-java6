package java6.assgiment.Controller.PageController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java6.assgiment.DAO.ProductDAO;
import java6.assgiment.Entity.Product;
import java6.assgiment.Stass.CartItem;

@Controller
public class CartController {

    @Autowired
    HttpSession session;

    @Autowired
    ProductDAO productDAO;

    @GetMapping("/cart")
    public String cart(Model model, HttpServletRequest request) {
        List<CartItem> cartItems = getCartItems(request);
        
        // Tính tổng số sản phẩm
        int totalItems = cartItems.stream().mapToInt(CartItem::getQuantity).sum();
        
        // Nhóm sản phẩm theo seller (giả sử seller là tên sản phẩm hoặc một trường khác)
        Map<String, List<CartItem>> sellerGroups = cartItems.stream()
            .collect(Collectors.groupingBy(item -> "Seller_" + item.getProduct().getId())); // Thay bằng trường seller nếu có
        
        // Tính tổng tiền
        double totalPrice = cartItems.stream()
            .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
            .sum();
        double totalDiscount = 0; // Giả sử chưa có logic giảm giá
        double totalPayment = totalPrice - totalDiscount;

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("sellerGroups", sellerGroups);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("totalDiscount", totalDiscount);
        model.addAttribute("totalPayment", totalPayment);
        model.addAttribute("promotions", new ArrayList<>()); // Thêm logic khuyến mãi nếu cần
        
        return "Dashboard/Cart"; // Đảm bảo tên template khớp với file HTML
    }

    @GetMapping("/clear-cart")
    public String clearCart() {
        session.setAttribute("cart", new ArrayList<>());
        return "redirect:/cart";
    }

    @PostMapping("/update-cart")
    @ResponseBody
    public ResponseEntity<String> updateCart(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        Long productId = Long.parseLong(payload.get("productId").toString());
        int newQuantity = Integer.parseInt(payload.get("quantity").toString());
        
        List<CartItem> cart = getCartItems(request);
        for (CartItem item : cart) {
            if (item.getProduct().getId().equals(productId)) {
                if (newQuantity > 0) {
                    item.setQuantity(newQuantity);
                } else {
                    cart.remove(item);
                }
                break;
            }
        }
        session.setAttribute("cart", cart);
        return ResponseEntity.ok("Cart updated");
    }

    @GetMapping("/remove-from-cart")
    public String removeFromCart(@RequestParam("id") Long productId, HttpServletRequest request) {
        List<CartItem> cart = getCartItems(request);
        cart.removeIf(item -> item.getProduct().getId().equals(productId));
        session.setAttribute("cart", cart);
        return "redirect:/cart";
    }

    @PostMapping("/checkout")
    public String checkout(Model model) {
        List<CartItem> cart = getCartItems(null);
        if (cart.isEmpty()) {
            return "redirect:/cart";
        }
        
        session.removeAttribute("cart");
        model.addAttribute("message", "Thanh toán thành công!");
        return "Dashboard/checkout";
    }

    private List<CartItem> getCartItems(HttpServletRequest request) {
        HttpSession session = request != null ? request.getSession() : this.session;
        @SuppressWarnings("unchecked")
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute("cart", cart);
        }
        return cart;
    }
}