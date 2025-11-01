package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.booking.mapper.SimpleBookingMapperImpl;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.comment.mapper.SimpleCommentMapperImpl;
import ru.practicum.shareit.comment.storage.CommentStorage;
import ru.practicum.shareit.item.mapper.SimpleItemMapperImpl;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.request.mapper.SimpleItemRequestMapper;
import ru.practicum.shareit.request.mapper.SimpleItemRequestMapperImpl;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.request.storage.ItemRequestStorage;
import ru.practicum.shareit.user.mapper.SimpleUserMapperImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;
import ru.practicum.shareit.user.storage.UserStorage;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@Import({
        ItemServiceImpl.class,
        UserServiceImpl.class,
        SimpleItemMapperImpl.class,
        SimpleBookingMapperImpl.class,
        SimpleCommentMapperImpl.class,
        SimpleUserMapperImpl.class,
        ItemRequestServiceImpl.class,
        SimpleItemRequestMapperImpl.class
})
public class ItemRequestServiceIntegrationTest {
    @Autowired
    private ItemServiceImpl itemService;
    @Autowired
    private UserStorage userStorage;
    @Autowired
    private ItemStorage itemStorage;
    @Autowired
    private BookingStorage bookingStorage;
    @Autowired
    private CommentStorage commentStorage;
    @Autowired
    private SimpleUserMapperImpl simpleUserMapper;
    @Autowired
    private ItemRequestServiceImpl itemRequestService;
    @Autowired
    private SimpleItemRequestMapper itemRequestMapper;
    @Autowired
    ItemRequestStorage itemRequestStorage;

    private User user;

    @BeforeEach
    void beforeEach() {
        user = new User();
        user.setName("Андрей");
        user.setEmail("some@mail.ru");
        user = userStorage.save(user);
    }

    @Test
    @DisplayName("Запрос создается")
    void shouldRequestCreate() {
        ItemRequest request = new ItemRequest();
        request.setRequester(user);
        request.setDescription("Срочно нужна машина");
        request.setCreated(LocalDateTime.now());

        ItemRequest result = itemRequestStorage.save(request);

        assertNotNull(result);
        assertEquals(request.getDescription(), result.getDescription());
        assertEquals(request.getRequester(), result.getRequester());
        assertEquals(request.getCreated(), result.getCreated());
    }
}
