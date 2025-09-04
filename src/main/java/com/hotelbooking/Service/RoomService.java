package com.hotelbooking.Service;

import com.hotelbooking.DTO.RoomDTO.RoomCreateDTO;
import com.hotelbooking.DTO.RoomDTO.RoomFilterDTO;
import com.hotelbooking.DTO.RoomDTO.RoomResponseDTO;
import com.hotelbooking.DTO.RoomDTO.RoomUpdateDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface RoomService {

    Page<RoomResponseDTO> search(RoomFilterDTO filter);

    List<RoomResponseDTO> getAllRoomsByHotel(Long hotelId);

    RoomResponseDTO getRoomById(Long id);

    RoomResponseDTO createRoom(RoomCreateDTO dto);

    RoomResponseDTO updateRoom(RoomUpdateDTO dto);

    void deleteRooms(List<Long> ids);

}
