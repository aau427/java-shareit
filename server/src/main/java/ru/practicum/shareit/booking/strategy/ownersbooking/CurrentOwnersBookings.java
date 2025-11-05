package ru.practicum.shareit.booking.strategy.ownersbooking;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.strategy.BookingFindStrategy;
import ru.practicum.shareit.booking.strategy.FindBookingStateEnum;
import ru.practicum.shareit.common.Common;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class CurrentOwnersBookings extends BookingFindStrategy {
    @Override
    public FindBookingStateEnum getState() {
        return FindBookingStateEnum.CURRENT_OWNERS;
    }

    @Override
    public List<Booking> findBooking(User user) {
        LocalDateTime dateTime = Common.getLocalDateTime();
        return bookingStorage.getCurrentBookingForOwner(user.getId(), dateTime, dateTime);
    }
}
