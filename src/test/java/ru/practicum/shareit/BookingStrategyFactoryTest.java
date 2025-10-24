package ru.practicum.shareit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.strategy.BookingFindStrategy;
import ru.practicum.shareit.booking.strategy.BookingStrategyFactory;
import ru.practicum.shareit.booking.strategy.FindBookingStateEnum;
import ru.practicum.shareit.booking.strategy.usersbooking.AllUsersBookings;

@SpringBootTest
class BookingStrategyFactoryTest {
    @Autowired
    private BookingStrategyFactory factory;

    @Test
    public void testCalculateStrategies() {
        Assertions.assertEquals(11, factory.getListOfStrategies().size());
    }

    @Test
    public void getAllUsersStrategy() {
        BookingFindStrategy strategy = factory.getStrategyByState(FindBookingStateEnum.ALL_USERS);

        Assertions.assertEquals(strategy.getClass(), AllUsersBookings.class);
    }
}