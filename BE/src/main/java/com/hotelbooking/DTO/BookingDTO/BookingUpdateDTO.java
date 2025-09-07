package com.hotelbooking.DTO.BookingDTO;
import com.hotelbooking.Enums.BookingStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import static com.hotelbooking.Constants.Constants.DateFormatType.INPUT_MESSAGE;
import static com.hotelbooking.Constants.Constants.DateFormatType.INPUT_REGEX;

@Data
public class BookingUpdateDTO {

    @NotNull(message = "ID booking không được để trống")
    private Long id;

    @NotNull(message = "ID người dùng không được để trống")
    private Long userId;

    @NotNull(message = "ID phòng không được để trống")
    private Long roomId;

    @NotBlank(message = "Ngày nhận phòng không được để trống")
    @Pattern(regexp = INPUT_REGEX, message = INPUT_MESSAGE)
    private String checkInDate;

    @NotBlank(message = "Ngày trả phòng không được để trống")
    @Pattern(regexp = INPUT_REGEX, message = INPUT_MESSAGE)
    private String checkOutDate;

    @NotNull(message = "Tổng giá không được để trống")
    @Positive(message = "Tổng giá phải lớn hơn 0")
    private Integer totalPrice;

    private BookingStatus bookingStatus;
}
