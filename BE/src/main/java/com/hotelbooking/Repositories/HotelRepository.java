package com.hotelbooking.Repositories;

import com.hotelbooking.Entities.HotelEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HotelRepository extends JpaRepository<HotelEntity, Long>, JpaSpecificationExecutor<HotelEntity> {

    @Query(
            "SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END " +
                    "FROM   HotelEntity h " +
                    "WHERE  LOWER(TRIM(h.name))    = :nameNorm " +
                    "  AND  LOWER(TRIM(h.address)) = :addrNorm " +
                    "  AND  LOWER(TRIM(h.city))    = :cityNorm " +
                    "  AND  h.user.id              = :userId " +
                    "  AND  (:id IS NULL OR h.id <> :id)"
    )
    boolean existsDuplicate(@Param("nameNorm") String nameNorm,
                            @Param("addrNorm") String addrNorm,
                            @Param("cityNorm") String cityNorm,
                            @Param("userId")   Long   userId,
                            @Param("id")       Long   id);

    Page<HotelEntity> findAllByUserId(Long userId, Pageable pageable);
}
