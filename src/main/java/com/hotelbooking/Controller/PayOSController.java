package com.hotelbooking.Controller;

import com.hotelbooking.DTO.PaymentDTO.PayOSCheckoutRequestDTO;
import com.hotelbooking.DTO.PaymentDTO.PaymentUrlResponse;
import com.hotelbooking.Entities.BookingEntity;
import com.hotelbooking.Repositories.BookingRepository;
import com.hotelbooking.Service.PayOSPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PayOSController {

    private final PayOSPaymentService payOSPaymentService;
    private final BookingRepository bookingRepo;

    @PostMapping("/create-checkout")
    public ResponseEntity<PaymentUrlResponse> createCheckout(@RequestBody PayOSCheckoutRequestDTO req) {

        // 1) Tải booking
        BookingEntity b = bookingRepo.findById(req.getBookingId())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy booking"));

        // 2) Sinh các tham số cho PayOS
        long orderCode = System.currentTimeMillis();               // mã đơn
        long amountVnd = b.getDepositAmount().longValue();         // tiền cọc
        String orderInfo = "Thanh toán đặt phòng #" + b.getId();    // mô tả mặc định

        // 3) Tạo link thanh toán
        String url = payOSPaymentService.createCheckoutLink( b.getId(), orderCode, amountVnd, orderInfo);

        // 4) Trả về cho FE
        return ResponseEntity.ok(new PaymentUrlResponse(url));
    }

    /* ---------- 2. Webhook PayOS ---------- */
    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(@RequestBody String payload, @RequestHeader("x-signature") String sig) {
        payOSPaymentService.handleWebhook(payload, sig);
        return ResponseEntity.ok("Webhook received");
    }

    /* ---------- 3. URL thành công ---------- */
    @GetMapping("/return")
    public ResponseEntity<String> payReturn(@RequestParam("orderCode") Long orderCode) {
        boolean paid = payOSPaymentService.queryPaymentStatus(orderCode);

        return paid
                ? ResponseEntity.ok("Thanh toán thành công. Cảm ơn bạn!")
                : ResponseEntity.ok("Đang chờ xác nhận. Vui lòng thử lại sau ít phút.");
    }

    /* ---------- 4. URL huỷ ---------- */
    @GetMapping("/cancel")
    public ResponseEntity<String> payCancel(@RequestParam("orderCode") Long orderCode) {
        payOSPaymentService.cancelPayment(orderCode, "Khách huỷ tại PayOS");

        return ResponseEntity.ok("Bạn đã huỷ giao dịch. Đơn đặt phòng vẫn ở trạng thái chờ.");
    }
}
