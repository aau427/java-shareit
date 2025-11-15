package shareit.item.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shareit.common.Common;
import shareit.item.client.ItemClient;
import shareit.item.dto.CommentDto;
import shareit.item.dto.ItemDto;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestBody @Valid ItemDto itemDto,
                                             @RequestHeader(value = Common.USER_HEADER) Long userId) {
        return itemClient.createItem(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestBody ItemDto itemDto,
                                             @PathVariable Long itemId,
                                             @RequestHeader(value = Common.USER_HEADER) Long userId) {
        return itemClient.updateItem(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@RequestHeader(value = Common.USER_HEADER) Long userId,
                                              @PathVariable Long itemId) {
        return itemClient.getItemById(itemId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getUsersItems(@RequestHeader(value = Common.USER_HEADER) Long userId) {
        return itemClient.getUsersItems(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> getItemsByContext(@RequestParam("text") String context,
                                                    @RequestHeader(value = Common.USER_HEADER) Long userId) {
        return itemClient.getItemsByContext(userId, context);
    }


    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestBody @Valid CommentDto commentDto,
                                             @PathVariable Integer itemId,
                                             @RequestHeader(value = Common.USER_HEADER) Integer authorId) {
        return itemClient.addCommentToItem(authorId, itemId, commentDto);
    }
}
