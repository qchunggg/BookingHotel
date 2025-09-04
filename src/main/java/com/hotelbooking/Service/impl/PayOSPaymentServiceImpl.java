package com.hotelbooking.Service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelbooking.Config.PayOSCredentialProvider;
import com.hotelbooking.Config.PayOSCredentials;
import com.hotelbooking.Config.PayOSProps;
import com.hotelbooking.Entities.BookingEntity;
import com.hotelbooking.Entities.PaymentEntity;
import com.hotelbooking.Mappers.PayOSRequestMapper;
import com.hotelbooking.Repositories.BookingRepository;
import com.hotelbooking.Repositories.PaymentRepository;
import com.hotelbooking.Service.BookingService;
import com.hotelbooking.Service.PayOSPaymentService;
import com.hotelbooking.Utils.PayOSUtil;
import jakarta.transaction.Transactional;
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
    private final BookingRepository bookingRepository;        // ← cần để truy hotel
    private final PayOSCredentialProvider credentialProvider;
    private final PaymentRepository paymentRepository;
    private final BookingService bookingService;
    private final RestTemplate rest = new RestTemplate();      // hoặc @Bean ngoài Config
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    @Transactional
    public String createCheckoutLink(Long bookingId, Long orderCode, Long amountVnd, String description) {

        BookingEntity booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking không tồn tại"));
        Long hotelId = booking.getRoom().getHotel().getId();

        PayOSCredentials cred = credentialProvider.ofHotel(hotelId);

        /* 1) Build & ký body qua mapper */
        Map<String,Object> body = PayOSRequestMapper.toRequestBody(
                orderCode,
                amountVnd,
                description,
                props.getCancelUrl(),
                props.getReturnUrl(),
                cred.checksumKey()
        );

        /* 2) Header xác thực PayOS */
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.set("x-client-id", cred.clientId());
        h.set("x-api-key",  cred.apiKey());

        /* D) Gọi PayOS */
        String url = props.getBaseUrl() + "/v2/payment-requests";
        ResponseEntity<String> resp =
                rest.postForEntity(url, new HttpEntity<>(body, h), String.class);

        if (!resp.getStatusCode().is2xxSuccessful())
            throw new IllegalStateException("PayOS HTTP " + resp.getStatusCode());

        JsonNode root;
        try { root = mapper.readTree(resp.getBody()); }
        catch (Exception e) { throw new IllegalStateException("Cannot parse PayOS response", e); }

        if (!"00".equals(root.path("code").asText()))
            throw new IllegalStateException("PayOS error: " + root.path("desc").asText());

        String checkoutUrl = root.path("data").path("checkoutUrl").asText();

        /* E) Lưu Payment (lưu cả hotelId để tiện webhook) */
        paymentRepository.save(PaymentEntity.builder()
                .bookingId(bookingId)
                .hotelId(hotelId)          // ← thêm cột này nếu chưa có
                .orderCode(orderCode)
                .amount(amountVnd.intValue())         // giữ Long → không mất tiền lẻ
                .success(false)
                .createdAt(LocalDateTime.now())
                .build());

        return checkoutUrl;
    }

    @Override
    @Transactional
    public void handleWebhook(String payload, String signature) {

        try {
            /* 1) Parse JSON { code, desc, data{...}, signature } */
            JsonNode root      = mapper.readTree(payload);
            JsonNode dataNode  = root.path("data");

            long orderCode     = dataNode.path("orderCode").asLong();
            if (orderCode == 0) throw new IllegalArgumentException("orderCode missing");

            /* 2) Lấy Payment → hotelId → checksumKey */
            PaymentEntity payment = paymentRepository.findByOrderCode(orderCode)
                    .orElseThrow(() -> new IllegalStateException("Payment không tồn tại"));

            Long hotelId = payment.getHotelId();                       // bạn đã lưu ở bước create
            String checksumKey = credentialProvider.ofHotel(hotelId)
                    .checksumKey();     // key riêng

            /* 3) Verify chữ ký */
            Map<String,Object> dataMap = mapper.convertValue(
                    dataNode, new TypeReference<>() {});

            boolean valid = PayOSUtil.verifySignature(dataMap, checksumKey, signature);
            if (!valid) throw new IllegalArgumentException("Invalid PayOS signature");

            /* 4) Chỉ xử lý khi thành công */
            if (!"PAID".equalsIgnoreCase(dataNode.path("status").asText())) return;

            /* 5) Idempotent update */
            if (!Boolean.TRUE.equals(payment.getSuccess())) {
                payment.setSuccess(true);
                paymentRepository.save(payment);

                bookingService.confirmBooking(payment.getBookingId());
            }

        } catch (Exception ex) {
            throw new RuntimeException("Xử lý webhook thất bại", ex);
        }
    }

    @Override
    @Transactional
    public boolean queryPaymentStatus(Long orderCode) {

        PaymentEntity payment = paymentRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy payment"));
        if (Boolean.TRUE.equals(payment.getSuccess())) return true;

        PayOSCredentials cred = credentialProvider.ofHotel(payment.getHotelId());

        /* 1) Ký chữ ký GET */
        String signature = PayOSUtil.signRequest(
                Map.of("orderCode", orderCode), cred.checksumKey());

        /* 2) Header */
        HttpHeaders h = new HttpHeaders();
        h.set("x-client-id", cred.clientId());
        h.set("x-api-key",   cred.apiKey());

        /* 3) Gọi PayOS */
        String url = props.getBaseUrl()
                + "/v2/payment-requests/" + orderCode
                + "?signature=" + signature;

        ResponseEntity<String> resp =
                rest.exchange(url, HttpMethod.GET, new HttpEntity<>(h), String.class);

        JsonNode root;
        try { root = mapper.readTree(resp.getBody()); }
        catch (Exception e) { throw new IllegalStateException("Cannot parse PayOS response", e); }
        boolean paid = "00".equals(root.path("code").asText()) &&
                "PAID".equalsIgnoreCase(root.path("data").path("status").asText());

        /* 4) Cập nhật DB */
        if (paid && !Boolean.TRUE.equals(payment.getSuccess())) {
            payment.setSuccess(true);
            paymentRepository.save(payment);
            bookingService.confirmBooking(payment.getBookingId());
        }
        return paid;
    }

    @Override
    public void cancelPayment(Long orderCode, String reason) {
        PaymentEntity payment = paymentRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new NoSuchElementException("Payment không tồn tại"));

        PayOSCredentials cred = credentialProvider.ofHotel(payment.getHotelId());

        /* 1) Endpoint */
        String url = props.getBaseUrl()
                + "/v2/payment-requests/" + orderCode + "/cancel";

        /* 2) Body + ký */
        Map<String, Object> body = new LinkedHashMap<>();
        if (reason != null && !reason.isBlank())
            body.put("cancellationReason", reason);

        body.put("signature", PayOSUtil.signRequest(body, cred.checksumKey()));

        /* 3) Header */
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.set("x-client-id", cred.clientId());
        h.set("x-api-key", cred.apiKey());

        /* 4) POST */
        ResponseEntity<String> resp =
                rest.postForEntity(url, new HttpEntity<>(body, h), String.class);

        JsonNode root;
        try {
            root = mapper.readTree(resp.getBody());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Không thể phân tích phản hồi PayOS", e);
        }
        if (!resp.getStatusCode().is2xxSuccessful() ||
                !"00".equals(root.path("code").asText()))
            throw new IllegalStateException("Huỷ PayOS thất bại: "
                    + root.path("desc").asText());

        /* 5) DB */
        payment.setSuccess(false);
        paymentRepository.save(payment);
    }
}
