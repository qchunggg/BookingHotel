package com.hotelbooking.Mappers;

import com.hotelbooking.Constants.Constants;
import com.hotelbooking.DTO.BookingDTO.BookingCreateDTO;
import com.hotelbooking.DTO.BookingDTO.BookingResponseDTO;
import com.hotelbooking.DTO.BookingDTO.BookingUpdateDTO;
import com.hotelbooking.Entities.BookingEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true) // set thủ công trong Service
    @Mapping(target = "room", ignore = true)
    @Mapping(target = "checkInDate", ignore = true)   // <- thêm dòng này
    @Mapping(target = "checkOutDate", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)    // do BE tính
    @Mapping(target = "bookingStatus", ignore = true)
    BookingEntity toEntity(BookingCreateDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true) // set ở Service sau khi load UserEntity
    @Mapping(target = "room", ignore = true)
    @Mapping(target = "checkInDate", ignore = true)   // nếu update dùng String -> BE parse
    @Mapping(target = "checkOutDate", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)    // không cho FE ghi đè
    @Mapping(target = "bookingStatus", ignore = true) // trạng thái do BE quản
    BookingEntity toEntity(BookingUpdateDTO dto);

    // Mapping cho phản hồi (Response)
    @Mapping(source = "user.id",       target = "userId")
    @Mapping(source = "user.fullName", target = "fullName")   // <-- thêm dòng này
    @Mapping(source = "room.id",       target = "roomId")
    @Mapping(source = "room.roomStatus", target = "roomStatus")
    @Mapping(source = "checkInDate",   target = "checkInDate", dateFormat = Constants.DateFormatType.JAVA_PATTERN)
    @Mapping(source = "checkOutDate",  target = "checkOutDate", dateFormat = Constants.DateFormatType.JAVA_PATTERN)
    BookingResponseDTO toResponseDTO(BookingEntity entity);
}
