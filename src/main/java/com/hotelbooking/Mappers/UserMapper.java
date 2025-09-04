package com.hotelbooking.Mappers;

import com.hotelbooking.DTO.UserDTO.UserCreateDTO;
import com.hotelbooking.DTO.UserDTO.UserResponseDTO;
import com.hotelbooking.DTO.UserDTO.UserUpdateDTO;
import com.hotelbooking.Entities.UserEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    /* Response – KHÔNG trả password */
    UserResponseDTO toResponseDTO(UserEntity entity);

    /* Create */
    @Mapping(target = "id",   ignore = true)
    @Mapping(target = "role", ignore = true)
    UserEntity toEntity(UserCreateDTO dto);

    /* Update (copy không-null) */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(UserUpdateDTO dto, @MappingTarget UserEntity entity);
}
