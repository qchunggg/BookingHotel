package com.hotelbooking.Controller;

import com.hotelbooking.DTO.BookingDTO.BookingCreateDTO;
import com.hotelbooking.DTO.BookingDTO.BookingFilterDTO;
import com.hotelbooking.DTO.BookingDTO.BookingResponseDTO;
import com.hotelbooking.DTO.BookingDTO.BookingUpdateDTO;
import com.hotelbooking.Service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping("/search")
    public ResponseEntity<Page<BookingResponseDTO>> search(@RequestBody BookingFilterDTO filter) {
        Page<BookingResponseDTO> page = bookingService.search(filter);
        return ResponseEntity.ok(page);
    }

    @PostMapping("/{id}")
    public ResponseEntity<BookingResponseDTO> getBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    @PostMapping("/get-all-booking")
    public ResponseEntity<List<BookingResponseDTO>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @PostMapping("/create-booking")
    public ResponseEntity<BookingResponseDTO> createBooking(@Valid @RequestBody BookingCreateDTO dto) {
        BookingResponseDTO created = bookingService.createBooking(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/update-booking")
    public ResponseEntity<BookingResponseDTO> updateBooking(@Valid @RequestBody BookingUpdateDTO dto) {
        return ResponseEntity.ok(bookingService.updateBooking(dto));
    }

    @PostMapping("/delete-booking/{id}")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/confirm/{id}")
    public ResponseEntity<BookingResponseDTO> confirmBooking(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.confirmBooking(id));
    }

    @PostMapping("/check-in/{id}")
    public ResponseEntity<BookingResponseDTO> checkIn(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.checkIn(id));
    }

    // 8) Check-out
    @PostMapping("/check-out/{id}")
    public ResponseEntity<BookingResponseDTO> checkOut(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.checkOut(id));
    }

    // 9) Lọc booking theo User
    @PostMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponseDTO>> getBookingsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(bookingService.getBookingsByUser(userId));
    }

    // 10) Lọc booking theo khoảng ngày
    @PostMapping("/between")
    public ResponseEntity<List<BookingResponseDTO>> getBookingsBetweenDates(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime startDate,
            @RequestParam("endDate")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime endDate) {
        return ResponseEntity.ok(bookingService.getBookingsBetweenDates(startDate, endDate));
    }

    // 11) Lọc booking theo Room
    @PostMapping("/room/{roomId}")
    public ResponseEntity<List<BookingResponseDTO>> getBookingsByRoom(@PathVariable Long roomId) {
        return ResponseEntity.ok(bookingService.getBookingsByRoom(roomId));
    }
}
