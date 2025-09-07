package com.hotelbooking.Service;

import com.hotelbooking.DTO.ManagerResquestDTO.ManagerRequestFilterDTO;
import com.hotelbooking.DTO.ManagerResquestDTO.ManagerRequestResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ManagerRequestService {
    /**
     * Người dùng tạo yêu cầu “trở thành MANAGER”.
     * @param userId  id của người gửi (lấy từ principal)
     * @return        DTO trạng thái PENDING vừa tạo
     * @throws IllegalStateException  nếu đã có yêu cầu PENDING
     */
    ManagerRequestResponseDTO createRequest(Long userId);


    /* ===== ADMIN ===== */

    /**
     * Tra cứu danh sách yêu cầu (phân trang + filter).
     * Cho phép lọc theo status, keyword (username / email), khoảng ngày, v.v.
     */
    Page<ManagerRequestResponseDTO> searchRequests(ManagerRequestFilterDTO filter, Pageable pageable);

    /**
     * ADMIN phê duyệt -> đổi status = APPROVED
     * đồng thời nâng quyền user thành MANAGER.
     */
    List<ManagerRequestResponseDTO> approve(List<Long> requestIds);

    /**
     * ADMIN từ chối -> status = REJECTED + ghi chú.
     */
    ManagerRequestResponseDTO reject(Long requestId, String note);
}
