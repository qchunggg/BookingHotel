package com.hotelbooking.Mappers;

import com.hotelbooking.DTO.ManagerResquestDTO.ManagerRequestResponseDTO;
import com.hotelbooking.Entities.ManagerRequestEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ManagerRequestMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.userName", target = "userName")
    ManagerRequestResponseDTO toDto(ManagerRequestEntity e);
}
