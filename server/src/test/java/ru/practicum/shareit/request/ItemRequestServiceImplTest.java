package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.BaseUtility;
import ru.practicum.shareit.exceptions.ResourceNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithItems;
import ru.practicum.shareit.request.dto.ItemRequestInDto;
import ru.practicum.shareit.request.dto.ItemRequestOutDto;
import ru.practicum.shareit.request.mapper.SimpleItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.request.storage.ItemRequestStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest extends BaseUtility {
    @Mock
    private ItemRequestStorage itemRequestStorage;
    @Mock
    private ItemStorage itemStorage;
    @Mock
    private
    SimpleItemRequestMapper itemRequestMapper;
    @Mock
    private UserService userService;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    private User user;
    private User user1;
    private ItemRequestInDto requestInDto;
    private ItemRequest itemRequest;
    private ItemRequestOutDto requestOutDto;
    private LocalDateTime created;
    private Item item;

    @BeforeEach
    void beforeEach() {
        user = createUser(1, "Andrey", "some@mail.ru");
        user1 = createUser(2, "Foma", "some1@mail.ru");
        requestInDto = createItemRequestInDto(user, "Необходим игровой ноутбук");
        created = LocalDateTime.now();
        itemRequest = createItemRequest(1, user, "Необходиiм игровой ноутбук", created);
        requestOutDto = createItemRequestOutDto(1, "Необходим игровой ноутбук", created);
        item = createItem(1, "Ноутбук",
                "Ноутбук старенький, но хороший", true, user1, itemRequest);
    }

    @DisplayName("Запрос успешно создается")
    @Test
    void shouldRequestCreate() {
        when(itemRequestMapper.dtoToItemRequest(requestInDto)).thenReturn(itemRequest);
        when(itemRequestStorage.save(itemRequest)).thenReturn(itemRequest);
        when(itemRequestMapper.itemRequestToDto(itemRequest)).thenReturn(requestOutDto);

        ItemRequestOutDto result = itemRequestService.createRequest(requestInDto);

        assertNotNull(result);
        assertEquals(requestOutDto.getId(), result.getId());
        verify(itemRequestStorage, times(1)).save(itemRequest);
    }


    @DisplayName("Пользователь может получить список своих запросов вместе с ответами на них")
    @Test
    void ShouldReturnRequestsWithItems_getRequestByUserId() {
        List<ItemRequest> requestList = List.of(itemRequest);
        List<Item> itemList = List.of(item);
        ItemRequestDtoWithItems dtoWithItems = createItemRequestDtoWithItems(itemRequest, itemList);

        when(userService.getUserById(user.getId())).thenReturn(user);
        when(itemRequestStorage.findAllByRequesterOrderByCreatedDesc(user)).thenReturn(requestList);
        when(itemStorage.findAllByRequestInOrderById(anyList())).thenReturn(itemList);
        when(itemRequestMapper.itemToItemRequestWithItems(any(ItemRequest.class), anyList())).thenReturn(dtoWithItems);

        List<ItemRequestDtoWithItems> result = itemRequestService.getRequestByUserId(user.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(itemRequestStorage, times(1)).findAllByRequesterOrderByCreatedDesc(user);
    }

    @DisplayName("Получить запросы других пользователей")
    @Test
    void ShouldReturnAllRequestsWithItems_getAllRequests() {
        List<ItemRequest> requestList = List.of(itemRequest);
        List<Item> itemList = List.of(item);
        ItemRequestDtoWithItems dtoWithItems = createItemRequestDtoWithItems(itemRequest, itemList);

        when(itemRequestStorage.findAll(any(Sort.class))).thenReturn(requestList);
        when(itemStorage.findAllByRequestInOrderById(anyList())).thenReturn(itemList);
        when(itemRequestMapper.itemToItemRequestWithItems(any(ItemRequest.class), anyList())).thenReturn(dtoWithItems);

        List<ItemRequestDtoWithItems> result = itemRequestService.getAllRequests();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(itemRequestStorage, times(1)).findAll(any(Sort.class));
    }

    @DisplayName("Получить запрос по ID")
    @Test
    void shouldReturnItemRequest_getRequestById() {
        when(itemRequestStorage.findById(itemRequest.getId())).thenReturn(Optional.of(itemRequest));

        ItemRequest result = itemRequestService.getRequestById(itemRequest.getId());

        assertNotNull(result);
        assertEquals(itemRequest.getId(), result.getId());
    }

    @DisplayName("Выбрасываем исключение, если запрос не найден")
    @Test
    void shouldThrowExceptionWhenRequestNotFound_getRequestById() {
        when(itemRequestStorage.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> itemRequestService.getRequestById(666));
    }

    @DisplayName("Получение DTO запроса по его ID")
    @Test
    void shouldReturnRequestWithItems_getRequestDtoById() {
        List<Item> itemsList = List.of(item);
        ItemRequestDtoWithItems expectedDto = createItemRequestDtoWithItems(itemRequest, itemsList);

        when(itemRequestStorage.findById(anyInt())).thenReturn(Optional.of(itemRequest));
        when(itemStorage.findAllByRequestOrderById(any(ItemRequest.class))).thenReturn(itemsList);
        when(itemRequestMapper.itemToItemRequestWithItems(any(ItemRequest.class), anyList())).thenReturn(expectedDto);

        ItemRequestDtoWithItems result = itemRequestService.getRequestDtoById(itemRequest.getId());

        assertNotNull(result);
        assertEquals(expectedDto, result);
        verify(itemStorage, times(1)).findAllByRequestOrderById(itemRequest);
    }
}