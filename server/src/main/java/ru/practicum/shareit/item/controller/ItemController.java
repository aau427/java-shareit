package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.service.CommentService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOutDtoWithDates;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.shareit.common.Common.USER_HEADER;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private final CommentService commentService;

    @PostMapping
    public ItemDto createItem(@RequestBody @Valid ItemDto itemDto,
                              @RequestHeader(value = USER_HEADER) Integer userId) {
        itemDto.setOwner(userId);
        return itemService.createItem(itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestBody ItemDto itemDto,
                              @PathVariable Integer itemId,
                              @RequestHeader(value = USER_HEADER) Integer userId) {
        itemDto.setOwner(userId);
        itemDto.setId(itemId);
        return itemService.updateItem(itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemOutDtoWithDates getItemById(@RequestHeader(value = USER_HEADER) Integer userId,
                                           @PathVariable Integer itemId) {
        return itemService.getItemDtoById(itemId, userId);
    }

    @GetMapping
    public List<ItemOutDtoWithDates> getUsersItems(@RequestHeader(value = USER_HEADER) Integer userId) {
        return itemService.getUsersItems(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> getItemsByContext(@RequestParam("text") String context) {
        return itemService.getItemsByContext(context);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestBody @Valid CommentDto commentDto,
                                 @PathVariable Integer itemId,
                                 @RequestHeader(value = USER_HEADER) Integer authorId) {
        commentDto.setItemId(itemId);
        commentDto.setAuthorId(authorId);
        if (commentDto.getCreated() == null) {
            commentDto.setCreated(LocalDateTime.now());
        }
        return commentService.addComment(commentDto);
    }
}
