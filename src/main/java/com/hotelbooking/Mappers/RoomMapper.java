package com.hotelbooking.Mappers;

import com.hotelbooking.DTO.RoomDTO.RoomCreateDTO;
import com.hotelbooking.DTO.RoomDTO.RoomResponseDTO;
import com.hotelbooking.DTO.RoomDTO.RoomUpdateDTO;
import com.hotelbooking.Entities.RoomEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface RoomMapper {

    @Mapping(source = "hotel.id", target = "hotelId") // ánh xạ hotel.id -> hotelId
    @Mapping(source = "hotel.name", target = "name")
    RoomResponseDTO toResponseDTO(RoomEntity entity);

    // Create
    @Mapping(target = "id", ignore = true)
    RoomEntity toEntity(RoomCreateDTO dto);

    // Update: cập nhật vào entity sẵn có để giữ id/quan hệ
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(RoomUpdateDTO dto, @MappingTarget RoomEntity entity);
}
