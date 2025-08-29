package com.hotelbooking.Mappers;

import com.hotelbooking.Utils.PayOSUtil;

import java.util.LinkedHashMap;
import java.util.Map;

public final class PayOSRequestMapper {

    private PayOSRequestMapper() {}

    public static Map<String,Object> toRequestBody(
            Long orderCode, Long amount, String desc,
            String baseCancelUrl, String baseReturnUrl, String checksumKey) {

        String cancelUrl = baseCancelUrl + "?orderCode=" + orderCode;
        String returnUrl = baseReturnUrl  + "?orderCode=" + orderCode;

        Map<String,Object> body = new LinkedHashMap<>();
        body.put("amount",      amount);
        body.put("cancelUrl",   cancelUrl);
        body.put("description", desc);
        body.put("orderCode",   orderCode);
        body.put("returnUrl",   returnUrl);

        String raw = PayOSUtil.buildRaw(body);          // sort theo alphabet
        String sig = PayOSUtil.hmacSHA256(raw, checksumKey);
        body.put("signature", sig);
        return body;
    }
}
