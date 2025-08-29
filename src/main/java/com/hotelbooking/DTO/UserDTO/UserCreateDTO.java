package com.hotelbooking.DTO.UserDTO;

import com.hotelbooking.Enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserCreateDTO {

    @NotBlank(message = "Username không được để trống")
    @Size(max = 50, message = "Username tối đa 50 ký tự")
    private String userName;

    @NotBlank(message = "Password không được để trống")
    @Size(min = 6, message = "Password tối thiểu 6 ký tự")
    private String password;

    @NotBlank(message = "Full name không được để trống")
    @Size(max = 100, message = "Full name tối đa 100 ký tự")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @Size(max = 10, message = "Số điện thoại tối đa 10 ký tự")
    private String phone;

    @NotNull(message = "Role không được để trống")
    private UserRole role; // USER / ADMIN / MANAGER
}
