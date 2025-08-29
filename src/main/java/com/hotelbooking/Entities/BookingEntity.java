package com.hotelbooking.Entities;
import com.hotelbooking.Enums.BookingStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "BOOKINGS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nhiều booking thuộc về 1 user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    // Nhiều booking thuộc về 1 phòng
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private RoomEntity room;

    @Column(name = "CHECK_IN_DATE", nullable = false)
    private LocalDateTime checkInDate;

    @Column(name = "CHECK_OUT_DATE", nullable = false)
    private LocalDateTime checkOutDate;

    @Column(nullable = false)
    private Integer totalPrice;

    @Column(name = "PRICE_PER_DAY", nullable = false)
    private Integer pricePerDay;

    // 👇 NEW: tiền đặt cọc (tuỳ rule: mặc định % theo config, có thể 0)
    @Column(name = "DEPOSIT_AMOUNT")
    private Integer depositAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "BOOKING_STATUS", nullable = false, length = 20)
    private BookingStatus bookingStatus;
}
