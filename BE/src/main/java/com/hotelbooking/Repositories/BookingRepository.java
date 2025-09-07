package com.hotelbooking.Repositories;

import com.hotelbooking.Entities.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<BookingEntity, Long>, JpaSpecificationExecutor<BookingEntity> {

    @Query(
            "SELECT COUNT(b) > 0 " +
                    "FROM BookingEntity b " +
                    "WHERE b.room.id = :roomId " +
                    "  AND (b.bookingStatus = 'CONFIRMED' OR b.bookingStatus = 'CHECKED_IN') " +
                    "  AND (:excludeId IS NULL OR b.id <> :excludeId) " +
                    "  AND (b.checkInDate < :checkOut AND b.checkOutDate > :checkIn)"
    )
    boolean existsOverlapping(
            @Param("roomId") Long roomId,
            @Param("checkIn") LocalDateTime checkIn,
            @Param("checkOut") LocalDateTime checkOut,
            @Param("excludeId") Long excludeId
    );

    List<BookingEntity> findByUserIdOrderByCheckInDateDesc(Long userId);

    List<BookingEntity> findByRoomIdOrderByCheckInDateDesc(Long roomId);

    @Query("SELECT b FROM BookingEntity b " +
            "WHERE b.checkInDate < :endDate AND b.checkOutDate > :startDate " +
            "ORDER BY b.checkInDate DESC")
    List<BookingEntity> findIntersecting(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    @Query(
            "SELECT CASE WHEN COUNT(b) > 0 THEN TRUE ELSE FALSE END " +
                    "FROM BookingEntity b " +
                    "WHERE b.room.id IN :roomIds " +
                    "AND b.bookingStatus IN (" +
                    "     com.hotelbooking.Enums.BookingStatus.CONFIRMED," +
                    "     com.hotelbooking.Enums.BookingStatus.CHECKED_IN" +
                    ")" +
                    "  AND b.checkOutDate > :today"
    )
    boolean existsByRoomIdsAndFutureActive(@Param("roomIds") List<Long> roomIds,
                                           @Param("today") LocalDateTime today);

    @Query(
            "SELECT COUNT(b) > 0 FROM BookingEntity b " +
                    "WHERE b.room.id = :roomId " +
                    "  AND b.bookingStatus = com.hotelbooking.Enums.BookingStatus.CONFIRMED " +
                    "  AND b.checkInDate > :today"
    )
    boolean existsConfirmedAfterDate(@Param("roomId") Long roomId, @Param("today") LocalDateTime today);

    @Query("SELECT COUNT(b) > 0 FROM BookingEntity b " +
            "WHERE b.room.id = :roomId " +
            "  AND b.bookingStatus IN (\n" +
            "              com.hotelbooking.Enums.BookingStatus.CONFIRMED,\n" +
            "              com.hotelbooking.Enums.BookingStatus.CHECKED_IN\n" +
            "         )" +
            "  AND b.checkInDate <= :now " +
            "  AND b.checkOutDate  > :now")
    boolean existsConfirmedOngoing(@Param("roomId") Long roomId,
                                   @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(b) > 0 FROM BookingEntity b " +
            "WHERE b.room.id = :roomId " +
            "  AND b.bookingStatus = com.hotelbooking.Enums.BookingStatus.CONFIRMED " +
            "  AND b.checkInDate > :now")
    boolean existsConfirmedAfter(@Param("roomId") Long roomId,
                                 @Param("now") LocalDateTime now);
}
