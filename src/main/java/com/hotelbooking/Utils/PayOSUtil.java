package com.hotelbooking.Utils;


import lombok.experimental.UtilityClass;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Tiện ích ký/kiểm tra chữ ký cho PayOS.
 * Thuật toán chính thức: HMAC-SHA256 trên chuỗi query "key1=value1&key2=value2..."
 * (các key sắp xếp alphabet tăng dần, không urlencode).
 */
@UtilityClass
public class PayOSUtil {

    /**
     * Tạo chữ ký cho request gửi lên PayOS.
     *
     * @param params      map tham số (chỉ các cặp key PayOS yêu cầu)
     * @param checksumKey key bí mật lấy từ dashboard (Checksum Key)
     * @return chuỗi hex-lowercase
     */
    public static String signRequest(Map<String, Object> params, String checksumKey) {
        String raw = buildRaw(params);
        return hmacSHA256(raw, checksumKey);
    }

    /**
     * Dùng cho webhook: kiểm tra chữ ký PayOS gửi về.
     */
    public static boolean verifySignature(Map<String, Object> payload,
                                          String checksumKey,
                                          String signatureFromPayOS) {
        String raw = buildRaw(payload);
        String expected = hmacSHA256(raw, checksumKey);
        return expected.equals(signatureFromPayOS);
    }

    /* ----------------- helper private ----------------- */

    public static String buildRaw(Map<String, Object> params) {
        return params.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())                 // sort key ASC
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
    }

    public static String hmacSHA256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);  // ← thay cho DatatypeConverter
        } catch (Exception e) {
            throw new IllegalStateException("Không thể xác thực PayOS", e);
        }
    }

    public static String signCheckout(Long amount,
                                      Long orderCode,
                                      String description,
                                      String returnUrl,
                                      String cancelUrl,
                                      String checksumKey) {

        String raw = "amount="      + amount +
                "&orderCode="  + orderCode +
                "&description=" + description +
                "&returnUrl="  + returnUrl +
                "&cancelUrl="  + cancelUrl;

        return hmacSHA256(raw, checksumKey);
    }
}
