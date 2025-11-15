package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.model.LastAndNextBookings;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.comment.storage.CommentStorage;
import ru.practicum.shareit.exceptions.CustomValidationException;
import ru.practicum.shareit.exceptions.ResourceNotFoundException;
import ru.practicum.shareit.exceptions.RightsException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOutDtoWithDates;
import ru.practicum.shareit.item.mapper.SimpleItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserService userService;
    private final SimpleItemMapper itemMapper;
    private final BookingStorage bookingStorage;
    private final CommentStorage commentStorage;
    private final ItemRequestService itemRequestService;

    @Transactional
    @Override
    public ItemDto createItem(ItemDto itemDto) {
        if (itemDto.getId() != null) {
            log.error("Ошибка при создании вещи: указан Id = {}", itemDto.getId());
            throw new CustomValidationException(String.format("Ошибка при создании вещи: указан Id = %d", itemDto.getId()));
        }
        Item item = itemMapper.dtoToItem(itemDto, itemRequestService);
        item.setOwner(userService.getUserById(itemDto.getOwner()));
        return itemMapper.itemToDto(itemStorage.save(item));
    }

    @Transactional
    @Override
    public ItemDto updateItem(ItemDto itemDto) {
        Item itemForUpdate = getItemById(itemDto.getId());
        Item newItem = itemMapper.dtoToItem(itemDto, itemRequestService);
        //отредактировать вещь может только ее владелец
        checkOwner(newItem.getOwner(), itemForUpdate.getOwner());
        /* Отредактировать можно только название, комментарий и доступность.
           Причем во вх. DTO в наличии только те поля, которые обновляются */
        if (newItem.getName() != null) {
            itemForUpdate.setName(newItem.getName());
        }
        if (newItem.getDescription() != null) {
            itemForUpdate.setDescription(newItem.getDescription());
        }
        if (newItem.getAvailable() != null) {
            itemForUpdate.setAvailable(newItem.getAvailable());
        }
        return itemMapper.itemToDto(itemStorage.save(itemForUpdate));
    }

    @Override
    public ItemOutDtoWithDates getItemDtoById(Integer itemId, Integer userId) {
        Item item = getItemById(itemId);
        List<Booking> bookingList = bookingStorage.getBookingsByItemOwner(itemId, userId);
        LastAndNextBookings twoBookings = getLastAndNextBookings(bookingList);
        List<Comment> commentList = commentStorage.findAllByItemInOrderByCreatedDesc(List.of(item));
        return itemMapper.toItemOutDtoWithDate(item, twoBookings, commentList);
    }

    @Override
    public List<ItemOutDtoWithDates> getUsersItems(Integer userId) {
        List<Item> itemList = itemStorage.findAllByOwner(userService.getUserById(userId));
        Map<Integer, List<Booking>> bookingMap = getBookingMap(itemList);
        Map<Integer, List<Comment>> commentMap = getCommentsMap(itemList);
        return itemList.stream()
                .map(item -> {
                    List<Comment> commentList = commentMap.get(item.getId());
                    if (commentList == null) {
                        commentList = new ArrayList<>();
                    }
                    List<Booking> bookingList = bookingMap.get(item.getId());
                    if (bookingList == null) {
                        bookingList = new ArrayList<>();
                    }
                    LastAndNextBookings twoBookings = getLastAndNextBookings(bookingList);
                    return itemMapper.toItemOutDtoWithDate(item, twoBookings, commentList);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> getItemsByContext(String context) {
        if (context.isEmpty() || context.isBlank()) {
            return new ArrayList<>();
        }

        return itemStorage.contextSearch(context.toUpperCase()).stream()
                .map(itemMapper::itemToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> getItemsWasCompleteBookingByUser(Integer itemId, Integer userId, LocalDateTime created) {
        return itemStorage.getItemsWasCompleteBookingByUser(itemId, userId, created);
    }

    @Override
    public Item getItemById(int itemId) {
        return itemStorage.findById(itemId).orElseThrow(() -> {
            log.error("Не нашел вещь с Id = {}", itemId);
            throw new ResourceNotFoundException(String.format("Не нашел вещь с Id = %d", itemId));
        });
    }

    private void checkOwner(User newOwner, User oldOwner) {
        if (!newOwner.getId().equals(oldOwner.getId())) {
            throw new RightsException("Редактировать вещь может только ее владелец");
        }
    }

    private LastAndNextBookings getLastAndNextBookings(List<Booking> bookingList) {
        /*
        Букинги на входе отсортированы в порядке возрастания даты начала....
        Последнее бронирование таково, что началось в самом ближайшем прошлом.
         А следующее, которое начнется в самом ближайшем будущем.
         */
        LastAndNextBookings twoBookings = new LastAndNextBookings();
        LocalDateTime now = LocalDateTime.now();
        if (bookingList.isEmpty()) {
            twoBookings.setLastBooking(null);
            twoBookings.setNextBooking(null);
        } else {
            for (Booking booking : bookingList) {
                if (booking.getStart().isBefore(now)) {
                    twoBookings.setLastBooking(booking);
                }
                if (booking.getStart().isAfter(now)) {
                    twoBookings.setNextBooking(booking);
                    break;
                }
            }
        }
        return twoBookings;
    }

    private Map<Integer, List<Booking>> getBookingMap(List<Item> itemList) {
        List<Booking> bookingList = bookingStorage.findAllByItemInAndStatusOrderByStart(itemList, BookingStatus.APPROVED);

        return bookingList.stream()
                .collect(Collectors.groupingBy(booking -> booking.getItem().getId()));
    }

    private Map<Integer, List<Comment>> getCommentsMap(List<Item> itemList) {
        List<Comment> commentList = commentStorage.findAllByItemInOrderByCreatedDesc(itemList);
        return commentList.stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));
    }
}
