package com.hotelbooking.Constants;

public final class Constants {
    private Constants() {}

    public static final class DateFormatType {
        public static final String INPUT_REGEX = "^\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}:\\d{2}$";
        public static final String INPUT_MESSAGE = "Ngày phải có định dạng dd/MM/yyyy HH:mm:ss";
        public static final String JAVA_PATTERN  = "dd/MM/yyyy HH:mm:ss";
    }
}
