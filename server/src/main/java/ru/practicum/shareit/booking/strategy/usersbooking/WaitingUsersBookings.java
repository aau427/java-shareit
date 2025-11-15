package ru.practicum.shareit.booking.strategy.usersbooking;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.strategy.BookingFindStrategy;
import ru.practicum.shareit.booking.strategy.FindBookingStateEnum;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Component
public class WaitingUsersBookings extends BookingFindStrategy {
    @Override
    public List<Booking> findBooking(User user) {
        return bookingStorage.findBookingByBookerAndStatus(user, BookingStatus.WAITING);
    }

    @Override
    public FindBookingStateEnum getState() {
        return FindBookingStateEnum.WAITING_USERS;
    }
}
