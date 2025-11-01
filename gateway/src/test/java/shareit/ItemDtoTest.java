package shareit;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import shareit.item.dto.ItemDto;

import java.io.IOException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@JsonTest
public class ItemDtoTest {

    @Autowired
    private JacksonTester<ItemDto> json;

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private ItemDto itemDto;

    @BeforeEach()
    void beforeEach() {
        itemDto = ItemDto.builder()
                .id(1)
                .name("Кофемашина")
                .description("Отличная кофемашина")
                .available(true)
                .owner(101)
                .requestId(202)
                .build();
    }

    @DisplayName("Сериализация ItemDto в JSON: все поля присутствуют")
    @Test
    void itemDtoJsonSerializationTest() throws IOException {

        JsonContent<ItemDto> result = json.write(itemDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Кофемашина");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Отличная кофемашина");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(true);
        assertThat(result).extractingJsonPathNumberValue("$.owner").isEqualTo(101);
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(202);
    }


    @DisplayName("Валидация: поле name не может быть пустым")
    @Test
    void validation_WhenBlankName_ShouldHaveViolation() {
        itemDto.setName("");

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(itemDto);

        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Не указано наименование вещи", violations.iterator().next().getMessage());
    }

    @DisplayName("Валидация: поле description не может быть null")
    @Test
    void validation_WhenNullDescription_ShouldHaveViolation() {
        itemDto.setDescription(null);

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(itemDto);

        assertFalse(violations.isEmpty());
        assertEquals("Не указано описание вещи", violations.iterator().next().getMessage());
    }

    @DisplayName("Валидация: поле available не может быть null")
    @Test
    void validation_WhenNullAvailable_ShouldHaveViolation() {
        itemDto.setAvailable(null);

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(itemDto);

        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Не указана доступна ли вещь", violations.iterator().next().getMessage());
    }

    @DisplayName("Валидация: поля name не превышает 50 символов")
    @Test
    void validation_WhenNameTooLong_ShouldHaveViolation() {
        itemDto.setName("a".repeat(51));

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(itemDto);

        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
    }

    @DisplayName("Валидация: поле description не превышает 50 символов")
    @Test
    void validation_WhenDescriptionTooLong_ShouldHaveViolation() {
        itemDto.setDescription("a".repeat(51));

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(itemDto);

        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
    }
}




