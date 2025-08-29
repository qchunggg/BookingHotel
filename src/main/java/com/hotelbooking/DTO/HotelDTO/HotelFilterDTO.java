package com.hotelbooking.DTO.HotelDTO;

import com.hotelbooking.DTO.PageFilterDTO;
import lombok.Data;

@Data
public class HotelFilterDTO extends PageFilterDTO {

    private String keyword;       // tìm theo tên hoặc thành phố (LIKE %keyword%)

    private String city;          // lọc theo thành phố

    private Double minRating;     // lọc rating >=

    private Double maxRating;     // lọc rating <=
}
