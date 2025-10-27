package ru.practicum.shareit.request.dto;

import lombok.Data;
import ru.practicum.shareit.item.dto.ItemShortOutDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ItemRequestDtoWithItems {
    private Integer id;
    private String description;
    private LocalDateTime created;
    private List<ItemShortOutDto> items;
}
