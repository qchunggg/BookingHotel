package com.hotelbooking.Enums;

public enum BookingStatus {
    PENDING,       // Vừa đặt, chưa xác nhận
    CONFIRMED,     // Đã xác nhận, chưa check-in
    CHECKED_IN,    // Đang ở
    CHECKED_OUT,   // Đã trả phòng
}
