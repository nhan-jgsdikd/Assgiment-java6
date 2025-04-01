package java6.assgiment.Controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import java6.assgiment.DAO.OrderDAO;
import java6.assgiment.DAO.OrderDetailDAO;
import java6.assgiment.Entity.Orders;
import java6.assgiment.Entity.User;
import java6.assgiment.Entity.Orders.OrderStatus;

@RestController
public class ZalopayController {

    @Autowired
    HttpSession session;

    @Autowired
    private OrderDAO ordersDAO;


    @PostMapping("/zalopay-payment")
    public ResponseEntity<Map<String, Object>> zaloPayPayment(@RequestBody Map<String, Object> payload) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        Map<String, Object> response = new HashMap<>();

        if (loggedInUser == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập!");
            return ResponseEntity.status(401).body(response);
        }

        Optional<Orders> cartOrderOpt = ordersDAO.findByUserAndStatusAndIsDeleted(loggedInUser, OrderStatus.PREPARING, false);
        if (!cartOrderOpt.isPresent()) {
            response.put("success", false);
            response.put("message", "Giỏ hàng không tồn tại!");
            return ResponseEntity.badRequest().body(response);
        }

        Orders cartOrder = cartOrderOpt.get();
        // Update shipping details
        cartOrder.setShippingAddressLine((String) payload.get("shippingAddressLine"));
        cartOrder.setShippingWard((String) payload.get("shippingWard"));
        cartOrder.setShippingDistrict((String) payload.get("shippingDistrict"));
        cartOrder.setShippingCity((String) payload.get("shippingCity"));
        cartOrder.setPaymentMethod("zalopay");
        cartOrder.setOrderDate(LocalDateTime.now());
        ordersDAO.save(cartOrder);

        // Safely convert totalPayment to Double
        Object totalPaymentObj = payload.get("totalPayment");
        Double totalPayment;
        if (totalPaymentObj instanceof Integer) {
            totalPayment = ((Integer) totalPaymentObj).doubleValue();
        } else if (totalPaymentObj instanceof Double) {
            totalPayment = (Double) totalPaymentObj;
        } else {
            response.put("success", false);
            response.put("message", "Dữ liệu totalPayment không hợp lệ!");
            return ResponseEntity.badRequest().body(response);
        }

        // Placeholder ZaloPay integration
        try {
            String paymentUrl = integrateZaloPay(cartOrder, totalPayment);
            response.put("success", true);
            response.put("paymentUrl", paymentUrl);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi xử lý thanh toán ZaloPay: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    private String integrateZaloPay(Orders order, Double totalPayment) {
        // Replace with actual ZaloPay API integration
        return "https://zalopay.vn/example-payment-url?orderId=" + order.getId() + "&amount=" + totalPayment;
    }
}