package com.hotelbooking.Service.impl;

import com.hotelbooking.DTO.ManagerResquestDTO.ManagerRequestFilterDTO;
import com.hotelbooking.DTO.ManagerResquestDTO.ManagerRequestResponseDTO;
import com.hotelbooking.Entities.ManagerRequestEntity;
import com.hotelbooking.Entities.UserEntity;
import com.hotelbooking.Enums.RequestStatus;
import com.hotelbooking.Enums.UserRole;
import com.hotelbooking.Mappers.ManagerRequestMapper;
import com.hotelbooking.Repositories.ManagerRequestRepository;
import com.hotelbooking.Repositories.UserRepository;
import com.hotelbooking.Service.ManagerRequestService;
import com.hotelbooking.Utils.Utils;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ManagerRequesServiceImpl implements ManagerRequestService {

    private final ManagerRequestRepository managerRequestRepository;
    private final UserRepository userRepository;
    private final ManagerRequestMapper managerRequestMapper;
    private final EmailService emailService;

    @Override
    @Transactional
    public ManagerRequestResponseDTO createRequest(Long userId) {

        /* 1. Kiểm tra user tồn tại */
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Không tồn tại người dùng với userId: " + userId));

        /* 2. Đã là MANAGER hoặc ADMIN rồi thì không cần yêu cầu */
        if (user.getRole() == UserRole.MANAGER || user.getRole() == UserRole.ADMIN) {
            throw new IllegalStateException("Tài khoản đã có quyền quản lý");
        }

        /* 3. Chặn trùng: đã có yêu cầu PENDING chưa xử lý */
        boolean dup = managerRequestRepository.existsByUserIdAndStatus(userId, RequestStatus.PENDING);
        if (dup) {
            throw new IllegalStateException("Bạn đã gửi yêu cầu và đang chờ phê duyệt");
        }

        if (managerRequestRepository.existsByUserIdAndStatus(userId, RequestStatus.APPROVED)) {
            throw new IllegalStateException("Yêu cầu đã được phê duyệt trước đó");
        }

        /* 4. Tạo bản ghi mới status = PENDING */
        ManagerRequestEntity entity = new ManagerRequestEntity();
        entity.setUser(user);
        entity.setStatus(RequestStatus.PENDING);
        entity = managerRequestRepository.save(entity);

        /* 5. Trả DTO cho FE */
        return managerRequestMapper.toDto(entity);
    }

    @Override
    public Page<ManagerRequestResponseDTO> searchRequests(ManagerRequestFilterDTO filter, Pageable pageable) {
        Sort sort = Utils.generatedSort(filter.getSort());

        if (pageable == null) {
            int page  = filter.getPage()  == null || filter.getPage()  < 0  ? 0  : filter.getPage();
            int limit = filter.getLimit() == null || filter.getLimit() <= 0 ? 10 : filter.getLimit();
            pageable  = PageRequest.of(page, limit, sort);
        }

        /* 3. Specification */
        Specification<ManagerRequestEntity> spec = getSearchSpecification(filter);

        /* 4. Query & map */
        return managerRequestRepository.findAll(spec, pageable)
                .map(managerRequestMapper::toDto);
    }

    @Override
    @Transactional
    public List<ManagerRequestResponseDTO> approve(List<Long> requestIds) {

        if (requestIds == null || requestIds.isEmpty()) {
            return List.of();
        }

        /* 1. Lấy toàn bộ bản ghi theo ID */
        List<ManagerRequestEntity> requests = managerRequestRepository.findAllById(requestIds);

        /* 2. Kiểm tra thiếu id nào */
        if (requests.size() != requestIds.size()) {
            throw new IllegalArgumentException("Tồn tại id không hợp lệ hoặc đã bị xoá");
        }

        /* 3. Xử lý từng yêu cầu */
        for (ManagerRequestEntity req : requests) {

            if (req.getStatus() != RequestStatus.PENDING) {
                throw new IllegalStateException(
                        "Yêu cầu id=" + req.getId() + " không ở trạng thái PENDING");
            }

            /* Nâng quyền user nếu cần */
            UserEntity user = req.getUser();
            if (user.getRole() == UserRole.USER) {
                user.setRole(UserRole.MANAGER);
                userRepository.save(user);
            }

            req.setStatus(RequestStatus.APPROVED);
        }

        /* 4. Lưu hàng loạt */
        managerRequestRepository.saveAll(requests);

        for (ManagerRequestEntity req : requests) {
            emailService.sendApprovedMail(req.getUser());   // gửi song song
        }

        /* 5. Trả danh sách DTO */
        return requests.stream()
                .map(managerRequestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ManagerRequestResponseDTO reject(Long requestId, String note) {
        ManagerRequestEntity req = managerRequestRepository.findById(requestId)
                .orElseThrow(() ->
                        new NoSuchElementException("Không tìm thấy yêu cầu id = " + requestId));

        /* 2. Kiểm tra trạng thái */
        if (req.getStatus() == RequestStatus.APPROVED) {
            throw new IllegalStateException("Yêu cầu đã được phê duyệt, không thể từ chối");
        }
        if (req.getStatus() == RequestStatus.REJECTED) {
            throw new IllegalStateException("Yêu cầu này đã bị từ chối trước đó");
        }
        // Lúc này chắc chắn đang PENDING

        /* 3. Cập nhật trạng thái */
        req.setStatus(RequestStatus.REJECTED);
        req.setNote(note);
        managerRequestRepository.save(req);

        /* 4. (Tuỳ chọn) gửi email thông báo từ chối cho user */
        emailService.sendRejectedMail(req.getUser(), req.getNote());

        /* 5. Trả DTO */
        return managerRequestMapper.toDto(req);
    }

    private Specification<ManagerRequestEntity> getSearchSpecification(ManagerRequestFilterDTO f) {
        return (root, query, cb) -> {

            List<Predicate> ps = new ArrayList<>();

            /* ---- 1. status exact ---- */
            if (f.getStatus() != null) {
                ps.add(cb.equal(root.get("status"), f.getStatus()));
            }

            /* ---- 2. keyword trên username OR email ---- */
            String keyword = normalize(f.getKeyword());
            if (!keyword.isEmpty()) {
                String like = "%" + keyword + "%";
                Join<ManagerRequestEntity, UserEntity> u = root.join("user", JoinType.INNER);

                Predicate byUsername = cb.like(cb.lower(u.get("userName")), like);
                Predicate byEmail    = cb.like(cb.lower(u.get("email")),    like);

                ps.add(cb.or(byUsername, byEmail));

                /* ưu tiên username match > email match */
                query.orderBy(
                        cb.desc(cb.selectCase().when(byUsername, 1).otherwise(0)),
                        cb.desc(cb.selectCase().when(byEmail,    1).otherwise(0))
                );
            }

            /* ---- 3. Khoảng ngày tạo ---- */
            if (f.getFromDate() != null) {
                ps.add(cb.greaterThanOrEqualTo(root.get("createdAt"),
                        f.getFromDate().atStartOfDay()));
            }
            if (f.getToDate() != null) {
                ps.add(cb.lessThanOrEqualTo(root.get("createdAt"),
                        f.getToDate().atTime(23, 59, 59)));
            }
            if (f.getFromDate() != null && f.getToDate() != null
                    && f.getFromDate().isAfter(f.getToDate())) {
                ps.add(cb.disjunction());          // vô hiệu hoá kết quả
            }

            /* ---- 4. Kết hợp ---- */
            return ps.isEmpty() ? cb.conjunction()
                    : cb.and(ps.toArray(new Predicate[0]));
        };
    }

    private String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }
}
