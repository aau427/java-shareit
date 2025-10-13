package ru.practicum.shareit.booking.service.strategy.ownersbooking;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.strategy.BookingFindStrategy;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public class RejectedOwnersBookings extends BookingFindStrategy {
    @Override
    public List<Booking> findBooking(User user) {
        return bookingStorage.getBookingsForOwnerByStatus(user.getId(), "REJECTED");
    }
}
