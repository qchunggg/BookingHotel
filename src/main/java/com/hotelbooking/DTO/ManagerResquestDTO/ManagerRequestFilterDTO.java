package com.hotelbooking.DTO.ManagerResquestDTO;

import com.hotelbooking.DTO.PageFilterDTO;
import com.hotelbooking.Enums.RequestStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ManagerRequestFilterDTO extends PageFilterDTO {
    private RequestStatus status;  // mặc định

    private String keyword;         // username hoặc email

    private LocalDate fromDate;     // lọc >=
    private LocalDate toDate;
}
