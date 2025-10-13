package ru.practicum.shareit.booking.service.strategy.ownersbooking;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.strategy.BookingFindStrategy;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

public class PastOwnersBookings extends BookingFindStrategy {
    @Override
    public List<Booking> findBooking(User user) {
        return bookingStorage.getPastBookingForOwner(user.getId(), LocalDateTime.now());
    }
}
