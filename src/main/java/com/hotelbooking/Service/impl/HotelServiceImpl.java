package com.hotelbooking.Service.impl;

import com.hotelbooking.DTO.HotelDTO.HotelCreateDTO;
import com.hotelbooking.DTO.HotelDTO.HotelFilterDTO;
import com.hotelbooking.DTO.HotelDTO.HotelResponseDTO;
import com.hotelbooking.DTO.HotelDTO.HotelUpdateDTO;
import com.hotelbooking.Entities.HotelEntity;
import com.hotelbooking.Mappers.HotelMapper;
import com.hotelbooking.Repositories.HotelRepository;
import com.hotelbooking.Service.HotelService;
import com.hotelbooking.Utils.Utils;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class HotelServiceImpl implements HotelService {

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private HotelMapper hotelMapper;

    @Override
    public Page<HotelResponseDTO> search(HotelFilterDTO filter) {
        Sort sort = Utils.generatedSort(filter.getSort()); // ví dụ: "rating,desc,name,asc"
        int page  = (filter.getPage()  == null || filter.getPage()  < 0) ? 0  : filter.getPage();
        int limit = (filter.getLimit() == null || filter.getLimit() <= 0) ? 10 : filter.getLimit();
        Pageable pageable = PageRequest.of(page, limit, sort);

        Specification<HotelEntity> spec = getSearchSpecification(filter);

        return hotelRepository.findAll(spec, pageable)
                .map(hotelMapper::toResponseDTO);
    }

    @Override
    public List<HotelResponseDTO> getAllHotels() {
        return hotelRepository.findAll()
                .stream()
                .map(hotelMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public HotelResponseDTO getHotelById(Long id) {
        HotelEntity entity = hotelRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không thể tìm khách sạn với id: " + id));
        return hotelMapper.toResponseDTO(entity);
    }

    @Override
    @Transactional
    public HotelResponseDTO createHotel(HotelCreateDTO dto) {
        // Chuẩn hoá input để check trùng
        String name = normalize(dto.getName());
        String addr = normalize(dto.getAddress());
        String city = normalize(dto.getCity());

        boolean dup = hotelRepository.existsDuplicate(name, addr, city, null);
        if (dup) {
            throw new IllegalArgumentException("Khách sạn đã tồn tại!");
        }

        HotelEntity toSave = hotelMapper.toEntity(dto);
        HotelEntity saved = hotelRepository.save(toSave);
        return hotelMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public HotelResponseDTO updateHotel(Long id, HotelUpdateDTO dto) {
        HotelEntity entity = hotelRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không thể tìm khách sạn với id: " + id));

        // Chuẩn hoá input để check trùng (loại trừ chính nó)
        String name = normalize(dto.getName());
        String addr = normalize(dto.getAddress());
        String city = normalize(dto.getCity());

        boolean dup = hotelRepository.existsDuplicate(name, addr, city, id);
        if (dup) {
            throw new IllegalArgumentException("Thông tin cập nhật bị trùng với khách sạn khác!");
        }

        hotelMapper.updateEntityFromDto(dto, entity);
        HotelEntity saved = hotelRepository.save(entity);
        return hotelMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public void deleteHotels(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        hotelRepository.deleteAllByIdInBatch(ids);
    }

    private Specification<HotelEntity> getSearchSpecification(HotelFilterDTO filter) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();

            // --- Keyword: name OR city OR description ---
            String keyword = normalize(filter.getKeyword());
            Predicate byName = null, byCityLike = null, byDesc = null;
            if (!keyword.isEmpty()) {
                String kw = "%" + keyword.toLowerCase() + "%";
                Expression<String> nameExpr = cb.lower(root.get("name"));
                Expression<String> cityExpr = cb.lower(root.get("city"));
                Expression<String> descExpr = cb.lower(root.get("description"));

                byName     = cb.like(nameExpr, kw);
                byCityLike = cb.like(cityExpr, kw);
                byDesc     = cb.like(descExpr, kw);

                ps.add(cb.or(byName, byCityLike, byDesc));

                // Ưu tiên: match name > city > description
                query.orderBy(
                        cb.desc(cb.selectCase().when(byName, 1).otherwise(0)),
                        cb.desc(cb.selectCase().when(byCityLike, 1).otherwise(0)),
                        cb.desc(cb.selectCase().when(byDesc, 1).otherwise(0)),
                        cb.desc(root.get("rating")),         // tie-break: rating cao hơn trước
                        cb.asc(root.get("name"))             // tie-break: tên A-Z
                );
            }

            // --- City: equal (case-insensitive) ---
            String cityEq = normalize(filter.getCity());
            if (!cityEq.isEmpty()) {
                ps.add(cb.equal(cb.lower(root.get("city")), cityEq.toLowerCase()));
            }

            // --- Rating range ---
            if (filter.getMinRating() != null) {
                ps.add(cb.greaterThanOrEqualTo(root.get("rating"), filter.getMinRating()));
            }
            if (filter.getMaxRating() != null) {
                ps.add(cb.lessThanOrEqualTo(root.get("rating"), filter.getMaxRating()));
            }
            if (filter.getMinRating() != null && filter.getMaxRating() != null
                    && filter.getMinRating() > filter.getMaxRating()) {
                // min > max -> không match gì
                ps.add(cb.disjunction());
            }

            // Nếu sau này có join (amenities...), nên bật distinct để tránh trùng dòng
            // query.distinct(true);

            return ps.isEmpty() ? cb.conjunction() : cb.and(ps.toArray(new Predicate[0]));
        };
    }

    private String normalize(String s) {
        if (s == null) return "";
        String t = s.trim().replaceAll("\\s+", " ");
        return t;
    }
}
