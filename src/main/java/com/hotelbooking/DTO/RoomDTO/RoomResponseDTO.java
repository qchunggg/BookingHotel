package com.hotelbooking.DTO.RoomDTO;

import com.hotelbooking.Enums.RoomStatus;
import com.hotelbooking.Enums.RoomType;
import lombok.Data;

@Data
public class RoomResponseDTO {

    private Long id;

    private String roomNumber;

    private RoomType roomType;

    private Integer pricePerDay;

    private RoomStatus roomStatus;

    private String roomImage;

    private Long hotelId;

    private String name;
}
