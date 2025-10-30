package ru.practicum.shareit.item.dto;

import lombok.Data;
import ru.practicum.shareit.booking.dto.ShortOutBookingDto;
import ru.practicum.shareit.comment.dto.CommentDto;

import java.util.List;

@Data
public class ItemOutDtoWithDates {
    private Integer id;
    private String name;
    private String description;
    private Boolean available;
    private ShortOutBookingDto lastBooking;
    private ShortOutBookingDto nextBooking;
    private List<CommentDto> comments;
}
