package com.hotelbooking.Repositories;

import com.hotelbooking.Entities.HotelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HotelRepository extends JpaRepository<HotelEntity, Long>, JpaSpecificationExecutor<HotelEntity> {

    @Query("SELECT COUNT(h) > 0 FROM HotelEntity h " +
            "WHERE LOWER(TRIM(h.name)) = LOWER(TRIM(:name)) " +
            "  AND LOWER(TRIM(h.address)) = LOWER(TRIM(:address)) " +
            "  AND LOWER(TRIM(h.city)) = LOWER(TRIM(:city)) " +
            "  AND (:id IS NULL OR h.id <> :id)")
    boolean existsDuplicate(
            @Param("name") String name,
            @Param("address") String address,
            @Param("city") String city,
            @Param("id") Long id // null khi create, khác null khi update
    );
}
