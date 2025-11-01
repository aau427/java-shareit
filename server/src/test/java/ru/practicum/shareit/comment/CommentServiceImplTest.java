package ru.practicum.shareit.comment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.BaseUtility;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.mapper.SimpleCommentMapper;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.comment.service.CommentServiceImpl;
import ru.practicum.shareit.comment.storage.CommentStorage;
import ru.practicum.shareit.exceptions.LogicalException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest extends BaseUtility {

    @Mock
    private CommentStorage commentStorage;

    @Mock
    private SimpleCommentMapper commentMapper;

    @Mock
    private ItemService itemService;

    @Mock
    private UserService userService;

    @InjectMocks
    private CommentServiceImpl commentService;

    private User author;
    private Item item;
    private CommentDto inputCommentDto;
    private Comment commentToSave;
    private CommentDto savedCommentDto;
    private LocalDateTime created;

    @BeforeEach
    void beforeEach() {
        created = LocalDateTime.now();
        author = createUser(1, "Андрей", "some@mail.ru");
        item = createItem(1, "Носок", "я сейчас сойду с ума с этими тестами", true,
                author, null);

        inputCommentDto = createCommentDto(1, "грязный старый носок", author, item, created);
        commentToSave = createComment(1, "Test comment text", author, item, created);
        savedCommentDto = createCommentDto(1, "грязный старый носок", author, item, created);

    }

    @DisplayName("Комментарий успешно добавлен, если пользователь брал вещь в аренду")
    @Test
    void shouldSaveCommentSuccessfully() {
        when(commentMapper.dtoToComment(inputCommentDto, itemService, userService))
                .thenReturn(commentToSave);
        when(itemService.getItemsWasCompleteBookingByUser(
                item.getId(),
                author.getId(),
                created))
                .thenReturn(List.of(item));

        when(commentStorage.save(commentToSave)).thenReturn(commentToSave);

        when(commentMapper.commentToDto(commentToSave)).thenReturn(savedCommentDto);

        CommentDto result = commentService.addComment(inputCommentDto);


        assertNotNull(result);
        assertEquals(savedCommentDto.getText(), result.getText());
        assertEquals(savedCommentDto.getId(), result.getId());
        verify(commentStorage, times(1)).save(commentToSave);
    }


    @DisplayName("Добавление комментария должно выбросить исключение, если пользователь не брал вещь в аренду")
    @Test
    void ShouldThrowException_WhenUserDidNotBookItem() {

        when(commentMapper.dtoToComment(inputCommentDto, itemService, userService))
                .thenReturn(commentToSave);

        when(itemService.getItemsWasCompleteBookingByUser(
                anyInt(),
                anyInt(),
                any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        assertThrows(LogicalException.class, () -> commentService.addComment(inputCommentDto));

        verify(commentStorage, never()).save(any(Comment.class));
    }
}