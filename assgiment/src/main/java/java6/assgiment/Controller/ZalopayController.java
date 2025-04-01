package java6.assgiment.Controller;

<<<<<<< HEAD
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
=======
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java6.assgiment.Service.ZalopayService;

import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/zalopay")
public class ZalopayController {

    private static final Logger logger = Logger.getLogger(ZalopayController.class.getName());

    @Autowired
    private ZalopayService zalopayService;

    @PostMapping
    public ResponseEntity<String> createPayment(@RequestBody Map<String, Object> orderRequest) {
        try {
            logger.info("Received order request: " + orderRequest);
            String response = zalopayService.createOrder(orderRequest);

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> responseMap = mapper.readValue(response, Map.class);

            if (responseMap.containsKey("error")) {
                logger.warning("Error from ZaloPayService: " + response);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            logger.info("Order created successfully: " + response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("Error creating payment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Error creating payment: " + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/order-status/{appTransId}")
    public ResponseEntity<String> getOrderStatus(@PathVariable String appTransId) {
        try {
            logger.info("Checking order status for appTransId: " + appTransId);
            String response = zalopayService.getOrderStatus(appTransId);

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> responseMap = mapper.readValue(response, Map.class);

            if (responseMap.containsKey("error")) {
                logger.warning("Error from ZaloPayService: " + response);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            logger.info("Order status retrieved: " + response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("Error getting order status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Error getting order status: " + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/callback")
    public ResponseEntity<String> handleCallback(@RequestBody Map<String, Object> callbackData) {
        try {
            logger.info("Received callback from ZaloPay: " + callbackData);
            // Thêm logic xử lý callback ở đây (ví dụ: kiểm tra MAC, cập nhật trạng thái đơn hàng)
            return ResponseEntity.ok("{\"return_code\": 1, \"return_message\": \"Callback received\"}");
        } catch (Exception e) {
            logger.severe("Error processing callback: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Error processing callback: " + e.getMessage() + "\"}");
        }
>>>>>>> 5b7f43e002990d06e1b784d451983dd72f0b2a31
    }
}