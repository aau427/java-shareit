package ru.practicum.shareit.booking.model;

import lombok.Data;

@Data
public class LastAndNextBookings {
    private Booking lastBooking;
    private Booking nextBooking;
}
