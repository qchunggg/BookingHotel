package com.hotelbooking.Service;

import com.hotelbooking.DTO.HotelDTO.HotelCreateDTO;
import com.hotelbooking.DTO.HotelDTO.HotelFilterDTO;
import com.hotelbooking.DTO.HotelDTO.HotelResponseDTO;
import com.hotelbooking.DTO.HotelDTO.HotelUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface HotelService {

    Page<HotelResponseDTO> search(HotelFilterDTO filter);

    List<HotelResponseDTO> getAllHotels();

    HotelResponseDTO  getHotelById(Long id);

    Page<HotelResponseDTO> getHotelsByUserId(Long userId, Pageable pageable);

    HotelResponseDTO createHotel(HotelCreateDTO dto);

    HotelResponseDTO updateHotel(HotelUpdateDTO dto);

    void deleteHotels(List<Long> ids);
}
