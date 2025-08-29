package com.hotelbooking.Service;

import com.hotelbooking.DTO.HotelDTO.HotelCreateDTO;
import com.hotelbooking.DTO.HotelDTO.HotelFilterDTO;
import com.hotelbooking.DTO.HotelDTO.HotelResponseDTO;
import com.hotelbooking.DTO.HotelDTO.HotelUpdateDTO;
import org.springframework.data.domain.Page;
import java.util.List;

public interface HotelService {

    Page<HotelResponseDTO> search(HotelFilterDTO filter);

    List<HotelResponseDTO> getAllHotels();

    HotelResponseDTO  getHotelById(Long id);

    HotelResponseDTO createHotel(HotelCreateDTO dto);

    HotelResponseDTO updateHotel(Long id, HotelUpdateDTO dto);

    void deleteHotels(List<Long> ids);
}
