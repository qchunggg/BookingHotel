package com.hotelbooking.Enums;

public enum RoomStatus {
    AVAILABLE,       // Phòng trống, có thể đặt
    RESERVED,        // Đã được đặt (booking trong tương lai, khách chưa check-in)
    OCCUPIED,        // Đang có khách ở (đã check-in)
    OUT_OF_SERVICE   // Ngưng phục vụ (bảo trì, sửa chữa, dọn dẹp)
}
