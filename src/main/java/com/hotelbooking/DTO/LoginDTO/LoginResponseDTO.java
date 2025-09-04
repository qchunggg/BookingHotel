package com.hotelbooking.DTO.LoginDTO;

import com.hotelbooking.DTO.UserDTO.UserResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDTO {
    private String token;

    private UserResponseDTO userInfo;
}
