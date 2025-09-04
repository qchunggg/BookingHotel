package com.hotelbooking.Service;

import com.hotelbooking.DTO.BookingDTO.BookingCreateDTO;
import com.hotelbooking.DTO.BookingDTO.BookingFilterDTO;
import com.hotelbooking.DTO.BookingDTO.BookingResponseDTO;
import com.hotelbooking.DTO.BookingDTO.BookingUpdateDTO;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingService {

    Page<BookingResponseDTO> search(BookingFilterDTO filter);

    BookingResponseDTO getBookingById(Long id);

    List<BookingResponseDTO> getAllBookings();

    BookingResponseDTO createBooking(BookingCreateDTO dto);

    BookingResponseDTO updateBooking(BookingUpdateDTO dto);

    void cancelBooking(Long id);

    BookingResponseDTO confirmBooking(Long id);

    BookingResponseDTO checkIn(Long id);

    BookingResponseDTO checkOut(Long id);

    List<BookingResponseDTO> getBookingsByUser(Long userId);

    List<BookingResponseDTO> getBookingsBetweenDates(LocalDateTime startDate, LocalDateTime endDate);

    List<BookingResponseDTO> getBookingsByRoom(Long roomId);
}
