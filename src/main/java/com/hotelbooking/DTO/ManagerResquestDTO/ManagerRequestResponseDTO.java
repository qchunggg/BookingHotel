package com.hotelbooking.DTO.ManagerResquestDTO;

import com.hotelbooking.Enums.RequestStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ManagerRequestResponseDTO {

    private Long id;
    private Long userId;
    private String userName;
    private RequestStatus status;
    private String note;
    private LocalDateTime createdAt;
}
