package ru.practicum.shareit;

import ru.practicum.shareit.booking.dto.ShortOutBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOutDtoWithDates;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;

public abstract class BaseUtility {
    protected User createUser(int id, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    protected UserDto createUserDto(int id, String name, String email) {
        UserDto userDto = new UserDto();
        userDto.setId(id);
        userDto.setName(name);
        userDto.setEmail(email);
        return userDto;
    }


    protected Item createItem(int id, String name, String description, boolean isAvialabel, User owner, ItemRequest request) {
        Item item = new Item();
        item.setId(id);
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(isAvialabel);
        item.setOwner(owner);
        item.setRequest(request);
        return item;
    }

    protected ItemDto createItemDto(Integer id, String name, String description, boolean isAvialabel, int ownerId, Integer requestId) {
        return ItemDto.builder()
                .id(id)
                .name(name)
                .description(description)
                .available(isAvialabel)
                .owner(ownerId)
                .requestId(requestId)
                .build();
    }

    protected ItemOutDtoWithDates createItemDtoWithDates(int id, Item item, Booking lastBooking, Booking nextBooking) {
        ItemOutDtoWithDates itemDto = new ItemOutDtoWithDates();
        itemDto.setId(id);
        itemDto.setName(item.getName());
        itemDto.setDescription(item.getDescription());
        itemDto.setAvailable(item.getAvailable());
        if (lastBooking != null) {
            User lastBooker = lastBooking.getBooker();
            UserDto lastBookerDto = createUserDto(lastBooker.getId(), lastBooker.getName(), lastBooker.getEmail());

            ShortOutBookingDto lastBookingDto = getShortBookingDto(1, item, lastBooking.getStart(), lastBooking.getEnd(),
                    lastBooking.getStatus(), lastBookerDto);
            itemDto.setLastBooking(lastBookingDto);
        }
        if (nextBooking != null) {
            User nextBooker = nextBooking.getBooker();
            UserDto nextBookerDto = createUserDto(nextBooker.getId(), nextBooker.getName(), nextBooker.getEmail());
            ShortOutBookingDto nextBookingDto = getShortBookingDto(2, item, nextBooking.getStart(), nextBooking.getEnd(),
                    nextBooking.getStatus(), nextBookerDto);
            itemDto.setNextBooking(nextBookingDto);
        }
        itemDto.setComments(Collections.emptyList());
        return itemDto;
    }

    protected ShortOutBookingDto getShortBookingDto(int id, Item item, LocalDateTime start, LocalDateTime end,
                                                    BookingStatus status, UserDto booker) {
        ShortOutBookingDto bookingDto = new ShortOutBookingDto();
        bookingDto.setId(id);
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        bookingDto.setStatus(status);
        bookingDto.setBooker(booker);
        return bookingDto;
    }

    protected Booking createBooking(int id, User booker, LocalDateTime start, LocalDateTime end, BookingStatus status) {
        Booking booking = new Booking();
        booking.setId(1);
        booking.setBooker(booker);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setStatus(status);
        return booking;
    }

    protected UserDto getUserDtoCopy(UserDto iUserDto) {
        UserDto userDto = new UserDto();
        userDto.setId(iUserDto.getId());
        userDto.setName(iUserDto.getName());
        userDto.setEmail(iUserDto.getEmail());
        return userDto;
    }
}
