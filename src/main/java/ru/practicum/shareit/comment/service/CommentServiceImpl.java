package ru.practicum.shareit.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.mapper.SimpleCommentMapper;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.comment.storage.CommentStorage;
import ru.practicum.shareit.exceptions.LogicalException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {
    private final CommentStorage commentStorage;
    private final SimpleCommentMapper commentMapper;
    private final ItemService itemService;
    private final UserService userService;

    @Transactional
    @Override
    public CommentDto addComment(CommentDto inputCommentDto) {
        Comment comment = commentMapper.dtoToComment(inputCommentDto, itemService, userService);
        checkComment(comment);
        return commentMapper.commentToDto(commentStorage.save(comment));
    }

    private void checkComment(Comment comment) {
        List<Item> tmpList = itemService.getItemsWasCompleteBookingByUser(comment.getItem().getId(),
                comment.getAuthor().getId(),
                comment.getCreated());
        if (tmpList.isEmpty()) {
            log.warn("Пользователь {} не брал вещь {} в аренду или срок аренды не закончен!",
                    comment.getAuthor().getId(), comment.getItem().getId());
            throw new LogicalException(String.format("Пользователь %d не брал вещь %d в аренду или срок аренды не закончен!",
                    comment.getAuthor().getId(), comment.getItem().getId()));
        }
    }
}
