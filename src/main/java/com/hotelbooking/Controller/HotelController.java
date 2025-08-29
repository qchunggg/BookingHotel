package com.hotelbooking.Controller;

import com.hotelbooking.DTO.HotelDTO.HotelCreateDTO;
import com.hotelbooking.DTO.HotelDTO.HotelFilterDTO;
import com.hotelbooking.DTO.HotelDTO.HotelResponseDTO;
import com.hotelbooking.DTO.HotelDTO.HotelUpdateDTO;
import com.hotelbooking.Service.HotelService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {

    @Autowired
    private HotelService hotelService;

    @PostMapping("/search-hotels")
    public ResponseEntity<Page<HotelResponseDTO>> searchAndFilterHotels(@RequestBody HotelFilterDTO filter) {
        Page<HotelResponseDTO> hotel = hotelService.search(filter);
        return ResponseEntity.ok(hotel);
    }

    @PostMapping("/{id}")
    public ResponseEntity<HotelResponseDTO> getHotelById(@PathVariable Long id) {
        HotelResponseDTO detail = hotelService.getHotelById(id);
        return ResponseEntity.ok(detail);
    }

    // Tạo mới
    @PostMapping("/create-hotel")
    public ResponseEntity<HotelResponseDTO> createHotel(@RequestBody @Valid HotelCreateDTO dto) {
        HotelResponseDTO hotel = hotelService.createHotel(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(hotel);
    }

    // Cập nhật
    @PostMapping("/update-hotel/{id}")
    public ResponseEntity<HotelResponseDTO> updateHotel(@PathVariable Long id, @RequestBody @Valid HotelUpdateDTO dto) {
        HotelResponseDTO update = hotelService.updateHotel(id, dto);
        return ResponseEntity.ok(update);
    }

    @PostMapping("/delete-hotels")
    public ResponseEntity<Void> deleteHotels(@RequestBody List<Long> ids) {
        hotelService.deleteHotels(ids);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
