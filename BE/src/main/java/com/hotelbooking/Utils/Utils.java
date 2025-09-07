package com.hotelbooking.Utils;

import com.hotelbooking.Constants.Constants;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Utils {
    public static Sort generatedSort(String input) {
        // null/rỗng -> mặc định id desc
        if (input == null || input.trim().isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "id");
        }

        // Tách theo dấu phẩy, cho phép nhập: "name,asc,city,desc"
        String[] tokens = input.split(",");
        List<Sort.Order> orders = new ArrayList<>();

        for (int i = 0; i < tokens.length; i += 2) {
            String field = tokens[i].trim();
            if (field.isEmpty()) continue; // bỏ qua nếu field rỗng

            String direction = (i + 1 < tokens.length) ? tokens[i + 1].trim() : "asc";
            if ("desc".equalsIgnoreCase(direction)) {
                orders.add(Sort.Order.desc(field));
            } else {
                orders.add(Sort.Order.asc(field));
            }
        }

        // Nếu người dùng nhập lỗi dẫn đến không có order nào, fallback id desc
        if (orders.isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "id");
        }
        return Sort.by(orders);
    }

    public final class DateTimeUtils {
        private DateTimeUtils() {} // chặn khởi tạo

        private static final DateTimeFormatter FORMATTER =
                DateTimeFormatter.ofPattern(Constants.DateFormatType.JAVA_PATTERN, Locale.forLanguageTag("vi-VN"));

        /** Parse string sang LocalDateTime theo pattern chuẩn. */
        public static LocalDateTime parse(String input) {
            if (input == null || input.trim().isEmpty()) {
                throw new IllegalArgumentException("Thời gian không được để trống");
            }
            try {
                return LocalDateTime.parse(input.trim(), FORMATTER);
            } catch (DateTimeParseException e) {
                // dùng đúng message trong Constants
                throw new IllegalArgumentException(Constants.DateFormatType.INPUT_MESSAGE);
            }
        }

        /** Validate checkIn < checkOut và checkIn không ở quá khứ. */
        public static void validateRange(LocalDateTime checkIn, LocalDateTime checkOut) {
            if (checkIn == null || checkOut == null) {
                throw new IllegalArgumentException("Thời gian nhận/trả không được để trống");
            }
            if (!checkOut.isAfter(checkIn)) {
                throw new IllegalArgumentException("Ngày trả phòng phải sau ngày nhận phòng");
            }
            if (checkIn.isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Ngày nhận phòng phải từ hiện tại trở đi");
            }
        }
    }
}
