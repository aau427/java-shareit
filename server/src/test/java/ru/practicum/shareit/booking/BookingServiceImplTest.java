package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.BaseUtility;
import ru.practicum.shareit.booking.dto.InputBookingDto;
import ru.practicum.shareit.booking.dto.OutputBookingDto;
import ru.practicum.shareit.booking.mapper.SimpleBookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.booking.strategy.BookingFindStrategy;
import ru.practicum.shareit.booking.strategy.BookingStrategyFactory;
import ru.practicum.shareit.booking.strategy.FindBookingStateEnum;
import ru.practicum.shareit.exceptions.LogicalException;
import ru.practicum.shareit.exceptions.ResourceNotFoundException;
import ru.practicum.shareit.exceptions.RightsException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.SimpleItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest extends BaseUtility {

    @Mock
    private BookingStorage bookingStorage;
    @Mock
    private SimpleBookingMapper bookingMapper;
    @Mock
    private UserService userService;
    @Mock
    private ItemService itemService;
    @Mock
    private SimpleItemMapper itemMapper;
    @Mock
    private BookingStrategyFactory strategyFactory;
    @Mock
    private BookingFindStrategy bookingFindStrategy; // Мок стратегии для использования в тестах

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User owner;
    private User booker;
    private UserDto bookerDto;
    private Item item;
    private ItemDto itemDto;
    private Booking inputBooking;
    private Booking outBooking;
    private InputBookingDto inputDto;
    private OutputBookingDto outputDto;
    private LocalDateTime start;
    private LocalDateTime end;

    @BeforeEach
    void beforeEach() {
        owner = createUser(1, "Геннадий", "some@mail.ru");
        booker = createUser(2, "Анастасия", "so@example.com");
        bookerDto = createUserDto(2, "Анастасия", "so@example.com");
        item = createItem(1, "кофемашина", "просто супер кофе, даже без зерен",
                true, owner, null);
        itemDto = createItemDto(1, "кофемашина", "просто супер кофе, даже без зерен",
                true, owner.getId(), null);
        start = LocalDateTime.now().plusDays(1);
        end = LocalDateTime.now().plusDays(6);
        inputBooking = createBooking(null, booker, item, start, end, BookingStatus.WAITING);
        outBooking = createBooking(1, booker, item, start, end, BookingStatus.WAITING);
        inputDto = createInputBookingDto(item, booker, start, end);
        outputDto = createOutputBookingDto(1, itemDto, bookerDto, start, end, BookingStatus.WAITING);
    }

    @DisplayName("Бронирование создается!")
    @Test
    void shouldBookingCreateSuccessfully() {
        when(bookingMapper.dtoToBooking(inputDto, itemService)).thenReturn(inputBooking);
        when(bookingStorage.save(inputBooking)).thenReturn(outBooking);
        when(bookingMapper.bookingToDto(outBooking, itemMapper)).thenReturn(outputDto);

        OutputBookingDto result = bookingService.createBooking(inputDto);

        assertNotNull(result);
        verify(bookingStorage, times(1)).save(inputBooking);
        assertEquals(BookingStatus.WAITING, result.getStatus());
    }

    @DisplayName("Ошибка при создании бронирования: дата начала позже даты окончания")
    @Test
    void shouldThrowException_WhenBookingStartAfterEnd() {
        start = LocalDateTime.now().plusDays(10);
        end = LocalDateTime.now().plusDays(5);
        inputDto.setStart(start);
        inputDto.setEnd(end);
        inputBooking.setStart(start);
        inputBooking.setEnd(end);

        when(bookingMapper.dtoToBooking(inputDto, itemService)).thenReturn(inputBooking);

        assertThrows(LogicalException.class, () -> bookingService.createBooking(inputDto));
        verify(bookingStorage, never()).save(inputBooking);
    }

    @DisplayName("Ошибка при создании бронирования: владелец бронирует свою вещь")
    @Test
    void shouldThrowException_WhenOwnerEqvBooker() {
        inputBooking.setBooker(owner);
        inputDto.setBookerId(owner.getId());

        when(bookingMapper.dtoToBooking(inputDto, itemService)).thenReturn(inputBooking);

        assertThrows(LogicalException.class, () -> bookingService.createBooking(inputDto));
        verify(bookingStorage, never()).save(any(Booking.class));
    }

    @DisplayName("Ошибка при создании бронирования: вещь недоступна")
    @Test
    void createBooking_ShouldThrowException_WhenItemNotAvailable() {
        item.setAvailable(false);
        when(bookingMapper.dtoToBooking(inputDto, itemService)).thenReturn(inputBooking);

        assertThrows(LogicalException.class, () -> bookingService.createBooking(inputDto));
        verify(bookingStorage, never()).save(any(Booking.class));
    }


    @DisplayName("Бронирование успешно подтверждается")
    @Test
    void shouldApproveBookingSuccess() {
        OutputBookingDto bookingApproveDto = createOutputBookingDto(1, itemDto, bookerDto,
                start, end, BookingStatus.APPROVED);
        Booking bookingApproved = createBooking(1, booker, item, start, end, BookingStatus.APPROVED);
        when(bookingStorage.findById(outBooking.getId())).thenReturn(Optional.of(outBooking));
        when(bookingStorage.save(any(Booking.class))).thenReturn(bookingApproved);
        when(bookingMapper.bookingToDto(bookingApproved, itemMapper)).thenReturn(bookingApproveDto);

        OutputBookingDto result = bookingService.updateBooking(owner.getId(), inputBooking.getId(), true);

        assertNotNull(result);
        assertEquals(BookingStatus.APPROVED, result.getStatus());
        verify(bookingStorage, times(1)).save(bookingApproved);
    }

    @DisplayName("Выбрасывается исключение, если бронирование подтверждает не владелец вещи")
    @Test
    void shouldThrowRightsException_WhenUserIsNotOwner() {


        when(bookingStorage.findById(outBooking.getId())).thenReturn(Optional.of(outBooking));

        assertThrows(RightsException.class, () -> bookingService.updateBooking(booker.getId(), outBooking.getId(), true));
        verify(bookingStorage, never()).save(any(Booking.class));
    }

    @DisplayName("Владелец вещи может отклонить бронирование")
    @Test
    void shouldRejectBooking() {
        OutputBookingDto bookingApproveDto = createOutputBookingDto(1, itemDto, bookerDto,
                start, end, BookingStatus.REJECTED);
        Booking bookingApproved = createBooking(1, booker, item, start, end, BookingStatus.APPROVED);
        when(bookingStorage.findById(outBooking.getId())).thenReturn(Optional.of(outBooking));
        when(bookingStorage.save(any(Booking.class))).thenReturn(bookingApproved);
        when(bookingMapper.bookingToDto(bookingApproved, itemMapper)).thenReturn(bookingApproveDto);

        OutputBookingDto result = bookingService.updateBooking(owner.getId(), inputBooking.getId(), false);

        assertNotNull(result);
        assertEquals(BookingStatus.REJECTED, result.getStatus());
        verify(bookingStorage, times(1)).save(bookingApproved);
    }

    @DisplayName("Выбрасывается исключение, если бронирование отклоняет не владелец вещи")
    @Test
    void shouldThrowRightsException_WhenUserIsNotOwnerRejecting() {
        outBooking.setStatus(BookingStatus.REJECTED);

        when(bookingStorage.findById(outBooking.getId())).thenReturn(Optional.of(outBooking));

        assertThrows(RightsException.class, () -> bookingService.updateBooking(booker.getId(), outBooking.getId(), false));
        verify(bookingStorage, never()).save(any(Booking.class));
    }

    @DisplayName("Автор бронирование может получить свое бронирование")
    @Test
    void shouldReturnBookingForBooker_getBookingById_() {
        when(bookingStorage.findById(outBooking.getId())).thenReturn(Optional.of(outBooking));
        when(bookingMapper.bookingToDto(outBooking, itemMapper)).thenReturn(outputDto);

        OutputBookingDto result = bookingService.getBookingById(outBooking.getId(), booker.getId());

        assertNotNull(result);
        verify(bookingStorage, times(1)).findById(outBooking.getId());
    }

    @DisplayName("Владелец вещи может получить бронирование")
    @Test
    void shouldReturnBookingForOwner_getBookingById() {
        when(bookingStorage.findById(outBooking.getId())).thenReturn(Optional.of(outBooking));
        when(bookingMapper.bookingToDto(outBooking, itemMapper)).thenReturn(outputDto);

        OutputBookingDto result = bookingService.getBookingById(outBooking.getId(), owner.getId());

        assertNotNull(result);
        verify(bookingStorage, times(1)).findById(outBooking.getId());
    }

    @DisplayName("выбрасываем исключение, если бронирование запрашивает левый чувак")
    @Test
    void shouldThrowRightsException_WhenUserIsNotOwnerOrBooker_getBookingById() {
        User randomUser = createUser(666, "random user", "random@mail.ru");
        when(bookingStorage.findById(anyInt())).thenReturn(Optional.of(outBooking));

        assertThrows(RightsException.class, () -> bookingService.getBookingById(outBooking.getId(), randomUser.getId()));
    }


    @DisplayName("Ошибка при получении бронирования: бронирование не найдено")
    @Test
    void shouldThrowResourceNotFoundException_getBookingById_() {
        when(bookingStorage.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookingService.getBookingById(666, booker.getId()));
    }


    @DisplayName("Пользователь может получить  список всех  бронирований (ALL)")
    @Test
    void shouldReturnAllUserBookings() {
        String state = "ALL";
        List<Booking> expectedBookings = List.of(outBooking);

        when(userService.getUserById(booker.getId())).thenReturn(booker);
        when(strategyFactory.getStrategyByState(FindBookingStateEnum.ALL_USERS)).thenReturn(bookingFindStrategy);
        when(bookingFindStrategy.findBooking(any(User.class))).thenReturn(expectedBookings);
        when(bookingMapper.bookingToDto(any(Booking.class), any(SimpleItemMapper.class))).thenReturn(outputDto);

        List<OutputBookingDto> result = bookingService.getUsersBooking(booker.getId(), state);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.getFirst().getId());
        verify(bookingFindStrategy, times(1)).findBooking(booker);
    }

    @DisplayName("Владелец может получить список бронирований, ожидающих подтверждения (WAITING)")
    @Test
    void shouldReturnAllOwnersBookings() {
        String state = "WAITING";
        List<Booking> expectedBookings = List.of(outBooking);

        when(userService.getUserById(anyInt())).thenReturn(owner);
        when(strategyFactory.getStrategyByState(eq(FindBookingStateEnum.WAITING_OWNERS))).thenReturn(bookingFindStrategy);
        when(bookingFindStrategy.findBooking(owner)).thenReturn(expectedBookings);
        when(bookingMapper.bookingToDto(outBooking, itemMapper)).thenReturn(outputDto);

        List<OutputBookingDto> result = bookingService.getOwnersBookings(owner.getId(), state);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookingFindStrategy, times(1)).findBooking(owner);
    }

    @DisplayName("Выбрасывается исключение, если владелец пытается получить список бронирований с левым STATE")
    @Test
    void shouldThrowExceptionWhenIncorrectSate() {
        List<Booking> expectedBookings = List.of(outBooking);
        String state = "CUSTOM";

        when(userService.getUserById(anyInt())).thenReturn(owner);

        assertThrows(ValidationException.class, () -> bookingService.getOwnersBookings(owner.getId(), state));
        verify(bookingFindStrategy, never()).findBooking(owner);
    }


}