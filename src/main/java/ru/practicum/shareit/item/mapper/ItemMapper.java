package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

public class ItemMapper {
    public static Item dtoToItem(ItemDto itemDto) {
        Item item = Item.builder()
                .name(itemDto.getName())
                .description((itemDto.getDescription()))
                .available(itemDto.getAvailable())
                .build();
        if(itemDto.getId() != null) {
            item.setId(itemDto.getId());
        }
        return item;
    }

    public static ItemDto itemToDto(Item item) {
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
