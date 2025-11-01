package shareit;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import shareit.request.dto.ItemRequestInDto;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@JsonTest
class ItemRequestInDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private ItemRequestInDto itemRequestDto;

    @BeforeEach
    void beforeEach() {
        itemRequestDto = ItemRequestInDto.builder()
                .userId(1)
                .description("Хочу самовар")
                .build();
    }
    @DisplayName("Валидация: корректный DTO не должен иметь нарушений")
    @Test
    void validation_WhenValidDto_ShouldHaveNoViolations() {

        Set<ConstraintViolation<ItemRequestInDto>> violations = validator.validate(itemRequestDto);

        assertTrue(violations.isEmpty());
    }

    @DisplayName("Валидация: поле description не может быть пустым")
    @Test
    void validation_WhenBlankDescription_ShouldHaveViolation() {
       itemRequestDto.setDescription("   ");

        Set<ConstraintViolation<ItemRequestInDto>> violations = validator.validate(itemRequestDto);

        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Не указано описание запроса!", violations.iterator().next().getMessage());
    }

    @DisplayName("Валидация: поле description не может быть null")
    @Test
    void validation_WhenNullDescription_ShouldHaveViolation() {
        itemRequestDto.setDescription(null);
        Set<ConstraintViolation<ItemRequestInDto>> violations = validator.validate(itemRequestDto);

        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Не указано описание запроса!", violations.iterator().next().getMessage());
    }
}