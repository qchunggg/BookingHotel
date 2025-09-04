package com.hotelbooking.Service.impl;

import com.hotelbooking.Config.SchedulerConfig;
import com.hotelbooking.DTO.BookingDTO.BookingCreateDTO;
import com.hotelbooking.DTO.BookingDTO.BookingFilterDTO;
import com.hotelbooking.DTO.BookingDTO.BookingResponseDTO;
import com.hotelbooking.DTO.BookingDTO.BookingUpdateDTO;
import com.hotelbooking.Entities.BookingEntity;
import com.hotelbooking.Entities.RoomEntity;
import com.hotelbooking.Entities.UserEntity;
import com.hotelbooking.Enums.BookingStatus;
import com.hotelbooking.Enums.RoomStatus;
import com.hotelbooking.Mappers.BookingMapper;
import com.hotelbooking.Repositories.BookingRepository;
import com.hotelbooking.Repositories.RoomRepository;
import com.hotelbooking.Repositories.UserRepository;
import com.hotelbooking.Service.BookingService;
import com.hotelbooking.Utils.Utils;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private TaskScheduler taskScheduler;

    @Override
    public Page<BookingResponseDTO> search(BookingFilterDTO filter) {
        Sort sort = Utils.generatedSort(filter.getSort()); // ví dụ: "checkInDate,desc,id,desc"
        int page  = (filter.getPage()  == null || filter.getPage()  < 0) ? 0  : filter.getPage();
        int limit = (filter.getLimit() == null || filter.getLimit() <= 0) ? 10 : filter.getLimit();
        Pageable pageable = PageRequest.of(page, limit, sort);

        Specification<BookingEntity> spec = getSearchSpecification(filter);

        return bookingRepository.findAll(spec, pageable)
                .map(bookingMapper::toResponseDTO);
    }

    @Override
    public BookingResponseDTO getBookingById(Long id) {
        BookingEntity entity = bookingRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy đặt phòng với id: " + id));

        // Chuyển đổi BookingEntity thành BookingResponseDTO và trả về
        return bookingMapper.toResponseDTO(entity);
    }

    @Override
    public List<BookingResponseDTO> getAllBookings() {
        List<BookingEntity> bookings = bookingRepository.findAll();

        // Chuyển đổi tất cả các BookingEntity thành BookingResponseDTO
        return bookings.stream()
                .map(bookingMapper::toResponseDTO)  // Ánh xạ từ BookingEntity sang BookingResponseDTO
                .collect(Collectors.toList());  // Trả về danh sách BookingResponseDTO
    }

    @Override
    @Transactional
    public BookingResponseDTO createBooking(BookingCreateDTO dto) {
        // 0) Parse & validate thời gian
        LocalDateTime checkIn  = Utils.DateTimeUtils.parse(dto.getCheckInDate());
        LocalDateTime checkOut = Utils.DateTimeUtils.parse(dto.getCheckOutDate());
        Utils.DateTimeUtils.validateRange(checkIn, checkOut);

        // 1) Load user & room
        UserEntity user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy người dùng với id: " + dto.getUserId()));
        RoomEntity room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy phòng với id: " + dto.getRoomId()));

        // 2) Chặn phòng bảo trì
        if (room.getRoomStatus() == RoomStatus.OUT_OF_SERVICE) {
            throw new IllegalArgumentException("Phòng đang bảo trì, không thể đặt.");
        }

        // 3) Chống chồng lấn (dùng LocalDateTime)
        boolean overlap = bookingRepository.existsOverlapping(room.getId(), checkIn, checkOut, null);
        if (overlap) {
            throw new IllegalStateException("Phòng đã có đặt chỗ trong khoảng thời gian này.");
        }

        // 4) Map thô & gán khóa ngoại + thời gian
        BookingEntity entity = bookingMapper.toEntity(dto); // mapper đã ignore các field BE tính
        entity.setUser(user);
        entity.setRoom(room);
        entity.setCheckInDate(checkIn);
        entity.setCheckOutDate(checkOut);

        // 5) Snapshot giá & tính tiền (BE quyết định)
        long nights = ChronoUnit.DAYS.between(checkIn.toLocalDate(), checkOut.toLocalDate());
        if (nights <= 0) {
            throw new IllegalArgumentException("Thời gian lưu trú phải tối thiểu 1 đêm.");
        }

        Integer pricePerDay = room.getPricePerDay(); // snapshot tại thời điểm đặt
        if (pricePerDay == null) {
            throw new IllegalStateException("Phòng chưa cấu hình pricePerDay.");
        }

        int nightsInt = Math.toIntExact(nights);
        int total = Math.multiplyExact(nightsInt, pricePerDay);    // 30% đặt cọc (có thể đưa vào Constants cấu hình)
        int deposit = (total * 30 + 99) / 100;

        entity.setPricePerDay(pricePerDay);  // <-- NEW: lưu snapshot
        entity.setTotalPrice(total);
        entity.setDepositAmount(deposit);    // <-- NEW: lưu tiền cọc

        // 6) Trạng thái ban đầu
        entity.setBookingStatus(BookingStatus.PENDING);

        // 7) Lưu & trả DTO
        BookingEntity saved = bookingRepository.save(entity);
        return bookingMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public BookingResponseDTO updateBooking(BookingUpdateDTO dto) {
        BookingEntity existing = bookingRepository.findById(dto.getId())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy đặt phòng với id: " + dto.getId()));

        // Chặn đổi ngày khi đã ở/đã trả
        if (existing.getBookingStatus() == BookingStatus.CHECKED_IN ||
                existing.getBookingStatus() == BookingStatus.CHECKED_OUT) {
            throw new IllegalStateException("Không thể đổi ngày khi booking đã CHECKED_IN/CHECKED_OUT.");
        }

        // (Tuỳ) Không cho đổi trạng thái qua update (để API riêng)
        if (dto.getBookingStatus() != null) {
            throw new IllegalStateException("Không cập nhật bookingStatus qua update; dùng API nghiệp vụ riêng.");
        }

        // 1) Xác định phòng (nếu cho phép đổi phòng)
        RoomEntity room = existing.getRoom();
        if (dto.getRoomId() != null && !dto.getRoomId().equals(room.getId())) {
            RoomEntity newRoom = roomRepository.findById(dto.getRoomId())
                    .orElseThrow(() -> new NoSuchElementException("Không tìm thấy phòng với id: " + dto.getRoomId()));
            if (newRoom.getRoomStatus() == RoomStatus.OUT_OF_SERVICE) {
                throw new IllegalArgumentException("Phòng đang bảo trì, không thể chuyển.");
            }
            room = newRoom;
            existing.setRoom(newRoom);
        }

        // 2) Parse & validate thời gian (giống create)
        LocalDateTime newIn  = (dto.getCheckInDate()  != null)
                ? Utils.DateTimeUtils.parse(dto.getCheckInDate())
                : existing.getCheckInDate();

        LocalDateTime newOut = (dto.getCheckOutDate() != null)
                ? Utils.DateTimeUtils.parse(dto.getCheckOutDate())
                : existing.getCheckOutDate();

        Utils.DateTimeUtils.validateRange(newIn, newOut);

        // 3) Chống chồng lấn (giống create)
        boolean overlap = bookingRepository.existsOverlapping(room.getId(), newIn, newOut, existing.getId());
        if (overlap) {
            throw new IllegalStateException("Phòng đã có đặt chỗ trong khoảng thời gian này.");
        }

        // 4) Cập nhật trường (giống create)
        existing.setCheckInDate(newIn);
        existing.setCheckOutDate(newOut);

        if (dto.getTotalPrice() != null) {
            if (dto.getTotalPrice() <= 0) throw new IllegalArgumentException("Tổng giá phải lớn hơn 0.");
            existing.setTotalPrice(dto.getTotalPrice());
        } else {
            long nights = ChronoUnit.DAYS.between(newIn.toLocalDate(), newOut.toLocalDate());
            Integer pricePerDay = room.getPricePerDay();     // Integer
            if (pricePerDay == null) pricePerDay = 0;
            int nightsInt  = Math.toIntExact(nights);                        // ném ex nếu > 2^31-1
            int total      = Math.multiplyExact(nightsInt, pricePerDay);     // an toàn tràn
            existing.setTotalPrice(total);
        }

        // (Tuỳ) đổi user nếu cho phép
        if (dto.getUserId() != null && !dto.getUserId().equals(existing.getUser().getId())) {
            UserEntity u = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new NoSuchElementException("Không tìm thấy người dùng với id: " + dto.getUserId()));
            existing.setUser(u);
        }

        BookingEntity updated = bookingRepository.save(existing);

        // 5) Điều chỉnh RoomStatus tối thiểu (nếu không OOS)
        if (room.getRoomStatus() != RoomStatus.OUT_OF_SERVICE) {
            // Nếu còn booking CONFIRMED (kể cả chính booking này) từ hôm nay trở đi → RESERVED, else AVAILABLE
            boolean hasNext = bookingRepository.existsConfirmedAfterDate(room.getId(), LocalDateTime.now());
            room.setRoomStatus(hasNext ? RoomStatus.RESERVED : RoomStatus.AVAILABLE);
        }

        return bookingMapper.toResponseDTO(updated);
    }

    @Override
    @Transactional
    public void cancelBooking(Long id) {
        BookingEntity b = bookingRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy đặt phòng với id: " + id));

        if (b.getBookingStatus() == BookingStatus.CHECKED_IN || b.getBookingStatus() == BookingStatus.CHECKED_OUT) {
            throw new IllegalStateException("Không thể hủy khi khách đã/đang lưu trú.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (!now.isBefore(b.getCheckInDate())) { // now >= check-in
            throw new IllegalStateException("Chỉ được hủy trước thời điểm nhận phòng.");
        }

        // Đổi trạng thái booking
        b.setBookingStatus(BookingStatus.PENDING);
        bookingRepository.save(b);

        // Tính lại trạng thái phòng theo quy tắc chung
        recomputeRoomStatus(b.getRoom(), now);
    }

    @Override
    @Transactional
    public BookingResponseDTO confirmBooking(Long id) {
        // 1) Tải booking & kiểm tra trạng thái
        BookingEntity b = bookingRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy đặt phòng với id: " + id));

        if (b.getBookingStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Chỉ xác nhận được booking ở trạng thái PENDING.");
        }

        // 2) Re-check chống chồng lấn tại thời điểm xác nhận (loại trừ chính booking hiện tại)
        boolean overlap = bookingRepository.existsOverlapping(
                b.getRoom().getId(), b.getCheckInDate(), b.getCheckOutDate(), b.getId()
        );
        if (overlap) {
            throw new IllegalStateException("Phòng đã có đặt chỗ trùng khoảng ngày (vừa phát sinh).");
        }

        // 3) KHÔNG thay đổi pricePerDay / totalPrice / depositAmount
        b.setBookingStatus(BookingStatus.CONFIRMED);
        BookingEntity saved = bookingRepository.save(b);

        // 4) (Tuỳ rule) Cập nhật trạng thái phòng để phản ánh đã có booking sắp tới
        recomputeRoomStatus(saved.getRoom(), LocalDateTime.now());

        // 5) Trả DTO (ngày đã format theo mapper)
        return bookingMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public BookingResponseDTO checkIn(Long id) {
        BookingEntity b = bookingRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy đặt phòng với id: " + id));

        // Idempotent nhẹ: đã check-in thì trả về luôn
        if (b.getBookingStatus() == BookingStatus.CHECKED_IN) {
            return bookingMapper.toResponseDTO(b);
        }

        if (b.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Chỉ check-in khi booking ở trạng thái CONFIRMED.");
        }

        LocalDateTime now = LocalDateTime.now();
        // Hợp lệ khi: checkIn <= now < checkOut
        if (now.isBefore(b.getCheckInDate()) || !now.isBefore(b.getCheckOutDate())) {
            throw new IllegalStateException("Chỉ được check-in trong khoảng lưu trú (từ giờ nhận phòng đến trước giờ trả phòng).");
        }

        // Chỉ đổi trạng thái booking, không đụng tiền
        b.setBookingStatus(BookingStatus.CHECKED_IN);
        BookingEntity saved = bookingRepository.save(b);

        // Tính lại RoomStatus theo quy tắc chung (OCCUPIED nếu đang trong khoảng)
        recomputeRoomStatus(saved.getRoom(), now);

        return bookingMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public BookingResponseDTO checkOut(Long id) {
        BookingEntity b = bookingRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy đặt phòng với id: " + id));

        // Idempotent nhẹ: đã check-out rồi thì trả về luôn
        if (b.getBookingStatus() == BookingStatus.CHECKED_OUT) {
            return bookingMapper.toResponseDTO(b);
        }

        if (b.getBookingStatus() != BookingStatus.CHECKED_IN) {
            throw new IllegalStateException("Chỉ check-out sau khi CHECKED_IN.");
        }

        // (Tuỳ policy) Có thể ràng buộc: now >= checkInDate
        // Không bắt buộc now <= checkOutDate để cho phép trả phòng trễ
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(b.getCheckInDate())) {
            throw new IllegalStateException("Chưa đến thời gian lưu trú để check-out.");
        }

        // Đổi trạng thái booking (không đụng tiền)
        b.setBookingStatus(BookingStatus.CHECKED_OUT);
        BookingEntity saved = bookingRepository.save(b);

        // Cập nhật RoomStatus theo quy tắc chung
        recomputeRoomStatus(saved.getRoom(), now);

        taskScheduler.schedule(
                () -> revertToPendingSafely(b.getId()),
                Instant.now().plus(Duration.ofMinutes(5))   // dùng Instant thay Date
        );

        return bookingMapper.toResponseDTO(saved);
    }

    @Override
    public List<BookingResponseDTO> getBookingsByUser(Long userId) {
        // Lấy tất cả đặt phòng của người dùng
        List<BookingEntity> bookings = bookingRepository.findByUserIdOrderByCheckInDateDesc(userId);

        // Chuyển đổi danh sách BookingEntity thành BookingResponseDTO
        return bookings.stream()
                .map(bookingMapper::toResponseDTO)  // Ánh xạ BookingEntity thành BookingResponseDTO
                .collect(Collectors.toList());  // Trả về danh sách BookingResponseDTO
    }

    @Override
    public List<BookingResponseDTO> getBookingsBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null || !endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("Khoảng ngày không hợp lệ.");
        }
        return bookingRepository.findIntersecting(startDate, endDate)
                .stream().map(bookingMapper::toResponseDTO).toList();
    }

    @Override
    public List<BookingResponseDTO> getBookingsByRoom(Long roomId) {
        // Truy vấn tất cả các booking theo roomId
        List<BookingEntity> bookings = bookingRepository.findByRoomIdOrderByCheckInDateDesc(roomId);

        // Chuyển đổi BookingEntity thành BookingResponseDTO
        return bookings.stream()
                .map(bookingMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    private Specification<BookingEntity> getSearchSpecification(BookingFilterDTO f) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();

            // JOIN user để lọc theo userId / fullName
            Join<BookingEntity, UserEntity> user = root.join("user", JoinType.INNER);

            // userId = ?
            if (f.getUserId() != null) {
                ps.add(cb.equal(user.get("id"), f.getUserId()));
            }

            // fullName LIKE %kw% (case-insensitive)
            if (StringUtils.isNotBlank(f.getFullName())) {
                String kw = "%" + f.getFullName().trim().toLowerCase() + "%";
                ps.add(cb.like(cb.lower(user.get("fullName")), kw));
            }

            // checkInDate >= from
            if (f.getCheckInDate() != null) {
                ps.add(cb.greaterThanOrEqualTo(root.get("checkInDate"), f.getCheckInDate()));
            }
            // checkOutDate <= to
            if (f.getCheckOutDate() != null) {
                ps.add(cb.lessThanOrEqualTo(root.get("checkOutDate"), f.getCheckOutDate()));
            }
            // Nếu from > to -> không match gì
            if (f.getCheckInDate() != null && f.getCheckOutDate() != null
                    && f.getCheckInDate().isAfter(f.getCheckOutDate())) {
                ps.add(cb.disjunction());
            }

            // Không tự orderBy ở đây để nhường cho Pageable sort (Utils.generatedSort)
            // query.distinct(true); // bật nếu sau này join nhiều gây trùng

            return ps.isEmpty() ? cb.conjunction() : cb.and(ps.toArray(new Predicate[0]));
        };
    }

    private void recomputeRoomStatus(RoomEntity room, LocalDateTime now) {
        if (room.getRoomStatus() == RoomStatus.OUT_OF_SERVICE) return;

        Long roomId = room.getId();

        boolean hasOngoing = bookingRepository.existsConfirmedOngoing(roomId, now);
        if (hasOngoing) {
            room.setRoomStatus(RoomStatus.OCCUPIED);
            return;
        }

        boolean hasFuture = bookingRepository.existsConfirmedAfter(roomId, now);
        if (hasFuture) {
            room.setRoomStatus(RoomStatus.RESERVED);
            return;
        }

        room.setRoomStatus(RoomStatus.AVAILABLE);
    }

    /** Chạy trong thread pool, cần bắt lỗi & mở transaction riêng */
    @Transactional
    void revertToPendingSafely(Long bookingId) {
        bookingRepository.findById(bookingId).ifPresent(booking -> {
            // Chỉ đổi khi vẫn CHECKED_OUT (tránh đè status khác)
            if (booking.getBookingStatus() == BookingStatus.CHECKED_OUT) {
                booking.setBookingStatus(BookingStatus.PENDING);
                bookingRepository.save(booking);

                // Tính lại RoomStatus lần nữa
                recomputeRoomStatus(booking.getRoom(), LocalDateTime.now());
            }
        });
    }
}
