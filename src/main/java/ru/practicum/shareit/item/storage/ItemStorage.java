package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface ItemStorage {
    Item createItem(Item item);
    Item updateItem(Item item);
    Optional<Item> getItemById(int itemId);
    List<Item> getUsersItem(User user);
    List<Item> getItemsByContext(String context);
}
