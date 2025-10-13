package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.strategy.BookingFindStrategy;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public class FindBookingsManager {
    private BookingFindStrategy findStrategy;

    public void setStrategy(BookingFindStrategy findStrategy) {
        this.findStrategy = findStrategy;
    }

    public List<Booking> findBookings(User user) {
        return findStrategy.findBooking(user);
    }
}
