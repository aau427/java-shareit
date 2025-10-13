package ru.practicum.shareit.booking.service.strategy.usersbooking;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.strategy.BookingFindStrategy;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

public class PastUsersBookings extends BookingFindStrategy {
    @Override
    public List<Booking> findBooking(User user) {
        return bookingStorage.findBookingByBookerAndEndBefore(user, LocalDateTime.now());
    }
}
