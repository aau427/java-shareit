package ru.practicum.shareit.booking.strategy.ownersbooking;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.strategy.BookingFindStrategy;
import ru.practicum.shareit.booking.strategy.FindBookingStateEnum;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class PastOwnersBookings extends BookingFindStrategy {
    @Override
    public List<Booking> findBooking(User user) {
        return bookingStorage.getPastBookingForOwner(user.getId(), LocalDateTime.now());
    }

    @Override
    public FindBookingStateEnum getState() {
        return FindBookingStateEnum.PAST_OWNERS;
    }
}
