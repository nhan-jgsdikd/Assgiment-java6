package java6.assgiment.Controller;

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
    }
}