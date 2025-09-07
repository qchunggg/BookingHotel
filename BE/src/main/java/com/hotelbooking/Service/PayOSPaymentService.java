package com.hotelbooking.Service;

public interface PayOSPaymentService {

    /**
     * Tạo URL (hoặc QR) thanh toán cho 1 đơn đặt phòng.
     *
     * @param orderCode   mã đơn nội bộ (Long, duy nhất)
     * @param amountVnd   số tiền VND (LONG, không nhân 100)
     * @param description mô tả hiển thị cho khách
     * @return checkoutUrl (redirect hoặc gắn QR)
     */
    String createCheckoutLink(Long bookingId, Long orderCode, Long amountVnd, String description);

    /**
     * Xử lý webhook (IPN) mà PayOS gọi về – xác thực chữ ký, cập nhật DB.
     *
     * @param payload   JSON gốc PayOS gửi (String hoặc Map)
     * @param signature header "x-signature" (nếu bạn lấy qua Header)
     *                  hoặc trường "signature" trong body JSON
     */
    void handleWebhook(String payload, String signature);

    /**
     * Xác minh trạng thái thanh toán (nếu cần tra cứu chủ động).
     *
     * @param orderCode mã đơn
     * @return true = đã thanh toán thành công
     */
    boolean queryPaymentStatus(Long orderCode);

    /**
     * Huỷ (void) một yêu cầu thanh toán nếu khách bấm “Huỷ”.
     * – tùy use-case, PayOS hỗ trợ endpoint DELETE.
     */
    void cancelPayment(Long orderCode, String reason);
}
