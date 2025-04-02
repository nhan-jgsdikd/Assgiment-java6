package java6.assgiment.Config;

import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ZalopayConfig {

    public static final Map<String, String> config = new HashMap<String, String>() {
        {
            put("appid", "554");
            put("key1", "8NdU5pG5R2spGHGhyO99HN1OhD8IQJBn");
            put("key2", "uUfsWgfLkRLzq6W2uNXTCxrfxs51auny");
            put("endpoint", "https://sandbox.zalopay.com.vn/v001/tpe/createorder");
        }
    };
}