package ru.practicum.shareit.common;

import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Common {
    public static final String USER_HEADER = "X-Sharer-User-Id";
    public static final BookingStatus DEFAULT_BOOKING_STATUS = BookingStatus.WAITING;

    public static LocalDateTime getLocalDateTime() {
        return LocalDateTime.ofInstant(Instant.now(), ZoneId.of("Europe/Moscow"));
    }
}
