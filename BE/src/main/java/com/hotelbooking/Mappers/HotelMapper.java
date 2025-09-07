package com.hotelbooking.Mappers;

import com.hotelbooking.DTO.HotelDTO.HotelCreateDTO;
import com.hotelbooking.DTO.HotelDTO.HotelResponseDTO;
import com.hotelbooking.DTO.HotelDTO.HotelUpdateDTO;
import com.hotelbooking.Entities.HotelEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface HotelMapper {

    // Response
    @Mapping(source = "user.id",    target = "userId")
    HotelResponseDTO toResponseDTO(HotelEntity entity);

    // Create
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    HotelEntity toEntity(HotelCreateDTO dto);

    // Update: cập nhật vào entity sẵn có để giữ id/quan hệ
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(HotelUpdateDTO dto, @MappingTarget HotelEntity entity);
}
