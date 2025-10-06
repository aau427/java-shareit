package ru.practicum.shareit.booking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.practicum.shareit.booking.dto.ShortOutBookingDto;
import ru.practicum.shareit.booking.model.Booking;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ShortBookingMapper {
    ShortOutBookingDto bookingToShortDto(Booking booking);
}
