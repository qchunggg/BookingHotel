package com.hotelbooking.DTO.HotelDTO;

import lombok.Data;

@Data
public class HotelResponseDTO {

    private Long id;

    private String name;

    private String address;

    private String city;

    private String description;

    private Double rating;

    private String hotelImage;

    private Integer totalRooms;

    private Long userId;
}
