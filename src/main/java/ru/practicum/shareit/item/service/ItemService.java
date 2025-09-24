package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    ItemDto createItem(ItemDto itemDto);

    ItemDto updateItem(ItemDto itemDto);

    Item getItemById(int itemId);

    ItemDto getItemDtoById(Integer itemId);

    List<ItemDto> getUsersItems(Integer userId);

    List<ItemDto> getItemsByContext(String context);
}
