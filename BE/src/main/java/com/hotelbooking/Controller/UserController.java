package com.hotelbooking.Controller;

import com.hotelbooking.DTO.UserDTO.*;
import com.hotelbooking.Service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> search(@RequestBody UserFilterDTO filter) {
        return ResponseEntity.ok(userService.search(filter));
    }

    @PostMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDTO>> getAll() {
        return ResponseEntity.ok(userService.getAllUser());
    }

    @PostMapping("/update-user")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER', 'USER')")
    public ResponseEntity<UserResponseDTO> update(@Valid @RequestBody UserUpdateDTO dto) {
        return ResponseEntity.ok(userService.updateUser(dto));
    }

    @PostMapping("/delete-users")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER', 'USER')")
    public ResponseEntity<Void> delete(@RequestBody List<Long> ids) {
        userService.deleteUser(ids);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/change-password/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER', 'USER')")
    public ResponseEntity<Void> changePassword(@PathVariable Long id, @Valid @RequestBody ChangePasswordDTO dto) {
        userService.changePassword(id, dto);
        return ResponseEntity.ok().build();
    }
}
