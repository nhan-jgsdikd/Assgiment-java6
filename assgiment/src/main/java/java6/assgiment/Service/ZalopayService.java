package java6.assgiment.Service;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.stereotype.Service;

import java6.assgiment.Config.ZalopayConfig;
import java6.assgiment.Crypto.HMACUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

@Service
public class ZalopayService {

    private static final Logger logger = Logger.getLogger(ZalopayService.class.getName());

    private static String getCurrentTimeString(String format) {
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT+7"));
        SimpleDateFormat fmt = new SimpleDateFormat(format);
        fmt.setCalendar(cal);
        return fmt.format(cal.getTimeInMillis());
    }

    private boolean validateConfig() {
        return ZalopayConfig.config.get("appid") != null &&
               ZalopayConfig.config.get("key1") != null &&
               ZalopayConfig.config.get("key2") != null &&
               ZalopayConfig.config.get("endpoint") != null &&
               ZalopayConfig.config.get("orderstatus") != null;
    }

    public String createOrder(Map<String, Object> orderRequest) {
        if (!validateConfig()) {
            logger.severe("ZaloPay configuration is invalid");
            return "{\"return_code\": 0, \"return_message\": \"ZaloPay configuration is invalid\"}";
        }

        Random rand = new Random();
        int randomId = rand.nextInt(1000000);

        Object amountObj = orderRequest.get("amount");
        if (amountObj == null || amountObj.toString().trim().isEmpty()) {
            logger.warning("Amount is required");
            return "{\"return_code\": 0, \"return_message\": \"Amount is required\"}";
        }

        Long amount;
        try {
            amount = Long.parseLong(amountObj.toString());
        } catch (NumberFormatException e) {
            logger.warning("Invalid amount format: " + amountObj);
            return "{\"return_code\": 0, \"return_message\": \"Amount must be a valid number\"}";
        }

        Map<String, Object> order = new HashMap<>();
        order.put("appid", ZalopayConfig.config.get("appid"));
        order.put("apptransid", getCurrentTimeString("yyMMdd") + "_" + randomId);
        order.put("apptime", System.currentTimeMillis());
        order.put("appuser", orderRequest.getOrDefault("app_user", "user123"));
        order.put("amount", amount);
        order.put("description", orderRequest.getOrDefault("description", "Thanh toán qua ZaloPay #" + randomId));
        order.put("bankcode", "");
        order.put("item", orderRequest.getOrDefault("items", "[{}]"));
        order.put("embeddata", orderRequest.getOrDefault("embed_data", "{}"));
        order.put("callbackurl", "http://localhost:8080/api/zalopay/callback"); // Cần thay đổi khi deploy

        String data = order.get("appid") + "|" + order.get("apptransid") + "|" + order.get("appuser") + "|"
                + order.get("amount") + "|" + order.get("apptime") + "|" + order.get("embeddata") + "|"
                + order.get("item");

        String mac = HMACUtil.HMacHexStringEncode(HMACUtil.HMACSHA256, ZalopayConfig.config.get("key1"), data);
        order.put("mac", mac);

        logger.info("Generated order: " + order);
        logger.info("Generated MAC: " + mac);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(ZalopayConfig.config.get("endpoint"));

            List<NameValuePair> params = new ArrayList<>();
            for (Map.Entry<String, Object> entry : order.entrySet()) {
                params.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
            }

            post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            try (CloseableHttpResponse response = client.execute(post)) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                StringBuilder resultJsonStr = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    resultJsonStr.append(line);
                }

                logger.info("Zalopay Response: " + resultJsonStr.toString());
                return resultJsonStr.toString();
            }
        } catch (Exception e) {
            logger.severe("Failed to create order: " + e.getMessage());
            return "{\"return_code\": 0, \"return_message\": \"Failed to create order: " + e.getMessage() + "\"}";
        }
    }

    public String getOrderStatus(String appTransId) {
        if (appTransId == null || appTransId.trim().isEmpty()) {
            return "{\"return_code\": 0, \"return_message\": \"app_trans_id is required\"}";
        }
        if (!validateConfig()) {
            return "{\"return_code\": 0, \"return_message\": \"ZaloPay configuration is invalid\"}";
        }

        String data = ZalopayConfig.config.get("appid") + "|" + appTransId + "|" + ZalopayConfig.config.get("key1");
        String mac = HMACUtil.HMacHexStringEncode(HMACUtil.HMACSHA256, ZalopayConfig.config.get("key1"), data);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(ZalopayConfig.config.get("orderstatus"));

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("appid", ZalopayConfig.config.get("appid")));
            params.add(new BasicNameValuePair("apptransid", appTransId));
            params.add(new BasicNameValuePair("mac", mac));

            post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            try (CloseableHttpResponse response = client.execute(post)) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                StringBuilder resultJsonStr = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    resultJsonStr.append(line);
                }

                logger.info("Zalopay Order Status Response: " + resultJsonStr.toString());
                return resultJsonStr.toString();
            }
        } catch (Exception e) {
            logger.severe("Failed to get order status: " + e.getMessage());
            return "{\"return_code\": 0, \"return_message\": \"Failed to get order status: " + e.getMessage() + "\"}";
        }
    }
}