package ru.practicum.shareit.booking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.booking.dto.InputBookingDto;
import ru.practicum.shareit.booking.dto.OutputBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.mapper.SimpleItemMapper;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.mapper.SimpleUserMapper;
import ru.practicum.shareit.user.service.UserService;

@Mapper(componentModel = "spring")
public abstract class SimpleBookingMapper {
    @Autowired
    protected UserService userService;

    @Autowired
    protected ItemService itemService;

    @Autowired
    protected SimpleItemMapper itemMapper;

    @Autowired
    protected SimpleUserMapper userMapper;

    @Mapping(target = "item", expression = "java(itemService.getItemById(bookingDto.getItemId()))")
    @Mapping(target = "booker", expression = "java(userService.getUserById(bookingDto.getBookerId()))")
    public abstract Booking dtoToBooking(InputBookingDto bookingDto);

    @Mapping(target = "item", expression = "java(itemMapper.itemToDto(booking.getItem()))")
    @Mapping(target = "booker", expression = "java(userMapper.userToDto(booking.getBooker()))")
    public abstract OutputBookingDto bookingToDto(Booking booking);
}
