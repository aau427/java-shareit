package shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BookingDto {
    @NotNull(message = "Не указана дата начала бронирования")
    private LocalDateTime start;
    @NotNull(message = "Не указана дата окончания бронирования")
    private LocalDateTime end;
    @NotNull(message = "Не указано, что бронируется")
    private Integer itemId;
    private Integer bookerId;
}
