package com.hotelbooking.Controller;

import com.hotelbooking.DTO.RoomDTO.RoomCreateDTO;
import com.hotelbooking.DTO.RoomDTO.RoomFilterDTO;
import com.hotelbooking.DTO.RoomDTO.RoomResponseDTO;
import com.hotelbooking.DTO.RoomDTO.RoomUpdateDTO;
import com.hotelbooking.Service.RoomService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @PostMapping("/search")
    public ResponseEntity<Page<RoomResponseDTO>> searchRooms(@RequestBody RoomFilterDTO filter) {
        Page<RoomResponseDTO> rooms = roomService.search(filter);
        return ResponseEntity.ok(rooms);
    }

    @PostMapping("/hotel/{hotelId}")
    public ResponseEntity<List<RoomResponseDTO>> getAllRoomsByHotel(@PathVariable Long hotelId) {
        List<RoomResponseDTO> rooms = roomService.getAllRoomsByHotel(hotelId);
        return ResponseEntity.ok(rooms);
    }

    @PostMapping("/{id}")
    public ResponseEntity<RoomResponseDTO> getRoomById(@PathVariable Long id) {
        RoomResponseDTO room = roomService.getRoomById(id);
        return ResponseEntity.ok(room);
    }

    @PostMapping("/create-room")
    public ResponseEntity<RoomResponseDTO> createRoom(@RequestBody @Valid RoomCreateDTO dto) {
        RoomResponseDTO room = roomService.createRoom(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(room);
    }

    @PostMapping("/update-room/{id}")
    public ResponseEntity<RoomResponseDTO> updateRoom(@PathVariable Long id, @RequestBody @Valid RoomUpdateDTO dto) {
        RoomResponseDTO updatedRoom = roomService.updateRoom(id, dto);
        return ResponseEntity.ok(updatedRoom);
    }

    @PostMapping("/delete-rooms")
    public ResponseEntity<Void> deleteRooms(@RequestBody List<Long> ids) {
        roomService.deleteRooms(ids);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
