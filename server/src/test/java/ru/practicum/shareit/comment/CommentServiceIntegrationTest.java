package ru.practicum.shareit.comment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.booking.mapper.SimpleBookingMapperImpl;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.comment.mapper.SimpleCommentMapperImpl;
import ru.practicum.shareit.comment.storage.CommentStorage;
import ru.practicum.shareit.item.mapper.SimpleItemMapperImpl;
import ru.practicum.shareit.item.model.Item;
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
@ActiveProfiles("test")
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
public class CommentServiceIntegrationTest {

    @Autowired
    ItemRequestStorage itemRequestStorage;
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
    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void beforeEach() {
        owner = new User();
        owner.setName("Андрей");
        owner.setEmail("some@mail.ru");
        owner = userStorage.save(owner);

        booker = new User();
        booker.setName("Фома");
        booker.setEmail("some1@mail.ru");
        booker = userStorage.save(booker);

        item = new Item();
        item.setName("Холодильник");
        item.setDescription("Стильный желтый холодильник");
        item.setAvailable(true);
        item.setOwner(owner);
        item = itemStorage.save(item);

        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(LocalDateTime.now().minusDays(10));
        booking.setEnd(LocalDateTime.now().minusDays(5));
        booking.setStatus(BookingStatus.APPROVED);
        booking = bookingStorage.save(booking);
    }

    @DisplayName("Отзыв создается")
    @Test
    void shouldCreateRequest() {
        ItemRequest request = new ItemRequest();
        request.setRequester(booker);
        request.setCreated(LocalDateTime.now());
        request.setDescription("Норм вещь");

        ItemRequest result = itemRequestStorage.save(request);

        assertNotNull(result.getId());
        assertEquals(request.getRequester(), request.getRequester());
        assertEquals(request.getCreated(), result.getCreated());
        assertEquals(request.getDescription(), result.getDescription());
    }


}
