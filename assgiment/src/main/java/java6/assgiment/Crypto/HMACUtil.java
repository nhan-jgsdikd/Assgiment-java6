package java6.assgiment.Crypto;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedList;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HMACUtil {

    public final static String HMACMD5 = "HmacMD5";
    public final static String HMACSHA1 = "HmacSHA1";
    public final static String HMACSHA256 = "HmacSHA256";
    public final static String HMACSHA512 = "HmacSHA512";

    public final static LinkedList<String> HMACS = new LinkedList<String>(
        Arrays.asList("UnSupport", "HmacSHA256", "HmacMD5", "HmacSHA384", "HmacSHA1", "HmacSHA512")
    );

    private static byte[] HMacEncode(final String algorithm, final String key, final String data) {
        try {
            Mac macGenerator = Mac.getInstance(algorithm);
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), algorithm);
            macGenerator.init(signingKey);
            return macGenerator.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            ex.printStackTrace(); // In lỗi ra console để dễ debug
            return null;
        }
    }

    public static String HMacBase64Encode(final String algorithm, final String key, final String data) {
        byte[] hmacEncodeBytes = HMacEncode(algorithm, key, data);
        return (hmacEncodeBytes != null) ? Base64.getEncoder().encodeToString(hmacEncodeBytes) : null;
    }

    public static String HMacHexStringEncode(final String algorithm, final String key, final String data) {
        byte[] hmacEncodeBytes = HMacEncode(algorithm, key, data);
        return (hmacEncodeBytes != null) ? HexStringUtil.byteArrayToHexString(hmacEncodeBytes) : null;
    }
}
