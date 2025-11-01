package ru.practicum.shareit.booking;

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
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOutDtoWithDates;
import ru.practicum.shareit.item.mapper.SimpleItemMapperImpl;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.request.mapper.SimpleItemRequestMapper;
import ru.practicum.shareit.request.mapper.SimpleItemRequestMapperImpl;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
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
public class BookingServiceIntegrationTest {
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
    private ItemDto itemDto;

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

        itemDto = ItemDto.builder()
                .name("Холодильник")
                .description("Стильный желтый холодильник")
                .available(true)
                .owner(owner.getId())
                .build();
    }

    @DisplayName("Бронирование создается")
    @Test
    void shouldBookingCreate() {
        ItemDto createdItemDto = itemService.createItem(itemDto);
        Integer itemId = createdItemDto.getId();
        Item savedItem = itemStorage.findById(itemId).get();

        LocalDateTime now = LocalDateTime.now();
        Booking booking = createBooking(savedItem, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1),
                booker);

        Booking result = bookingStorage.save(booking);

        assertNotNull(result);
        assertEquals(savedItem.getId(), result.getItem().getId());
    }

    private Booking createBooking(Item item, LocalDateTime start, LocalDateTime end, User booker) {
        Booking booking = new Booking();
        booking.setStart(start);
        booking.setEnd(end);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.APPROVED);
        return booking;
    }

}
