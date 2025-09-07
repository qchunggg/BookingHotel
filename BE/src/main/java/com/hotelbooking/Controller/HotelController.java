package com.hotelbooking.Controller;

import com.hotelbooking.DTO.HotelDTO.HotelCreateDTO;
import com.hotelbooking.DTO.HotelDTO.HotelFilterDTO;
import com.hotelbooking.DTO.HotelDTO.HotelResponseDTO;
import com.hotelbooking.DTO.HotelDTO.HotelUpdateDTO;
import com.hotelbooking.Service.HotelService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {

    @Autowired
    private HotelService hotelService;

    // Tạo mới
    @PostMapping("/create-hotel")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<HotelResponseDTO> createHotel(@RequestBody @Valid HotelCreateDTO dto) {
        HotelResponseDTO hotel = hotelService.createHotel(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(hotel);
    }

    // Cập nhật
    @PostMapping("/update-hotel")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<HotelResponseDTO> updateHotel(@RequestBody @Valid HotelUpdateDTO dto) {
        HotelResponseDTO update = hotelService.updateHotel(dto);
        return ResponseEntity.ok(update);
    }

    @PostMapping("/delete-hotels")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteHotels(@RequestBody List<Long> ids) {
        hotelService.deleteHotels(ids);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/get-by-user")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> getHotelByUserId(@RequestBody HotelFilterDTO filter) {
        Pageable  pageable = PageRequest.of(filter.getPage(), filter.getLimit());
        return ResponseEntity.ok(hotelService.getHotelsByUserId(filter.getUserId(), pageable));
    }
}
