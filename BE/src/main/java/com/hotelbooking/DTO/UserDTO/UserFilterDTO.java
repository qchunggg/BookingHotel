package com.hotelbooking.DTO.UserDTO;

import com.hotelbooking.DTO.PageFilterDTO;
import com.hotelbooking.Enums.UserRole;
import lombok.Data;

@Data
public class UserFilterDTO extends PageFilterDTO {

    private String fullName;

    private String phone;

    private UserRole role;
}
