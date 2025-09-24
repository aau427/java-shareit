package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.CustomValidationException;
import ru.practicum.shareit.exceptions.ResourceNotFoundException;
import ru.practicum.shareit.exceptions.RightsException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.SimpleItemMapperImpl;
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
    private final SimpleItemMapperImpl itemMapper;

    @Override
    public ItemDto createItem(ItemDto itemDto) {
        if (itemDto.getId() != null) {
            String message = String.format("Ошибка при создании вещи: указан Id = " + itemDto.getId());
            log.error(message);
            throw new CustomValidationException(message);
        }
        Item item = itemMapper.dtoToItem(itemDto);
        item.setOwner(userService.getUserById(itemDto.getOwner()));
        return itemMapper.itemToDto(itemStorage.createItem(item));
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto) {
        Item itemForUpdate = getItemById(itemDto.getId());
        Item newItem = itemMapper.dtoToItem(itemDto);
        //отредактировать вещь может только ее владелец
        checkOwner(newItem.getOwner(), itemForUpdate.getOwner());
        /* Отредактировать можно только название, комментарий и доступность.
           Причем во вх. DTO в наличии только те поля, которые обновляются */
        if (newItem.getName() != null) {
            itemForUpdate.setName(newItem.getName());
        }
        if (newItem.getDescription() != null) {
            itemForUpdate.setDescription(newItem.getDescription());
        }
        if (newItem.getAvailable() != null) {
            itemForUpdate.setAvailable(newItem.getAvailable());
        }
        return itemMapper.itemToDto(itemStorage.updateItem(itemForUpdate));
    }

    @Override
    public ItemDto getItemDtoById(Integer itemId) {
        return itemMapper.itemToDto(getItemById(itemId));
    }

    @Override
    public List<ItemDto> getUsersItems(Integer userId) {
        return itemStorage.getUsersItem(userService.getUserById(userId)).stream()
                .map(itemMapper::itemToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> getItemsByContext(String context) {
        if (context.isEmpty()) {
            return new ArrayList<>();
        }
        return itemStorage.getItemsByContext(context).stream()
                .map(itemMapper::itemToDto)
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
