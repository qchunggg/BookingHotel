package com.hotelbooking.Controller;

import com.hotelbooking.DTO.HotelDTO.HotelFilterDTO;
import com.hotelbooking.DTO.HotelDTO.HotelResponseDTO;
import com.hotelbooking.DTO.LoginDTO.LoginRequestDTO;
import com.hotelbooking.DTO.LoginDTO.LoginResponseDTO;
import com.hotelbooking.DTO.RoomDTO.RoomFilterDTO;
import com.hotelbooking.DTO.RoomDTO.RoomResponseDTO;
import com.hotelbooking.DTO.UserDTO.UserCreateDTO;
import com.hotelbooking.DTO.UserDTO.UserResponseDTO;
import com.hotelbooking.Security.CustomUserDetails;
import com.hotelbooking.Security.JWT.JwtTokenProvider;
import com.hotelbooking.Service.HotelService;
import com.hotelbooking.Service.RoomService;
import com.hotelbooking.Service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    private final HotelService hotelService;
    private final RoomService roomService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> create(@Valid @RequestBody UserCreateDTO dto) {
        UserResponseDTO created = userService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        /* 1. Spring Security xác thực user & mật khẩu BCrypt */
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUserName(), dto.getPassword())
        );

        /* 2. Lấy principal (CustomUserDetails) */
        CustomUserDetails principal = (CustomUserDetails) auth.getPrincipal();

        /* 3. Sinh JWT */
        String token = jwtTokenProvider.generateToken(principal.getUsername());

        /* 3. Tạo cookie – HttpOnly, Secure (nếu chạy HTTPS), SameSite=Lax */
        ResponseCookie cookie = ResponseCookie.from("JWT_TOKEN", token)
                .httpOnly(true)
                .secure(false)          // đặt true nếu deploy HTTPS
                .path("/")
                .maxAge(86400)
                .sameSite("Lax")        // tránh CSRF cơ bản; Strict nếu chỉ API thuần
                .build();

        UserResponseDTO userInfo = userService.getUserById(principal.getId());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new LoginResponseDTO(token, userInfo));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        ResponseCookie clearJwt = ResponseCookie.from("JWT_TOKEN", null)
                .httpOnly(true)
                .secure(false)          // đặt true nếu deploy HTTPS
                .path("/")
                .maxAge(0)
                .sameSite("Lax")        // tránh CSRF cơ bản; Strict nếu chỉ API thuần
                .build();
        /* 2. (Tuỳ chọn) xoá Authentication trong thread hiện tại */
        SecurityContextHolder.clearContext();

        /* 3. Trả về 200 hoặc 204 kèm header Set-Cookie */
        return ResponseEntity.noContent()               // 204
                .header(HttpHeaders.SET_COOKIE, clearJwt.toString())
                .build();
    }


    @PostMapping("/search-hotels")
    public ResponseEntity<Page<HotelResponseDTO>> searchAndFilterHotels(@RequestBody HotelFilterDTO filter) {
        Page<HotelResponseDTO> hotel = hotelService.search(filter);
        return ResponseEntity.ok(hotel);
    }

    @PostMapping("/detail-hotel/{id}")
    public ResponseEntity<HotelResponseDTO> getHotelById(@PathVariable Long id) {
        HotelResponseDTO detail = hotelService.getHotelById(id);
        return ResponseEntity.ok(detail);
    }

    @PostMapping("/search-rooms")
    public ResponseEntity<Page<RoomResponseDTO>> searchRooms(@RequestBody RoomFilterDTO filter) {
        Page<RoomResponseDTO> rooms = roomService.search(filter);
        return ResponseEntity.ok(rooms);
    }

    @PostMapping("/hotel/{hotelId}")
    public ResponseEntity<List<RoomResponseDTO>> getAllRoomsByHotel(@PathVariable Long hotelId) {
        List<RoomResponseDTO> rooms = roomService.getAllRoomsByHotel(hotelId);
        return ResponseEntity.ok(rooms);
    }

    @PostMapping("/detail-room/{id}")
    public ResponseEntity<RoomResponseDTO> getRoomById(@PathVariable Long id) {
        RoomResponseDTO room = roomService.getRoomById(id);
        return ResponseEntity.ok(room);
    }
}
