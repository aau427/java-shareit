package ru.practicum.shareit.booking.dto;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

@Setter
@Getter
public class OutputBookingDto {
    Integer id;
    LocalDateTime start;
    LocalDateTime end;
    BookingStatus status;
    ItemDto item;
    UserDto booker;
}
