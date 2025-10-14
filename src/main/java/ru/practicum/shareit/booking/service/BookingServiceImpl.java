package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.InputBookingDto;
import ru.practicum.shareit.booking.dto.OutputBookingDto;
import ru.practicum.shareit.booking.mapper.SimpleBookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.booking.strategy.BookingFindStrategy;
import ru.practicum.shareit.booking.strategy.BookingStrategyFactory;
import ru.practicum.shareit.booking.strategy.FindBookingStateEnum;
import ru.practicum.shareit.booking.strategy.FindBookingsManager;
import ru.practicum.shareit.common.Common;
import ru.practicum.shareit.exceptions.LogicalException;
import ru.practicum.shareit.exceptions.ResourceNotFoundException;
import ru.practicum.shareit.exceptions.RightsException;
import ru.practicum.shareit.item.mapper.SimpleItemMapper;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingStorage bookingStorage;
    private final SimpleBookingMapper bookingMapper;
    private final UserService userService;
    private final ItemService itemService;
    private final SimpleItemMapper itemMapper;
    private final BookingStrategyFactory strategyFactory;

    @Transactional
    @Override
    public OutputBookingDto createBooking(InputBookingDto bookingDto) {
        Booking booking = bookingMapper.dtoToBooking(bookingDto, itemService);
        booking.setStatus(Common.DEFAULT_BOOKING_STATUS);
        checkBookingBeforeCreate(booking);
        return bookingMapper.bookingToDto(bookingStorage.save(booking), itemMapper);
    }

    @Transactional
    @Override
    public OutputBookingDto updateBooking(Integer userId, Integer bookingId, Boolean isApprove) {
        Booking booking = getBookingById(bookingId);
        checkRightsForUpdate(booking, userId);
        if (isApprove) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }
        return bookingMapper.bookingToDto(bookingStorage.save(booking), itemMapper);
    }

    private void checkBookingBeforeCreate(Booking booking) {
        if (booking.getStart().isAfter(booking.getEnd())) {
            throw new LogicalException("Дата начала позже даты окончания");
        } else if (booking.getStart().equals(booking.getEnd())) {
            throw new LogicalException("Начало бронирования не может быть равно его окончанию!");
        }
        if (Objects.equals(booking.getBooker().getId(), booking.getItem().getOwner().getId())) {
            throw new LogicalException("Владелец не может сам у себя забронировать. Это глупо");
        }
        if (!booking.getItem().getAvailable()) {
            throw new LogicalException("Вещь недоступна для бронирования");
        }
    }

    @Override
    public OutputBookingDto getBookingById(Integer bookingId, Integer userId) {
        Booking booking = getBookingById(bookingId);
        checkRightsForGet(booking, userId);
        return bookingMapper.bookingToDto(booking, itemMapper);
    }

    @Override
    public List<OutputBookingDto> getUsersBooking(Integer userId, String state) {
        String newSate = state + "_USERS";
        BookingFindStrategy strategy = strategyFactory.getStrategyByState(FindBookingStateEnum.parse(newSate));
        FindBookingsManager findBookings = new FindBookingsManager();
        findBookings.setStrategy(strategy);
        return findBookings.findBookings(userService.getUserById(userId))
                .stream()
                .map(booking -> bookingMapper.bookingToDto(booking, itemMapper))
                .collect(Collectors.toList());
    }

    @Override
    public List<OutputBookingDto> getOwnersBookings(Integer userId, String state) {
        User user = userService.getUserById(userId);
        String newSate = state + "_OWNERS";
        BookingFindStrategy strategy = strategyFactory.getStrategyByState(FindBookingStateEnum.parse(newSate));
        strategy.setBookingStorage(bookingStorage);
        FindBookingsManager findBookings = new FindBookingsManager();

        return findBookings.findBookings(user)
                .stream().map(booking -> bookingMapper.bookingToDto(booking, itemMapper))
                .collect(Collectors.toList());
    }

    private Booking getBookingById(Integer bookingId) {
        return bookingStorage.findById(bookingId).orElseThrow(() -> {
            log.error("Бронирование {} не найдено!", bookingId);
            throw new ResourceNotFoundException(String.format("Бронирование %d не найдено!", bookingId));
        });
    }

    private void checkRightsForUpdate(Booking booking, Integer userId) {
        if (!Objects.equals(booking.getItem().getOwner().getId(), userId)) {
            log.error("Подтвердить/отменить  бронирование {} может только владелец!", booking.getId());
            throw new RightsException(String.format("Подтвердить/отменить  бронирование %d может только владелец!", booking.getId()));
        }
    }

    private void checkRightsForGet(Booking booking, Integer userId) {
        if (!userId.equals(booking.getBooker().getId()) &&
                !userId.equals(booking.getItem().getOwner().getId())) {
            log.error("Получить данные по бронированию {} может либо автор, либо владелец!", booking.getId());
            throw new RightsException("Получить данные по бронированию может либо автор, либо владелец!");
        }
    }
}
