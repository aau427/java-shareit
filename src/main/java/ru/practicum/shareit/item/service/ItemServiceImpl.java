package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.CustomValidationException;
import ru.practicum.shareit.exceptions.ResourceNotFoundException;
import ru.practicum.shareit.exceptions.RightsException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemStorage itemStorage;
    private final UserService userService;

    @Override
    public ItemDto createItem(ItemDto itemDto) {
        if (itemDto.getId() != null) {
            String message = String.format("Ошибка при создании вещи: указан Id = %d", itemDto.getId());
            log.error(message);
            throw new CustomValidationException(message);
        }
        Item item = ItemMapper.dtoToItem(itemDto);
        item.setOwner(userService.getUserById(itemDto.getOwner()));
        return ItemMapper.itemToDto(itemStorage.createItem(item));
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto) {
        Item itemForUpdate = getItemById(itemDto.getId());
        Item newItem = ItemMapper.dtoToItem(itemDto);
        newItem.setOwner(userService.getUserById(itemDto.getOwner()));
        //отредактировать вещь может только ее владелец
        checkOwner(newItem.getOwner(), itemForUpdate.getOwner());
        //отредактировать можно только название, комментарий и доступность...
        if (newItem.getName() != null) {
            itemForUpdate.setName(newItem.getName());
        }
        if (newItem.getDescription() != null) {
            itemForUpdate.setDescription(newItem.getDescription());
        }
        if (newItem.getAvailable() != null) {
            itemForUpdate.setAvailable(newItem.getAvailable());
        }
        return ItemMapper.itemToDto(itemStorage.updateItem(itemForUpdate));
    }

    @Override
    public ItemDto getItemDtoById(Integer itemId) {
        return ItemMapper.itemToDto(getItemById(itemId));
    }

    @Override
    public List<ItemDto> getUsersItems(Integer userId) {
        return itemStorage.getUsersItem(userService.getUserById(userId)).stream()
                .map(ItemMapper::itemToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> getItemsByContext(String context) {
        if (context.isEmpty()) {
            return new ArrayList<>();
        }
        return itemStorage.getItemsByContext(context).stream()
                .map(ItemMapper::itemToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Item getItemById(int itemId) {
        Optional<Item> itemOptional = itemStorage.getItemById(itemId);
        if (itemOptional.isEmpty()) {
            String msg = "Не нашел вещь с Id = " + itemId;
            log.error(msg);
            throw new ResourceNotFoundException(msg);
        }
        return itemOptional.get().clone();
    }

    private void checkOwner(User newOwner, User oldOwner) {
        if (!newOwner.getId().equals(oldOwner.getId())) {
            throw new RightsException("Редактировать вещь может только ее владелец");
        }
    }
}
