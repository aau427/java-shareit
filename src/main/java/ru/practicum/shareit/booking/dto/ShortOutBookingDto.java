package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

public class ShortOutBookingDto {
    Integer id;
    LocalDateTime start;
    LocalDateTime end;
    BookingStatus status;
    UserDto booker;
}
