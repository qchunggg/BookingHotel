package com.hotelbooking.DTO.BookingDTO;

import com.hotelbooking.DTO.PageFilterDTO;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingFilterDTO extends PageFilterDTO {

    private Long userId;           // tìm trực tiếp theo userId
    private String fullName;       // hoặc tìm mờ theo họ tên (fullName / userName)
    private LocalDate checkInDate; // từ ngày nhận
    private LocalDate checkOutDate;  // đến ngày trả
}
