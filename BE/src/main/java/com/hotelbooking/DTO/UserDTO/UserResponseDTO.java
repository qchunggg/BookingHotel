package com.hotelbooking.DTO.UserDTO;

import com.hotelbooking.Enums.UserRole;
import lombok.Data;

@Data
public class UserResponseDTO {

    private Long id;

    private String userName;

    private String fullName;

    private String email;

    private String phone;

    private UserRole role;
}
