package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOutDtoWithDates;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.List;

public interface ItemService {
    ItemDto createItem(ItemDto itemDto);

    ItemDto updateItem(ItemDto itemDto);

    Item getItemById(int itemId);

    ItemOutDtoWithDates getItemDtoById(Integer itemId, Integer userId);

    List<ItemOutDtoWithDates> getUsersItems(Integer userId);

    List<ItemDto> getItemsByContext(String context);

    List<Item> getItemsWasCompleteBookingByUser(Integer itemId, Integer userId, LocalDateTime created);
}
