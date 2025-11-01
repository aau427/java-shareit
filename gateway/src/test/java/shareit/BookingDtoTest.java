package shareit;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import shareit.booking.dto.BookingDto;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@JsonTest
class BookingDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private BookingDto bookingDto;
    @BeforeEach()
    void beforeEach() {
        bookingDto = BookingDto.builder()
                .itemId(1)
                .bookerId(11)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
    }

    @DisplayName("Валидация: корректный DTO не должен иметь нарушений")
    @Test
    void validation_WhenValidDto_ShouldHaveNoViolations() {

        Set<ConstraintViolation<BookingDto>> violations = validator.validate(bookingDto);

        assertTrue(violations.isEmpty());
    }

    @DisplayName("Валидация: поле start не может быть null")
    @Test
    void validation_WhenNullStart_ShouldHaveViolation() {
        bookingDto.setStart(null);
        Set<ConstraintViolation<BookingDto>> violations = validator.validate(bookingDto);

        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Не указана дата начала бронирования", violations.iterator().next().getMessage());
    }

    @DisplayName("Валидация: поле end не может быть null")
    @Test
    void validation_WhenNullEnd_ShouldHaveViolation() {
        bookingDto.setEnd(null);
        Set<ConstraintViolation<BookingDto>> violations = validator.validate(bookingDto);

        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Не указана дата окончания бронирования", violations.iterator().next().getMessage());
    }

    @DisplayName("Валидация: поле itemId не может быть null")
    @Test
    void validation_WhenNullItemId_ShouldHaveViolation() {
        bookingDto.setItemId(null);
        Set<ConstraintViolation<BookingDto>> violations = validator.validate(bookingDto);

        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Не указано, что бронируется", violations.iterator().next().getMessage());
    }
}