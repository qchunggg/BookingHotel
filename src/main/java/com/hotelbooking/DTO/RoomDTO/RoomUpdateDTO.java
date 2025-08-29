package com.hotelbooking.DTO.RoomDTO;

import com.hotelbooking.Enums.RoomStatus;
import com.hotelbooking.Enums.RoomType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class RoomUpdateDTO {

    @NotNull(message = "ID phòng không được để trống")
    private Long id;

    @NotBlank(message = "Số phòng không được để trống")
    @Size(max = 20, message = "Số phòng tối đa 20 ký tự")
    private String roomNumber;

    @NotNull(message = "Loại phòng không được để trống")
    private RoomType roomType;

    @NotNull(message = "Giá phòng theo ngày không được để trống")
    @Positive(message = "Giá phòng theo ngày phải lớn hơn 0")
    private Integer pricePerDay;

    @NotNull(message = "Trạng thái phòng không được để trống")
    private RoomStatus roomStatus;

    @URL(message = "Địa chỉ ảnh không hợp lệ")
    private String roomImage;

    @NotNull(message = "ID khách sạn không được để trống")
    private Long hotelId;
}
