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
        return ZalopayConfig.config.get("app_id") != null &&
               ZalopayConfig.config.get("key1") != null &&
               ZalopayConfig.config.get("endpoint") != null &&
               ZalopayConfig.config.get("orderstatus") != null;
    }

    public String createOrder(Map<String, Object> orderRequest) {
        Random rand = new Random();
        int randomId = rand.nextInt(1000000);

        Object amount = orderRequest.get("amount");
        if (amount == null || amount.toString().trim().isEmpty()) {
            return "{\"error\": \"Amount is required\"}";
        }
        try {
            Long.parseLong(amount.toString());
        } catch (NumberFormatException e) {
            return "{\"error\": \"Amount must be a valid number\"}";
        }

        if (!validateConfig()) {
            return "{\"error\": \"ZaloPay configuration is invalid\"}";
        }

        Map<String, Object> order = new HashMap<>();
        order.put("app_id", ZalopayConfig.config.get("app_id"));
        order.put("app_trans_id", getCurrentTimeString("yyMMdd") + "_" + randomId);
        order.put("app_time", System.currentTimeMillis());
        order.put("app_user", orderRequest.getOrDefault("app_user", "user123"));
        order.put("amount", amount);
        order.put("description", "SN Mobile - Payment for the order #" + randomId);
        order.put("bank_code", "");
        order.put("item", orderRequest.getOrDefault("items", "[{}]"));
        order.put("embed_data", orderRequest.getOrDefault("embed_data", "{}"));
        order.put("callback_url", "http://localhost:8080/api/zalopay/callback"); // Thay bằng URL thực tế nếu deploy

        String data = order.get("app_id") + "|" + order.get("app_trans_id") + "|" + order.get("app_user") + "|"
                + order.get("amount") + "|" + order.get("app_time") + "|" + order.get("embed_data") + "|"
                + order.get("item");

        String mac = HMACUtil.HMacHexStringEncode(HMACUtil.HMACSHA256, ZalopayConfig.config.get("key1"), data);
        order.put("mac", mac);

        logger.info("Generated MAC: " + mac);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(ZalopayConfig.config.get("endpoint"));

            List<NameValuePair> params = new ArrayList<>();
            for (Map.Entry<String, Object> entry : order.entrySet()) {
                params.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
            }

            post.setEntity(new UrlEncodedFormEntity(params));

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
            return "{\"error\": \"Failed to create order: " + e.getMessage() + "\"}";
        }
    }

    public String getOrderStatus(String appTransId) {
        if (appTransId == null || appTransId.trim().isEmpty()) {
            return "{\"error\": \"app_trans_id is required\"}";
        }
        if (!validateConfig()) {
            return "{\"error\": \"ZaloPay configuration is invalid\"}";
        }

        String data = ZalopayConfig.config.get("app_id") + "|" + appTransId + "|" + ZalopayConfig.config.get("key1");
        String mac = HMACUtil.HMacHexStringEncode(HMACUtil.HMACSHA256, ZalopayConfig.config.get("key1"), data);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(ZalopayConfig.config.get("orderstatus"));

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("app_id", ZalopayConfig.config.get("app_id")));
            params.add(new BasicNameValuePair("app_trans_id", appTransId));
            params.add(new BasicNameValuePair("mac", mac));

            post.setEntity(new UrlEncodedFormEntity(params));

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
            return "{\"error\": \"Failed to get order status: " + e.getMessage() + "\"}";
        }
    }
}