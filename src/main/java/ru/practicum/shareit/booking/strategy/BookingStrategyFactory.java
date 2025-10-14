package ru.practicum.shareit.booking.strategy;

import jakarta.validation.ValidationException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BookingStrategyFactory {
    private final List<BookingFindStrategy> listOfStrategies;

    public BookingStrategyFactory(List<BookingFindStrategy> listOfStrategies) {
        this.listOfStrategies = listOfStrategies;
    }

    public List<BookingFindStrategy> getListOfStrategies() {
        return listOfStrategies;
    }

    public BookingFindStrategy getStrategyByState(FindBookingStateEnum state) {
        return listOfStrategies.stream()
                .filter(strategy -> strategy.getState().equals(state))
                .findFirst()
                .orElseThrow(() -> new ValidationException(String.format("Недопустимый State = %s поиска бронирований", state)));
    }
}
