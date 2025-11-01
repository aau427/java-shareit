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
import shareit.user.dto.UserDto;

import java.io.IOException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@JsonTest
public class UserDtoTest {

    @Autowired
    private JacksonTester<UserDto> json;

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private UserDto userDto;

    @BeforeEach
    void beforeEach() {
        userDto = new UserDto();
        userDto.setId(1);
        userDto.setName("Анастасия");
        userDto.setEmail("some@yandex.ru");
    }

    @DisplayName("Сериализация UserDto в JSON: все поля заполнены")
    @Test
    void userDtoJsonSerializationTest() throws IOException {

        JsonContent<UserDto> result = json.write(userDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Анастасия");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("some@yandex.ru");
    }


    @DisplayName("Валидация: поле email должно быть в формате email")
    @Test
    void shouldHaveViolationsWhenInvalidEmail() {
        userDto.setEmail("неправильный формат");

        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);

        assertEquals(1, violations.size());
        assertEquals("Некорректный e-mail", violations.iterator().next().getMessage());
    }

    @DisplayName("Валидация: поле email не может быть пустым")
    @Test
    void shouldHaveViolationsWhenEmailEmpty() {
        userDto.setEmail("");

        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);

        assertEquals(1, violations.size());
        assertEquals("Не указан e-mail", violations.iterator().next().getMessage());
    }

    @DisplayName("Валидация: поле name не может быть пустым")
    @Test
    void shouldHaveViolationsWhenNameIsEmpty() {
        userDto.setName("   ");
        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);

        assertEquals(1, violations.size());
        assertEquals("Не указано имя пользователя", violations.iterator().next().getMessage());
    }

    @DisplayName("Валидация: поле name не может быть null")
    @Test
    void validation_WhenBlankName_ShouldHaveViolations() {
        userDto.setName(null);

        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);

        assertEquals(1, violations.size());
        assertEquals("Не указано имя пользователя", violations.iterator().next().getMessage());
    }
}




