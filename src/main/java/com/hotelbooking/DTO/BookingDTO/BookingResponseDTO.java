package com.hotelbooking.DTO.BookingDTO;

import com.hotelbooking.Enums.BookingStatus;
import com.hotelbooking.Enums.RoomStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingResponseDTO {

    private Long id;

    private Long userId;

    private String fullName;

    private Long roomId;

    private String checkInDate;

    private String checkOutDate;

    private Integer totalPrice;

    private Integer pricePerDay;     // 👈 giá 1 ngày (snapshot lúc booking)

    private Integer depositAmount;   // 👈 tiền cọc (ví dụ 30%)

    private BookingStatus bookingStatus;

    private RoomStatus roomStatus;
}
