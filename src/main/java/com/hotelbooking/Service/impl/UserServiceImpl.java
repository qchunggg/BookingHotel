package com.hotelbooking.Service.impl;

import com.hotelbooking.DTO.UserDTO.*;
import com.hotelbooking.Entities.UserEntity;
import com.hotelbooking.Enums.UserRole;
import com.hotelbooking.Mappers.UserMapper;
import com.hotelbooking.Repositories.UserRepository;
import com.hotelbooking.Service.UserService;
import com.hotelbooking.Utils.Utils;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl  implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Page<UserResponseDTO> search(UserFilterDTO filter) {
        Sort sort = Utils.generatedSort(filter.getSort());

        int page  = (filter.getPage()  == null || filter.getPage()  < 0) ? 0  : filter.getPage();
        int limit = (filter.getLimit() == null || filter.getLimit() <= 0) ? 10 : filter.getLimit();
        Pageable pageable = PageRequest.of(page, limit, sort);

        Specification<UserEntity> spec = getSearchSpecification(filter);

        return userRepository.findAll(spec, pageable)
                .map(userMapper::toResponseDTO);
    }

    @Override
    public List<UserResponseDTO> getAllUser() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDTO getUserById(Long id) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("Không thể tìm user với id: " + id));

        return userMapper.toResponseDTO(entity);
    }

    @Override
    @Transactional
    public UserResponseDTO createUser(UserCreateDTO dto) {
        String userNameNorm = normalize(dto.getUserName());
        String emailNorm    = normalize(dto.getEmail());
        String phoneNorm    = normalizePhone(dto.getPhone());

        Boolean dup = userRepository.existsDuplicate(userNameNorm, emailNorm, phoneNorm, null);
        if (dup) {
            throw new IllegalArgumentException("Người dùng đã tồn tại!");
        }

        UserEntity entity = userMapper.toEntity(dto);
        entity.setRole(UserRole.USER);
        entity.setPassword(passwordEncoder.encode(dto.getPassword()));

        UserEntity saved = userRepository.save(entity);
        return userMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public UserResponseDTO updateUser(UserUpdateDTO dto) {
        UserEntity entity = userRepository.findById(dto.getId())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy user với id: " + dto.getId()));

        String userNameNorm = normalize(dto.getUserName());
        String emailNorm    = normalize(dto.getEmail());
        String phoneNorm    = normalizePhone(dto.getPhone());

        Boolean dup = userRepository.existsDuplicate(userNameNorm, emailNorm, phoneNorm, dto.getId());
        if (dup) {
            throw new IllegalArgumentException("Người dùng đã tồn tại!");
        }

        userMapper.updateEntityFromDto(dto, entity);
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        UserEntity saved = userRepository.save(entity);
        return userMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public void deleteUser(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        userRepository.deleteAllByIdInBatch(ids);
    }

    @Override
    @Transactional
    public void changePassword(Long id, ChangePasswordDTO dto) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("Không tìm thấy user với id: " + id));

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu cũ không chính xác");
        }

        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu mới phải khác mật khẩu cũ");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));

    }

    private Specification<UserEntity> getSearchSpecification(UserFilterDTO f) {
        return (root, query, cb) -> {

            List<Predicate> ps = new ArrayList<>();

            /* -- 3.1. fullName LIKE -- */
            String fullName = normalize(f.getFullName());
            if (!fullName.isEmpty()) {
                ps.add(cb.like(cb.lower(root.get("fullName")), "%" + fullName + "%"));
            }

            /* -- 3.2. phone LIKE/equals -- */
            String phone = normalize(f.getPhone());
            if (!phone.isEmpty() && phone != null) {
                ps.add(cb.like(root.get("phone"), "%" + phone + "%"));
            }

            /* -- 3.3. role EQUAL -- */
            if (f.getRole() != null) {
                ps.add(cb.equal(root.get("role"), f.getRole()));
            }

            /* -- 3.4. Kết hợp predicate -- */
            return ps.isEmpty() ? cb.conjunction() : cb.and(ps.toArray(new Predicate[0]));
        };
    }

    private String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    private String normalizePhone(String s) {
        return s == null ? null : s.replaceAll("\\D", "");
    }
}
