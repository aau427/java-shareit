package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class InputBookingDto {
    @NotNull(message = "Не указана дата начала бронирования")
    LocalDateTime start;
    @NotNull(message = "Не указана дата окончания бронирования")
    LocalDateTime end;
    @NotNull(message = "Не указано, что бронируется")
    Integer itemId;
    Integer bookerId;
}
