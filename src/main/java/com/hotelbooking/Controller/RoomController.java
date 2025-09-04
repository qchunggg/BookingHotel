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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @PostMapping("/create-room")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<RoomResponseDTO> createRoom(@RequestBody @Valid RoomCreateDTO dto) {
        RoomResponseDTO room = roomService.createRoom(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(room);
    }

    @PostMapping("/update-room")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<RoomResponseDTO> updateRoom(@RequestBody @Valid RoomUpdateDTO dto) {
        RoomResponseDTO updatedRoom = roomService.updateRoom(dto);
        return ResponseEntity.ok(updatedRoom);
    }

    @PostMapping("/delete-rooms")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteRooms(@RequestBody List<Long> ids) {
        roomService.deleteRooms(ids);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
