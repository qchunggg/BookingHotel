package com.hotelbooking.Mappers;

import com.hotelbooking.DTO.HotelDTO.HotelCreateDTO;
import com.hotelbooking.DTO.HotelDTO.HotelResponseDTO;
import com.hotelbooking.DTO.HotelDTO.HotelUpdateDTO;
import com.hotelbooking.Entities.HotelEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface HotelMapper {

    // Response
    @Mapping(source = "hotelImage", target = "hotelImage")
    HotelResponseDTO toResponseDTO(HotelEntity entity);

    // Create
    @Mapping(target = "id", ignore = true)
    HotelEntity toEntity(HotelCreateDTO dto);

    // Update: cập nhật vào entity sẵn có để giữ id/quan hệ
    void updateEntityFromDto(HotelUpdateDTO dto, @MappingTarget HotelEntity entity);
}
