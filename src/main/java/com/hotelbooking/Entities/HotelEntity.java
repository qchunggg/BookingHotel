package com.hotelbooking.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "HOTELS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 255)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 500)
    private String description;

    @Column
    private Double rating;

    @Column(name = "HOTEL_IMAGE")
    private String hotelImage;

    @Column(name = "TOTAL_ROOMS")
    private Integer totalRooms;

    @Column(length = 255,  name = "PAYOS_CLIENT_ID")
    private String payosClientId;

    @Column(length = 255,  name = "PAYOS_API_KEY")
    private String payosApiKey;

    @Column(length = 255, name = "PAYOS_CHECKSUM_KEY")
    private String payosChecksumKey;
}
