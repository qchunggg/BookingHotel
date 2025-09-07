package com.hotelbooking.Config;

import com.hotelbooking.Entities.HotelEntity;
import com.hotelbooking.Repositories.HotelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

@Component
@RequiredArgsConstructor
public class PayOSCredentialProvider {
    private final HotelRepository hotelRepo;

    public PayOSCredentials ofHotel(Long hotelId) {
        HotelEntity h = hotelRepo.findById(hotelId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy hotel"));
        return new PayOSCredentials(
                h.getPayosClientId(),
                h.getPayosApiKey(),
                h.getPayosChecksumKey());
    }
}

