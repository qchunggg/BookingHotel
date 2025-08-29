package com.hotelbooking.Repositories;

import com.hotelbooking.Entities.RoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomRepository extends JpaRepository<RoomEntity, Long>, JpaSpecificationExecutor<RoomEntity> {

    @Query(
            "SELECT COUNT(r) > 0 FROM RoomEntity r " +
                    "WHERE r.hotel.id = :hotelId " +
                    "AND LOWER(TRIM(r.roomNumber)) = LOWER(TRIM(:roomNumber)) " +
                    "AND (:excludeId IS NULL OR r.id <> :excludeId)"
    )
    boolean existsDuplicate(
            @Param("hotelId") Long hotelId,
            @Param("roomNumber") String roomNumber,
            @Param("excludeId") Long excludeId // null khi create, khác null khi update
    );

    List<RoomEntity> findByHotelId(Long hotelId);
}
