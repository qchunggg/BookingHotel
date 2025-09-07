package com.hotelbooking.Controller;

import com.hotelbooking.DTO.ManagerResquestDTO.IdListWithNote;
import com.hotelbooking.DTO.ManagerResquestDTO.ManagerRequestFilterDTO;
import com.hotelbooking.DTO.ManagerResquestDTO.ManagerRequestResponseDTO;
import com.hotelbooking.Security.CustomUserDetails;
import com.hotelbooking.Service.ManagerRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/manager-requests")
@RequiredArgsConstructor
public class ManagerRequestController {

    private final ManagerRequestService managerRequestService;

    @PostMapping("/create-request")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ManagerRequestResponseDTO> create(@AuthenticationPrincipal CustomUserDetails principal) {
        ManagerRequestResponseDTO dto = managerRequestService.createRequest(principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ManagerRequestResponseDTO>> search(ManagerRequestFilterDTO filter, Pageable pageable) {

        Page<ManagerRequestResponseDTO> result = managerRequestService.searchRequests(filter, pageable);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ManagerRequestResponseDTO>> approve(@RequestBody List<Long> ids) {

        List<ManagerRequestResponseDTO> dtos = managerRequestService.approve(ids);
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ManagerRequestResponseDTO> reject(@RequestBody IdListWithNote body) {

        ManagerRequestResponseDTO dtos = managerRequestService.reject(body.getId(), body.getNote());
        return ResponseEntity.ok(dtos);
    }
}
