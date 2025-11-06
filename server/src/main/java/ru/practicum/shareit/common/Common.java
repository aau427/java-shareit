package ru.practicum.shareit.common;

import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;

public class Common {
    public static final String USER_HEADER = "X-Sharer-User-Id";
    public static final BookingStatus DEFAULT_BOOKING_STATUS = BookingStatus.WAITING;

    public static LocalDateTime getLocalDateTime() {
        return LocalDateTime.now();
    }
}
