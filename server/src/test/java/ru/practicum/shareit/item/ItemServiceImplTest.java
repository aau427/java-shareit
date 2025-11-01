package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.BaseUtility;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.model.LastAndNextBookings;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.comment.storage.CommentStorage;
import ru.practicum.shareit.exceptions.CustomValidationException;
import ru.practicum.shareit.exceptions.ResourceNotFoundException;
import ru.practicum.shareit.exceptions.RightsException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOutDtoWithDates;
import ru.practicum.shareit.item.mapper.SimpleItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest extends BaseUtility {

    @Mock
    private ItemStorage itemStorage;

    @Mock
    private UserService userService;

    @Mock
    private BookingStorage bookingStorage;

    @Mock
    private CommentStorage commentStorage;

    @Mock
    private ItemRequestService itemRequestService;

    @Mock
    private SimpleItemMapper itemMapper;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User owner;
    private ItemDto itemDto;
    private Item item;

    @BeforeEach
    void beforeEach() {
        owner = createUser(1, "Андрей", "some@mail.ru");
        item = createItem(1, "Носок", "Носок штопанный перештопанный", true, owner, null);
        itemDto = createItemDto(null, "Носок", "Носок штопанный перештопанный", true, owner.getId(), null);
    }

    @DisplayName("Item успешно создается")
    @Test
    void shouldCreateItem() {
        when(userService.getUserById(anyInt())).thenReturn(owner);
        when(itemStorage.save(any(Item.class))).thenReturn(item);
        when(itemMapper.dtoToItem(itemDto, itemRequestService)).thenReturn(item);
        when(itemMapper.itemToDto(item)).thenReturn(itemDto);

        ItemDto createdDto = itemService.createItem(itemDto);

        assertNotNull(createdDto, "Item не создался");
        assertEquals(itemDto.getName(), createdDto.getName(), "Неправильное имя");
        assertEquals(itemDto.getDescription(), createdDto.getDescription(), "Неправильное описание");
        assertEquals(itemDto.getAvailable(), createdDto.getAvailable(), "Неправильная доступность");
        assertEquals(itemDto.getOwner(), createdDto.getOwner(), "Неправильный владелец");
        verify(itemStorage, times(1)).save(item);
    }

    @DisplayName("Нельзя создать item с указанным ID")
    @Test
    void shouldNotCreateItem_WithId() {
        itemDto.setId(1);

        assertThrows(CustomValidationException.class, () -> itemService.createItem(itemDto));
        verify(itemStorage, never()).save(any(Item.class));
    }

    @DisplayName("Нельзя создать item для несуществующего пользователя")
    @Test
    void shouldNotCreateItem_WithNotFoundUser() {
        when(userService.getUserById(itemDto.getOwner())).thenThrow(new ResourceNotFoundException("Нет такого пользователя"));

        assertThrows(ResourceNotFoundException.class, () -> itemService.createItem(itemDto));
        verify(itemStorage, never()).save(any(Item.class));
    }

    @DisplayName("Меняем имя и описание")
    @Test
    void shouldUpdateItem_NameAndDescription() {
        item.setId(1);
        itemDto.setId(1);

        Item newItem = createItem(1, "Носок измененный", "Модернизированный носок", false, owner, null);
        ItemDto expectedDto = createItemDto(1, "Носок измененный", "Модернизированный носок", false, owner.getId(), null);


        when(itemStorage.findById(anyInt())).thenReturn(Optional.of(item));
        when(itemMapper.dtoToItem(any(ItemDto.class), any(ItemRequestService.class))).thenReturn(newItem);
        when(itemStorage.save(any(Item.class))).thenReturn(newItem);
        when(itemMapper.itemToDto(any(Item.class))).thenReturn(expectedDto);

        ItemDto result = itemService.updateItem(itemDto);

        assertNotNull(result);
        assertEquals("Носок измененный", result.getName());
        assertEquals("Модернизированный носок", result.getDescription());
        assertEquals(false, result.getAvailable());
        verify(itemStorage, times(1)).save(any(Item.class));
    }

    @DisplayName("Нельзя поменять несуществующий Item")
    @Test
    void shouldNotUpdateItemNotFound() {
        item.setId(666);
        itemDto.setId(666);

        when(itemStorage.findById(anyInt())).thenThrow(new ResourceNotFoundException("Ну нет его"));

        assertThrows(ResourceNotFoundException.class, () -> itemService.updateItem(itemDto),
                "Не выброшено исключение ResourceNotFoundException");
        verify(itemStorage, never()).save(any(Item.class));
    }

    @DisplayName("Нельзя поменять Item, когда меняется его владелец")
    @Test
    void shouldNotUpdateItemsOwner() {
        User otherUser = createUser(2, "Анастасия", "some@uandex.ru");
        Item existingItem = item;
        existingItem.setId(1);

        Item newItem = createItem(1, "Какое-то", "какой-то", true, otherUser, null); // Якобы обновляет otherUser (ID 2)
        ItemDto updateDto = createItemDto(1, "Имя", "Описание", true, otherUser.getId(), null);

        when(itemStorage.findById(anyInt())).thenReturn(Optional.of(existingItem));
        when(itemMapper.dtoToItem(any(ItemDto.class), any(ItemRequestService.class))).thenReturn(newItem);

        assertThrows(RightsException.class, () -> itemService.updateItem(updateDto), "Не выбросил исключение");
        verify(itemStorage, never()).save(any(Item.class));
    }


    @DisplayName("Получение вещи по ее ID")
    @Test
    void shouldReturnItemIfExists() {
        item.setId(1);
        when(itemStorage.findById(anyInt())).thenReturn(Optional.of(item));

        Item result = itemService.getItemById(item.getId());

        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
    }

    @DisplayName("Получение вещи по ее ID выбрасывает исключение, если вещь отсутствует")
    @Test
    void shouldNotReturnItemIfNotExists() {
        item.setId(666);

        when(itemStorage.findById(666)).thenThrow(new ResourceNotFoundException(""));


        assertThrows(ResourceNotFoundException.class, () -> itemService.getItemById(item.getId()),
                "не выбрасывается исключение ResourceNotFoundException");
        verify(itemStorage, never()).save(any(Item.class));
    }

    @DisplayName("Получение Item с датами бронирования")
    @Test
    void getItemDtoById() {
        item.setId(1);
        LocalDateTime startLastBooking = LocalDateTime.now().minusDays(10);
        LocalDateTime endLastBooking = LocalDateTime.now().minusDays(7);
        LocalDateTime startNextBooking = LocalDateTime.now().plusDays(1);
        LocalDateTime endNextBooking = LocalDateTime.now().plusDays(2);
        User booker1 = createUser(2, "Петр", "petr@mail.ru");
        User booker2 = createUser(3, "Фома", "foma@mail.ru");
        Booking lastBooking = createBooking(1, booker1, startLastBooking, endLastBooking, BookingStatus.APPROVED);
        Booking nextBooking = createBooking(2, booker2, startNextBooking, endNextBooking, BookingStatus.APPROVED);
        LastAndNextBookings twoBookings = new LastAndNextBookings();
        twoBookings.setLastBooking(lastBooking);
        twoBookings.setNextBooking(nextBooking);
        ItemOutDtoWithDates expectedDto = createItemDtoWithDates(1, item, lastBooking, nextBooking);

        when(itemStorage.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingStorage.getBookingsByItemOwner(item.getId(), item.getOwner().getId()))
                .thenReturn(List.of(lastBooking, nextBooking));
        when(commentStorage.findAllByItemInOrderByCreatedDesc(anyList())).thenReturn(Collections.emptyList());
        when(itemMapper.toItemOutDtoWithDate(item, twoBookings, Collections.emptyList())).thenReturn(expectedDto);

        ItemOutDtoWithDates result = itemService.getItemDtoById(item.getId(), item.getOwner().getId());

        assertNotNull(result);
        assertEquals(expectedDto, result);
    }

    @DisplayName("Получение ItemOutDtoById только с last")
    @Test
    void getItemDtoById_onlyLastBooking() {
        item.setId(1);
        LocalDateTime startLastBooking = LocalDateTime.now().minusDays(10);
        LocalDateTime endLastBooking = LocalDateTime.now().minusDays(7);
        User booker1 = createUser(2, "Петр", "petr@mail.ru");
        Booking lastBooking = createBooking(1, booker1, startLastBooking, endLastBooking, BookingStatus.APPROVED);
        LastAndNextBookings twoBookings = new LastAndNextBookings();
        twoBookings.setLastBooking(lastBooking);
        ItemOutDtoWithDates expectedDto = createItemDtoWithDates(1, item, lastBooking, null);

        when(itemStorage.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingStorage.getBookingsByItemOwner(item.getId(), item.getOwner().getId()))
                .thenReturn(List.of(lastBooking));
        when(commentStorage.findAllByItemInOrderByCreatedDesc(anyList())).thenReturn(Collections.emptyList());
        when(itemMapper.toItemOutDtoWithDate(item, twoBookings, Collections.emptyList())).thenReturn(expectedDto);

        ItemOutDtoWithDates result = itemService.getItemDtoById(item.getId(), item.getOwner().getId());

        assertNotNull(result);
        assertEquals(expectedDto, result);
    }

    @DisplayName("Получение Item c пустым списком бронирований")
    @Test
    void getItemDtoById_emptyBookings() {
        item.setId(1);
        LastAndNextBookings twoBookings = new LastAndNextBookings();
        ItemOutDtoWithDates expectedDto = createItemDtoWithDates(1, item, null, null);

        when(itemStorage.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingStorage.getBookingsByItemOwner(item.getId(), item.getOwner().getId()))
                .thenReturn(Collections.emptyList());
        when(commentStorage.findAllByItemInOrderByCreatedDesc(anyList())).thenReturn(Collections.emptyList());
        when(itemMapper.toItemOutDtoWithDate(item, twoBookings, Collections.emptyList())).thenReturn(expectedDto);

        ItemOutDtoWithDates result = itemService.getItemDtoById(item.getId(), item.getOwner().getId());

        assertNotNull(result);
        assertEquals(expectedDto, result);
    }

    @DisplayName("Если Item не найден, то выбрасывается исключение")
    @Test
    void getItemDtoByIdWhenItemNotFound() {
        item.setId(1);
        LastAndNextBookings twoBookings = new LastAndNextBookings();
        ItemOutDtoWithDates expectedDto = createItemDtoWithDates(1, item, null, null);

        when(itemStorage.findById(item.getId())).thenThrow(new ResourceNotFoundException("Не найден"));

        assertThrows(ResourceNotFoundException.class, () -> itemService.getItemDtoById(item.getId(), item.getOwner().getId()),
                "не выбрасывается исключение ResourceNotFoundException");
    }

    @DisplayName("Поиск вещей по контексту")
    @Test
    void getItemsByContext() {
        List<Item> foundItems = List.of(item);
        List<ItemDto> expectedDtos = List.of(itemDto);
        String context = "носок";

        when(itemStorage.contextSearch(context)).thenReturn(foundItems);
        when(itemMapper.itemToDto(item)).thenReturn(itemDto);

        List<ItemDto> result = itemService.getItemsByContext(context);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(expectedDtos.size(), result.size());
        verify(itemStorage, times(1)).contextSearch(context);
    }

    @DisplayName("Поиск вещей по пустому контексту возвращает пустой список")
    @Test
    void getItemsByContext_WithEmptyText() {
        List<ItemDto> result = itemService.getItemsByContext("");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(itemStorage, never()).contextSearch(anyString());
    }

    @DisplayName("Поиск вещей по контекту с одними пробелам")
    @Test
    void getItemsByContext_WithBlancText() {
        List<ItemDto> result = itemService.getItemsByContext("    ");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(itemStorage, never()).contextSearch(anyString());
    }

    @DisplayName("Возвращает список вещей с законченными бронированиями")
    @Test
    void getItemsWasCompleteBookingByUser_whenExist() {
        item.setId(1);
        LocalDateTime localDateTime = LocalDateTime.now();

        when(itemStorage.getItemsWasCompleteBookingByUser(1, 1, localDateTime))
                .thenReturn(List.of(item));

        List<Item> result = itemService.getItemsWasCompleteBookingByUser(1, 1, localDateTime);

        assertNotNull(result);
        verify(itemStorage, times(1)).getItemsWasCompleteBookingByUser(anyInt(), anyInt(), any(LocalDateTime.class));
    }
}