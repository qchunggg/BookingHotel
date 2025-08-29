package com.hotelbooking.Mappers;

import com.hotelbooking.DTO.UserDTO.UserCreateDTO;
import com.hotelbooking.DTO.UserDTO.UserUpdateDTO;
import com.hotelbooking.Entities.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    // Mapping cho tạo mới
    UserCreateDTO toCreateDTO(UserEntity entity);
    UserEntity toEntity(UserCreateDTO dto);

    // Mapping cho cập nhật
    UserUpdateDTO toUpdateDTO(UserEntity entity);
    UserEntity toEntity(UserUpdateDTO dto);
}
