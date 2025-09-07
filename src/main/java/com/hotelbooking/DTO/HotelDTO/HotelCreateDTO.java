package com.hotelbooking.DTO.HotelDTO;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class HotelCreateDTO {

    @NotBlank(message = "Tên khách sạn không được để trống")
    @Size(max = 150, message = "Tên khách sạn tối đa 150 ký tự")
    private String name;

    @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;

    @Size(max = 100, message = "Tên thành phố tối đa 100 ký tự")
    @NotBlank(message = "Tên thành phố không được để trống")
    private String city;

    @Size(max = 500, message = "Mô tả tối đa 500 ký tự")
    private String description;

    @NotNull(message = "Rating không được để trống")
    @DecimalMin(value = "1.0", message = "Rating tối thiểu 1.0")
    @DecimalMax(value = "5.0", message = "Rating tối đa 5.0")
    private Double rating;

    @URL(message = "Địa chỉ ảnh không hợp lệ")
    private String hotelImage;

    @NotNull(message = "Tổng số phòng không được để trống")
    @Min(value = 1, message = "Tổng số phòng tối thiểu là 1")
    private Integer totalRooms;

    @NotBlank(message = "PayOs ClientId không được để trống")
    private String payosClientId;

    @NotBlank(message = "PayOs API Key không được để trống")
    private String payosApiKey;

    @NotBlank(message = "PayOS Checksum Key không được để trống")
    private String payosChecksumKey;

    @NotNull(message = "Owner userId không được để trống")
    private Long userId;
}
