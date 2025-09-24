package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class InMemoryItemStorage implements ItemStorage {
    private final Map<Integer, Item> itemMap;

    public InMemoryItemStorage() {
        this.itemMap = new HashMap<>();
    }

    @Override
    public Item createItem(Item item) {
        item.setId(itemMap.size() + 1);
        itemMap.put(item.getId(), item);
        return item;
    }

    @Override
    public Item updateItem(Item item) {
        itemMap.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> getItemById(int itemId) {
        return Optional.ofNullable(itemMap.get(itemId));
    }

    @Override
    public List<Item> getUsersItem(User user) {
        return itemMap.values().stream()
                .filter(item -> item.getOwner().equals(user))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> getItemsByContext(String context) {
        String upperContext = context.toUpperCase();
        return itemMap.values().stream()
                .filter(item -> (item.getName().toUpperCase().contains(upperContext) ||
                        item.getDescription().toUpperCase().contains(upperContext))
                        && item.getAvailable())
                .collect(Collectors.toList());
    }
}
