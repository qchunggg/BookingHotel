package com.hotelbooking.Entities;

import com.hotelbooking.Enums.RoomStatus;
import com.hotelbooking.Enums.RoomType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ROOMS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ROOM_NUMBER", nullable = false, length = 20)
    private String roomNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "ROOM_TYPE", nullable = false, length = 50)
    private RoomType roomType;

    @Column(name = "PRICE_PER_DAY", nullable = false)
    private Integer pricePerDay;

    @Enumerated(EnumType.STRING)
    @Column(name = "ROOM_STATUS", nullable = false)
    private RoomStatus roomStatus;

    @Column(name = "ROOM_IMAGE")
    private String roomImage;  // Lưu trữ ảnh dưới dạng mảng byte

    // Quan hệ Nhiều phòng thuộc về 1 khách sạn
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private HotelEntity hotel;
}
