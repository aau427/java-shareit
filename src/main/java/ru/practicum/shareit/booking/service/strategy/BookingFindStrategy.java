package ru.practicum.shareit.booking.service.strategy;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public abstract class BookingFindStrategy {
    protected BookingStorage bookingStorage;

    public final void setBookingStorage(BookingStorage bookingStorage) {
        this.bookingStorage = bookingStorage;
    }

    public abstract List<Booking> findBooking(User user);
}
