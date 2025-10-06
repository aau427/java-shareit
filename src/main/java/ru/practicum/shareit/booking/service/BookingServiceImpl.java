package ru.practicum.shareit.booking.service;

import jakarta.validation.ValidationException;
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
import ru.practicum.shareit.common.Common;
import ru.practicum.shareit.exceptions.LogicalException;
import ru.practicum.shareit.exceptions.ResourceNotFoundException;
import ru.practicum.shareit.exceptions.RightsException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
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

    @Transactional
    @Override
    public OutputBookingDto createBooking(InputBookingDto bookingDto) {
        Booking booking = bookingMapper.dtoToBooking(bookingDto);
        booking.setStatus(Common.DEFAULT_BOOKING_STATUS);
        checkBookingBeforeCreate(booking);
        return bookingMapper.bookingToDto(bookingStorage.save(booking));
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
        return bookingMapper.bookingToDto(bookingStorage.save(booking));
    }

    private void checkBookingBeforeCreate(Booking booking) {
        if (booking.getStart().isAfter(booking.getEnd())) {
            throw new LogicalException("Дата начала позже даты окончания");
        } else if (booking.getStart().equals(booking.getEnd())) {
            throw new LogicalException("Начало бронирования не может быть равно его окончанию!");
        } /*else if (booking.getStart().isBefore(LocalDateTime.now())) {
            throw new LogicalException("Дата начала не может быть в прошлом");
        }*/
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
        return bookingMapper.bookingToDto(booking);
    }

    @Override
    public List<OutputBookingDto> getUsersBooking(Integer userId, String state) {
        User user = userService.getUserById(userId);
        List<Booking> bookList = switch (state) {
            case "ALL" -> bookingStorage.findBookingByBooker(user);
            case "WAITING", "REJECTED" -> bookingStorage.findBookingByBookerAndStatus(user, state);
            case "CURRENT" -> {
                LocalDateTime dateTime = LocalDateTime.now();
                yield bookingStorage.findBookingByBookerAndStartBeforeAndEndAfter(user, dateTime, dateTime);
            }
            case "PAST" -> bookingStorage.findBookingByBookerAndEndBefore(user, LocalDateTime.now());
            case "FUTURE" -> bookingStorage.findBookingByBookerAndStartAfter(user, LocalDateTime.now());
            default -> {
                log.info("Недопустимый State = {} в запросе на получение бронирований пользователя", state);
                throw new ValidationException(String.format("Недопустимый State = %s в запросе на получение бронирований пользователя", state));
            }
        };
        return bookList.stream()
                .map(bookingMapper::bookingToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<OutputBookingDto> getOwnersBookings(Integer userId, String state) {
        User user = userService.getUserById(userId);
        List<Booking> bookList = switch (state) {
            case "ALL" -> bookingStorage.getAllBookingsForOwner(userId);
            case "WAITING", "REJECTED" -> bookingStorage.getBookingsForOwnerByStatus(userId, state);
            case "CURRENT" -> {
                LocalDateTime dateTime = LocalDateTime.now();
                yield bookingStorage.getCurrentBookingForOwner(userId, dateTime, dateTime);
            }
            case "PAST" -> bookingStorage.getPastBookingForOwner(userId, LocalDateTime.now());
            case "FUTURE" -> {
                LocalDateTime dateTime2 = LocalDateTime.now();
                yield bookingStorage.getFutureBookingForOwner(userId, dateTime2);
            }
            default -> {
                log.info("Недопустимый State = {} в запросе на получение бронирований всех вещей пользователя", state);
                throw new ValidationException(String.format("Недопустимый State = %s в запросе на получение бронирований всех вещей пользователя", state));
            }
        };
        return bookList.stream().map(bookingMapper::bookingToDto).collect(Collectors.toList());
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
