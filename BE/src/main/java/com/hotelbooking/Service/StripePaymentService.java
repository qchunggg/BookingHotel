package com.hotelbooking.Service;

public interface StripePaymentService {

    String createCheckoutUrl(Long bookingId, String orderInfo);

    void handleWebhook(String payload, String sigHeader);

    String handleSuccess(String sessionId); // Optional
}
