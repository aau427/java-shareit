package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.booking.mapper.ShortBookingMapper;
import ru.practicum.shareit.booking.model.LastAndNextBookings;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.mapper.SimpleCommentMapper;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOutDtoWithDates;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class SimpleItemMapper {
    @Autowired
    protected UserService userService;
    @Autowired
    protected ShortBookingMapper bookingMapper;
    @Autowired
    protected SimpleCommentMapper commentMapper;

    @Mapping(target = "request", ignore = true)
    @Mapping(target = "owner", expression = "java(userService.getUserById(itemDto.getOwner()))")
    public abstract Item dtoToItem(ItemDto itemDto);

    @Mapping(target = "owner", expression = "java(item.getOwner().getId())")
    @Mapping(target = "request", expression = "java((item.getRequest() == null) ? null : item.getRequest().getId())")
    public abstract ItemDto itemToDto(Item item);

    @Mapping(target = "lastBooking", expression = "java(bookingMapper.bookingToShortDto(twoBookings.getLastBooking()))")
    @Mapping(target = "nextBooking", expression = "java(bookingMapper.bookingToShortDto(twoBookings.getNextBooking()))")
    @Mapping(target = "comments", expression = "java(this.getDtoList(commentList))")
    public abstract ItemOutDtoWithDates toItemOutDtoWithDate(Item item, LastAndNextBookings twoBookings, List<Comment> commentList);

    public List<CommentDto> getDtoList(List<Comment> commentList) {
        return commentList.stream()
                .map(comment -> commentMapper.commentToDto(comment))
                .toList();
    }
}
