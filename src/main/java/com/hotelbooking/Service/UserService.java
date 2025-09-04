package com.hotelbooking.Service;

import com.hotelbooking.DTO.UserDTO.*;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UserService {

    Page<UserResponseDTO> search(UserFilterDTO filter);

    List<UserResponseDTO> getAllUser();

    UserResponseDTO getUserById(Long id);

    UserResponseDTO createUser(UserCreateDTO dto);

    UserResponseDTO updateUser(UserUpdateDTO dto);

    void deleteUser(List<Long> ids);

    void changePassword(Long id, ChangePasswordDTO dto);
}
