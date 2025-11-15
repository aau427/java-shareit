package ru.practicum.shareit.booking.strategy;


import ru.practicum.shareit.exceptions.ValidationException;

public enum FindBookingStateEnum {
    ALL_USERS, CURRENT_USERS, PAST_USERS, FUTURE_USERS, WAITING_USERS, REJECTED_USERS,
    ALL_OWNERS, CURRENT_OWNERS, PAST_OWNERS, FUTURE_OWNERS, WAITING_OWNERS, REJECTED_OWNERS;

    public static FindBookingStateEnum parse(String raw) {
        try {
            return valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException(String.format("Недопустимый State = %s поиска бронирований", raw));
        }
    }
}
