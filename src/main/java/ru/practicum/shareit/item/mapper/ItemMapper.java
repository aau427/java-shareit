package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.service.UserService;

public class ItemMapper {
    private final UserService userService;

    public ItemMapper(UserService userService) {
        this.userService = userService;
    }

    public Item dtoToItem(ItemDto itemDto) {
        Item item = Item.builder()
                .name(itemDto.getName())
                .description((itemDto.getDescription()))
                .available(itemDto.getAvailable())
                .build();
        if (itemDto.getId() != null) {
            item.setId(itemDto.getId());
        }
        if (itemDto.getOwner() != null) {
            item.setOwner(userService.getUserById(itemDto.getOwner()));
        }
        return item;
    }

    public ItemDto itemToDto(Item item) {
        ItemDto itemDto = ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description((item.getDescription()))
                .available(item.getAvailable())
                .owner(item.getOwner().getId())
                .build();
        if (item.getRequest() != null) {
            itemDto.setRequest(item.getRequest().getId());
        }
        return itemDto;
    }
}
