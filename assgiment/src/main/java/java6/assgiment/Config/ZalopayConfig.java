package java6.assgiment.Config;

import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ZalopayConfig {

    public static final Map<String, String> config = new HashMap<String, String>() {
        {
<<<<<<< HEAD
            put("appid", "554");
            put("key1", "8NdU5pG5R2spGHGhyO99HN1OhD8IQJBn");
            put("key2", "uUfsWgfLkRLzq6W2uNXTCxrfxs51auny");
            put("endpoint", "https://sandbox.zalopay.com.vn/v001/tpe/createorder");
=======
            put("app_id", "553"); // Sửa từ "appid" thành "app_id"
            put("key1", "9phuAOYhan4urywHTh0ndEXiV3pKHr5Q");
            put("key2", "Iyz2habzyr7AG8SgvoBCbKwKi3UzlLi3");
            put("endpoint", "https://sandbox.zalopay.com.vn/v001/tpe/createorder");
            put("orderstatus", "https://sandbox.zalopay.com.vn/v001/tpe/getstatusbyapptransid"); // Thống nhất sandbox
>>>>>>> 5b7f43e002990d06e1b784d451983dd72f0b2a31
        }
    };
}