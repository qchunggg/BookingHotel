package com.hotelbooking.Service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelbooking.Config.PayOSProps;
import com.hotelbooking.Entities.PaymentEntity;
import com.hotelbooking.Mappers.PayOSRequestMapper;
import com.hotelbooking.Repositories.PaymentRepository;
import com.hotelbooking.Service.BookingService;
import com.hotelbooking.Service.PayOSPaymentService;
import com.hotelbooking.Utils.PayOSUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class PayOSPaymentServiceImpl implements PayOSPaymentService {

    private final PayOSProps props;
    private final PaymentRepository paymentRepository;
    private final BookingService bookingService;
    private final RestTemplate rest = new RestTemplate();      // hoặc @Bean ngoài Config
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String createCheckoutLink(Long bookingId, Long orderCode, Long amountVnd, String description) {
        /* 1) Build & ký body qua mapper */
        Map<String,Object> body = PayOSRequestMapper.toRequestBody(
                orderCode,
                amountVnd,
                description,
                props.getCancelUrl(),
                props.getReturnUrl(),
                props.getChecksumKey()
        );

        /* 2) Header xác thực PayOS */
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.set("x-client-id", props.getClientId());
        h.set("x-api-key",   props.getApiKey());

        /* 3) Call API */
        String url = props.getBaseUrl() + "/v2/payment-requests";
        HttpEntity<Map<String,Object>> req = new HttpEntity<>(body, h);
        ResponseEntity<String> resp = rest.postForEntity(url, req, String.class);
        if (resp.getStatusCode() != HttpStatus.OK)
            throw new IllegalStateException("PayOS HTTP " + resp.getStatusCode());

        /* 4) Parse kết quả */
        JsonNode root;
        try { root = mapper.readTree(resp.getBody()); }
        catch (Exception e) { throw new IllegalStateException("Cannot parse PayOS response", e); }

        if (!"00".equals(root.path("code").asText()))
            throw new IllegalStateException("PayOS error: " + root.path("desc").asText());

        String checkoutUrl = root.path("data").path("checkoutUrl").asText();

        /* 5) Lưu DB */
        paymentRepository.save(PaymentEntity.builder()
                .bookingId(bookingId)
                .orderCode(orderCode)
                .amount(amountVnd.intValue())
                .success(false)
                .createdAt(LocalDateTime.now())
                .build());

        return checkoutUrl;        // gửi về FE / redirect
    }

    @Override
    public void handleWebhook(String payload, String signature) {
        try {
            /* 1) Parse JSON ─ PayOS gửi { code, desc, data{...}, signature } */
            JsonNode root = mapper.readTree(payload);
            JsonNode dataNode = root.path("data");             // phần cần xác thực

            /* 2) Chuyển data → Map, verify HMAC */
            Map<String,Object> dataMap = mapper.convertValue(
                    dataNode, new TypeReference<Map<String,Object>>() {});

            boolean valid = PayOSUtil.verifySignature(
                    dataMap, props.getChecksumKey(), signature);
            if (!valid) {
                throw new IllegalArgumentException("Invalid PayOS signature");
            }

            /* 3) Kiểm tra kết quả giao dịch */
            String payStatus = dataNode.path("status").asText();   // PAID / CANCELED …
            if (!"PAID".equalsIgnoreCase(payStatus)) return;       // chỉ xử lý thành công

            long orderCode = dataNode.path("orderCode").asLong();

            /* 4) Cập nhật PaymentEntity & booking */
            paymentRepository.findByOrderCode(orderCode).ifPresent(payment -> {
                if (Boolean.TRUE.equals(payment.getSuccess())) return; // đã xử lý

                payment.setSuccess(true);
                paymentRepository.save(payment);

                bookingService.confirmBooking(payment.getBookingId()); // ↵ hàm bạn đã có
            });

        } catch (Exception ex) {
            throw new RuntimeException("Webhook xử lý thất bại", ex);
        }
    }

    @Override
    public boolean queryPaymentStatus(Long orderCode) {
        // 0) Nếu DB đã success -> trả ngay
        PaymentEntity payment = paymentRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy payment"));
        if (Boolean.TRUE.equals(payment.getSuccess())) return true;

        /* 1) Tạo chữ ký (chỉ cần orderCode) */
        Map<String, Object> sigMap = Map.of("orderCode", orderCode);
        String signature = PayOSUtil.signRequest(sigMap, props.getChecksumKey());

        /* 2) Header xác thực */
        HttpHeaders h = new HttpHeaders();
        h.set("x-client-id", props.getClientId());
        h.set("x-api-key",   props.getApiKey());

        /* 3) Call GET */
        String url = props.getBaseUrl()
                + "/v2/payment-requests/" + orderCode
                + "?signature=" + signature;

        ResponseEntity<String> resp = rest.exchange(url, HttpMethod.GET,
                new HttpEntity<>(h), String.class);

        JsonNode root;
        try {
            root = mapper.readTree(resp.getBody());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Không thể phân tích phản hồi PayOS", e);
        }
        boolean paid = "00".equals(root.path("code").asText())
                && "PAID".equalsIgnoreCase(root.path("data").path("status").asText());

        /* 4) Nếu đã thanh toán → update DB & booking */
        if (paid) {
            payment.setSuccess(true);
            paymentRepository.save(payment);
            bookingService.confirmBooking(payment.getBookingId());
        }
        return paid;
    }

    @Override
    public void cancelPayment(Long orderCode, String reason) {
        // 1) Endpoint đúng
        String url = props.getBaseUrl()               // https://api-merchant.payos.vn
                + "/v2/payment-requests/" + orderCode + "/cancel";

        // 2) Body – PayOS chấp nhận trống; nếu muốn ghi lý do:
        Map<String, Object> body = new LinkedHashMap<>();
        if (reason != null && !reason.isBlank())
            body.put("cancellationReason", reason);

        // 3) Ký body (PayOS yêu cầu với mọi request có JSON body)
        body.put("signature", PayOSUtil.signRequest(body, props.getChecksumKey()));

        // 4) Header xác thực
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.set("x-client-id", props.getClientId());
        h.set("x-api-key", props.getApiKey());

        // 5) Gửi POST
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, h);
        ResponseEntity<String> resp = rest.postForEntity(url, req, String.class);

        if (!resp.getStatusCode().is2xxSuccessful())
            throw new IllegalStateException("PayOS HTTP " + resp.getStatusCode());

        JsonNode root;
        try {
            root = mapper.readTree(resp.getBody());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Không thể phân tích phản hồi PayOS", e);
        }
        if (!"00".equals(root.path("code").asText()))
            throw new IllegalStateException("Huỷ PayOS thất bại: "
                    + root.path("desc").asText());

        // 6) Cập nhật DB
        paymentRepository.findByOrderCode(orderCode).ifPresent(p -> {
            p.setSuccess(false);
            paymentRepository.save(p);
        });
    }
}
