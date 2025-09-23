package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemDto createItem(@RequestBody @Valid ItemDto itemDto,
                              @RequestHeader(value = "X-Sharer-User-Id") Integer userId) {
        itemDto.setOwner(userId);
        return itemService.createItem(itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestBody ItemDto itemDto,
                              @PathVariable Integer itemId,
                              @RequestHeader(value = "X-Sharer-User-Id") Integer userId) {
        itemDto.setOwner(userId);
        itemDto.setId(itemId);
        return itemService.updateItem(itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@PathVariable Integer itemId) {
        return itemService.getItemDtoById(itemId);
    }

    @GetMapping
    public List<ItemDto> getUsersItems(@RequestHeader(value = "X-Sharer-User-Id") Integer userId) {
        return itemService.getUsersItems(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> getItemsByContext(@RequestParam("text") String context) {
        return itemService.getItemsByContext(context);
    }
}
