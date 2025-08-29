package com.hotelbooking.DTO.UserDTO;

import com.hotelbooking.Enums.UserRole;
import lombok.Data;

@Data
public class UserResponseDTO {

    private Long id;

    private String username;

    private String password;

    private String fullName;

    private String email;

    private String phone;

    private UserRole role;
}
