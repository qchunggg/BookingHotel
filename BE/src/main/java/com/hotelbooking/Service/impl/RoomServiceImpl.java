package com.hotelbooking.Service.impl;

import com.hotelbooking.DTO.RoomDTO.RoomCreateDTO;
import com.hotelbooking.DTO.RoomDTO.RoomFilterDTO;
import com.hotelbooking.DTO.RoomDTO.RoomResponseDTO;
import com.hotelbooking.DTO.RoomDTO.RoomUpdateDTO;
import com.hotelbooking.Entities.BookingEntity;
import com.hotelbooking.Entities.HotelEntity;
import com.hotelbooking.Entities.RoomEntity;
import com.hotelbooking.Enums.BookingStatus;
import com.hotelbooking.Enums.RoomStatus;
import com.hotelbooking.Mappers.RoomMapper;
import com.hotelbooking.Repositories.BookingRepository;
import com.hotelbooking.Repositories.HotelRepository;
import com.hotelbooking.Repositories.RoomRepository;
import com.hotelbooking.Service.RoomService;
import com.hotelbooking.Utils.Utils;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomMapper roomMapper;

    @Override
    public Page<RoomResponseDTO> search(RoomFilterDTO filter) {
        Sort sort = Utils.generatedSort(filter.getSort());
        int page  = (filter.getPage()  == null || filter.getPage()  < 0) ? 0  : filter.getPage();
        int limit = (filter.getLimit() == null || filter.getLimit() <= 0) ? 10 : filter.getLimit();
        Pageable pageable = PageRequest.of(page, limit, sort);

        // Nếu hệ thống đa khách sạn, bắt buộc hotelId
        if (filter.getHotelId() == null) {
            throw new IllegalArgumentException("hotelId là bắt buộc khi tìm phòng");
        }

        Specification<RoomEntity> spec = getSearchSpecification(filter);

        return roomRepository.findAll(spec, pageable)
                .map(roomMapper::toResponseDTO);
    }

    @Override
    public List<RoomResponseDTO> getAllRoomsByHotel(Long hotelId) {
        if (!hotelRepository.existsById(hotelId)) {
            throw new NoSuchElementException("Không tìm thấy khách sạn với id: " + hotelId);
        }

        return roomRepository.findByHotelId(hotelId)
                .stream()
                .map(roomMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public RoomResponseDTO getRoomById(Long id) {
        RoomEntity entity = roomRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy phòng với id: " + id));
        return roomMapper.toResponseDTO(entity);
    }

    @Override
    @Transactional
    public RoomResponseDTO createRoom(RoomCreateDTO dto) {
        // đảm bảo khách sạn tồn tại
        HotelEntity hotel = hotelRepository.findById(dto.getHotelId())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy khách sạn với id: " + dto.getHotelId()));

        String roomNumber = normalize(dto.getRoomNumber());
        // check trùng: cùng hotel + roomNumber (không phân biệt hoa/thường, trim)
        boolean dup = roomRepository.existsDuplicate(hotel.getId(), roomNumber, null);
        if (dup) {
            throw new IllegalArgumentException("Phòng đã tồn tại trong khách sạn!");
        }

        // 3. Map DTO -> Entity
        RoomEntity entity = roomMapper.toEntity(dto);
        entity.setHotel(hotel);
        entity.setRoomNumber(roomNumber);

        // 4. Nếu trạng thái phòng chưa được gửi thì default là AVAILABLE
        if (entity.getRoomStatus() == null) {
            entity.setRoomStatus(RoomStatus.AVAILABLE);
        }

        RoomEntity saved = roomRepository.save(entity);
        return roomMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public RoomResponseDTO updateRoom(RoomUpdateDTO dto) {
        RoomEntity entity = roomRepository.findById(dto.getId())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy phòng với id: " + dto.getId()));

        // Nếu đổi số phòng thì check trùng trong cùng hotel
        if (dto.getRoomNumber() != null) {
            String newNumber = normalize(dto.getRoomNumber());
            if (!newNumber.equalsIgnoreCase(entity.getRoomNumber())
                    && roomRepository.existsDuplicate(entity.getHotel().getId(), newNumber, dto.getId())) {
                throw new IllegalArgumentException("Phòng đã tồn tại trong khách sạn!");
            }
            entity.setRoomNumber(newNumber);
        }

        // Map các field còn lại, bỏ qua null
        roomMapper.updateEntityFromDto(dto, entity);

        // Nếu trạng thái null thì giữ nguyên hoặc default lại
        if (entity.getRoomStatus() == null) {
            entity.setRoomStatus(RoomStatus.AVAILABLE);
        }

        RoomEntity saved = roomRepository.save(entity);
        return roomMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public void deleteRooms(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;

        // Không cho xoá nếu còn booking tương lai (CONFIRMED hoặc CHECKED_IN)
        LocalDateTime today = LocalDateTime.now();
        boolean hasFutureBookings = bookingRepository.existsByRoomIdsAndFutureActive(ids, today);
        if (hasFutureBookings) {
            throw new IllegalStateException("Không thể xoá phòng vì còn đặt phòng trong tương lai.");
        }

        roomRepository.deleteAllByIdInBatch(ids);
    }

    private Specification<RoomEntity> getSearchSpecification(RoomFilterDTO filter) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();

            // --- hotelId (nếu có) ---
            if (filter.getHotelId() != null) {
                ps.add(cb.equal(root.get("hotel").get("id"), filter.getHotelId()));
            }

            if (filter.getName() != null && !filter.getName().trim().isEmpty()) {
                String kw = "%" + filter.getName().trim().toLowerCase() + "%";
                Path<String> hotelNamePath = root.join("hotel", JoinType.INNER).get("name");
                ps.add(cb.like(cb.lower(hotelNamePath), kw));
            }

            // --- roomStatus ---
            if (filter.getRoomStatus() != null) {
                ps.add(cb.equal(root.get("roomStatus"), filter.getRoomStatus()));
            }

            // --- roomType ---
            if (filter.getRoomType() != null) {
                ps.add(cb.equal(root.get("roomType"), filter.getRoomType()));
            }

            // --- khoảng giá theo pricePerDay ---
            if (filter.getMinPricePerDay() != null) {
                ps.add(cb.greaterThanOrEqualTo(root.get("pricePerDay"), filter.getMinPricePerDay()));
            }
            if (filter.getMaxPricePerDay() != null) {
                ps.add(cb.lessThanOrEqualTo(root.get("pricePerDay"), filter.getMaxPricePerDay()));
            }
            if (filter.getMinPricePerDay() != null && filter.getMaxPricePerDay() != null
                    && filter.getMinPricePerDay() > filter.getMaxPricePerDay()) {
                // min > max -> không match
                ps.add(cb.disjunction());
            }

            // --- lọc phòng trống theo khoảng ngày (overlap) ---
            if (filter.getCheckIn() != null && filter.getCheckOut() != null) {
                if (filter.getCheckIn().isAfter(filter.getCheckOut())) {
                    ps.add(cb.disjunction()); // khoảng ngày sai -> không match
                } else {
                    Subquery<Long> sub = query.subquery(Long.class);
                    Root<BookingEntity> b = sub.from(BookingEntity.class);
                    sub.select(b.get("id"))
                            .where(
                                    cb.equal(b.get("room").get("id"), root.get("id")),
                                    // CHỈ chặn các booking giữ chỗ thật sự
                                    cb.or(
                                            cb.equal(b.get("bookingStatus"), BookingStatus.CONFIRMED),
                                            cb.equal(b.get("bookingStatus"), BookingStatus.CHECKED_IN)
                                    ),
                                    // overlap: (b.checkIn < filter.checkOut) AND (b.checkOut > filter.checkIn)
                                    cb.lessThan(b.get("checkInDate"), filter.getCheckOut()),
                                    cb.greaterThan(b.get("checkOutDate"), filter.getCheckIn())
                            );
                    ps.add(cb.not(cb.exists(sub)));
                }
            }

            return ps.isEmpty() ? cb.conjunction() : cb.and(ps.toArray(new Predicate[0]));
        };
    }

    private String normalize(String s) {
        if (s == null) return "";
        return s.trim().replaceAll("\\s+", " ");
    }
}
