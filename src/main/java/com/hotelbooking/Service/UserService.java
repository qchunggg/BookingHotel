package com.hotelbooking.Service;

import com.hotelbooking.DTO.UserDTO.UserCreateDTO;
import com.hotelbooking.DTO.UserDTO.UserFilterDTO;
import com.hotelbooking.DTO.UserDTO.UserResponseDTO;
import com.hotelbooking.DTO.UserDTO.UserUpdateDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UserService {

    Page<UserResponseDTO> search(UserFilterDTO filter);

    List<UserResponseDTO> getAllUser();

    UserResponseDTO getUserById(Long id);

    UserResponseDTO createUser(UserCreateDTO dto);

    UserResponseDTO updateUser(UserUpdateDTO dto);

    void deleteUser(List<Long> ids);
}
