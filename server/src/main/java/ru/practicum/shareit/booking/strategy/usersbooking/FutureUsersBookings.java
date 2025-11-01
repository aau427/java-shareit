package ru.practicum.shareit.booking.strategy.usersbooking;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.strategy.BookingFindStrategy;
import ru.practicum.shareit.booking.strategy.FindBookingStateEnum;
import ru.practicum.shareit.common.Common;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Component
public class FutureUsersBookings extends BookingFindStrategy {

    @Override
    public List<Booking> findBooking(User user) {
        return bookingStorage.findBookingByBookerAndStartAfter(user, Common.getLocalDateTime());
    }

    @Override
    public FindBookingStateEnum getState() {
        return FindBookingStateEnum.FUTURE_USERS;
    }
}
