package ru.practicum.shareit.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.item.dto.ItemShortOutDto;
import ru.practicum.shareit.item.mapper.SimpleItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithItems;
import ru.practicum.shareit.request.dto.ItemRequestInDto;
import ru.practicum.shareit.request.dto.ItemRequestOutDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class SimpleItemRequestMapper {
    @Autowired
    protected UserService userService;

    @Autowired
    protected SimpleItemMapper itemMapper;

    @Mapping(target = "requester", expression = "java(userService.getUserById(itemRequestInDto.getUserId()))")
    @Mapping(target = "created", expression = "java(getCurrentDateTime())")
    public abstract ItemRequest dtoToItemRequest(ItemRequestInDto itemRequestInDto);

    public abstract ItemRequestOutDto itemRequestToDto(ItemRequest itemRequest);

    @Mapping(target = "items", expression = "java(getItemsDtoList(itemList))")
    public abstract ItemRequestDtoWithItems itemToItemRequestWithItems(ItemRequest itemRequest, List<Item> itemList);

    protected LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now();
    }

    protected List<ItemShortOutDto> getItemsDtoList(List<Item> itemList) {
        return itemList.stream()
                .map(item -> itemMapper.toItemShortDto(item))
                .collect(Collectors.toList());
    }
}
