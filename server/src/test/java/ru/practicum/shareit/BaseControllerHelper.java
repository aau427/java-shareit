package ru.practicum.shareit;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

public class BaseControllerHelper {
    protected UserDto createFirstUserDto() {
        UserDto userDto = new UserDto();
        userDto.setName("Андрей");
        userDto.setEmail("andrey@mail.ru");
        return userDto;
    }

    protected UserDto createSecondUserDto() {
        UserDto userDto = new UserDto();
        userDto.setName("Anastasia");
        userDto.setEmail("anastasia@mail.ru");
        return userDto;
    }


    protected ItemDto createFirstInputItemDto(int ownerId) {
        return ItemDto.builder()
                .name("Вещь 1")
                .description("Прекрасная вещь1")
                .available(true)
                .owner(ownerId)
                .build();
    }

    protected ItemDto createSecondItemDto(int ownerId) {
        return ItemDto.builder()
                .name("Вещь 2")
                .description("Прекрасная вещь 2")
                .available(true)
                .owner(ownerId)
                .build();
    }


}
