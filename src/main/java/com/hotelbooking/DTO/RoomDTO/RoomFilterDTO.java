package com.hotelbooking.DTO.RoomDTO;

import com.hotelbooking.DTO.PageFilterDTO;
import com.hotelbooking.Enums.RoomStatus;
import com.hotelbooking.Enums.RoomType;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RoomFilterDTO extends PageFilterDTO {

    private Long hotelId;

    private String name;

    private RoomStatus roomStatus;

    private RoomType roomType;

    private Integer minPricePerDay;
    private Integer maxPricePerDay;

    private LocalDate checkIn;
    private LocalDate checkOut;
}
