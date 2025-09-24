package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.service.UserService;

@Mapper(componentModel = "spring")
public abstract class SimpleItemMapper {
    @Autowired
    protected UserService userService;

    @Mapping(target = "request", ignore = true)
    @Mapping(target = "owner", expression = "java(userService.getUserById(itemDto.getOwner()))")
    public abstract Item dtoToItem(ItemDto itemDto);

    @Mapping(target = "owner", expression = "java(item.getOwner().getId())")
    @Mapping(target = "request", expression = "java((item.getRequest() == null) ? null : item.getRequest().getId())")
    public abstract ItemDto itemToDto(Item item);
}
