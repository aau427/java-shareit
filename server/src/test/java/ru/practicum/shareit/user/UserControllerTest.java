package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.practicum.shareit.BaseControllerHelper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.storage.UserStorage;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@SpringBootTest
class UserControllerTest extends BaseControllerHelper {

    @Autowired
    UserStorage userStorage;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    private UserDto ownerDto;

    @BeforeEach
    void beforeEach() {
        ownerDto = createOwnerDto();
    }

    @AfterEach
    void afterEach() {
        userStorage.deleteAll();
    }

    @DisplayName("Пользователь действительно создается")
    @Test
    @SneakyThrows
    void shouldUserCreate() {

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(ownerDto))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value(ownerDto.getName()))
                .andExpect(jsonPath("$.email").value(ownerDto.getEmail()));
    }

    @DisplayName("Не создается пользователь с указанным ID")
    @Test
    @SneakyThrows
    void shouldNotCreateUserWithId() {
        ownerDto.setId(1);

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(ownerDto))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is(409))
                .andExpect(jsonPath("$.error").value("Ошибка валидации"));
    }


    @DisplayName("Не создает 2-х пользователей с одинаковым e-mail")
    @Test
    @SneakyThrows
    void shouldNotCreateUsersWithTheSameEmail() {

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(ownerDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(ownerDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Нарушена уникальность при при вставке/обновлении"))
                .andExpect(jsonPath("$.description").isString());
    }

    @DisplayName("Пользователь не создается без имени")
    @Test
    @SneakyThrows
    void shouldUserNotCreateWithoutName() {
        ownerDto.setName(null);
        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(ownerDto))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is(400));
    }

    @DisplayName("Пользователь не создается c некорректным email")
    @Test
    @SneakyThrows
    void shouldUserNotCreateWithIncorrectEmail() {
        ownerDto.setEmail("email");
        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(ownerDto))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is(400));
    }

    private UserDto createOwnerDto() {
        UserDto userDto = new UserDto();
        userDto.setName("Item Owner");
        userDto.setEmail("owner@mail.ru");
        return userDto;
    }

    @Nested
    @DisplayName("Поиск, удаление, обновление пользователей")
    class CrudUserClassTest {
        private int createdUserId;

        @BeforeEach
        @SneakyThrows
        void beforeEach() {
            MvcResult result = mockMvc.perform(post("/users")
                            .content(objectMapper.writeValueAsString(ownerDto))
                            .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andReturn();
            String responseBody = result.getResponse().getContentAsString();
            createdUserId = JsonPath.read(responseBody, "$.id");
        }

        @DisplayName("Существующий пользователь возвращается по Id")
        @Test
        @SneakyThrows
        void shouldUserGetById() {
            mockMvc.perform(get("/users/" + createdUserId))
                    .andExpect(jsonPath("$.id").value(createdUserId))
                    .andExpect(jsonPath("$.name").value(ownerDto.getName()))
                    .andExpect(jsonPath("$.email").value(ownerDto.getEmail()));
        }

        @DisplayName("При запросе несуществующего пользователя получаем ошибку")
        @Test
        @SneakyThrows
        void shouldUserNotGetByIdIfNotExists() {
            mockMvc.perform(get("/users/" + 666))
                    .andExpect(status().is(404));
        }

        @DisplayName("Возвращает пустой список пользователей")
        @Test
        @SneakyThrows
        void shouldReturnEmptyUsersList() {
            userStorage.deleteAll();
            mockMvc.perform(get("/users"))
                    .andExpect(status().is(200))
                    .andExpect(jsonPath("$").isEmpty());
        }

        @DisplayName("Возвращает список существующих пользователей")
        @Test
        @SneakyThrows
        void shouldReturnUsersList() {
            mockMvc.perform(get("/users"))
                    .andExpect(status().is(200))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(1))) // Проверяем, что в списке 1 элемент
                    .andExpect(jsonPath("$[0].id").value(createdUserId))
                    .andExpect(jsonPath("$[0].name").value(ownerDto.getName()))
                    .andExpect(jsonPath("$[0].email").value(ownerDto.getEmail()));
        }

        @DisplayName("При попытке обновления несуществующего пользователя выдает ошибку")
        @Test
        @SneakyThrows
        void shouldNotUpdateUserIfNotExists() {
            mockMvc.perform(patch("/users/" + 666)
                            .content(objectMapper.writeValueAsString(ownerDto))
                            .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().is(404));
        }

        @DisplayName("Обновляет существующего пользователя")
        @Test
        @SneakyThrows
        void shouldUpdateUserIfExists() {
            ownerDto.setId(createdUserId);
            ownerDto.setName("Изменил имя");
            ownerDto.setEmail("change@email");

            mockMvc.perform(patch("/users/" + createdUserId)
                            .content(objectMapper.writeValueAsString(ownerDto))
                            .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().is(200))
                    .andExpect(jsonPath("$.id").value(createdUserId))
                    .andExpect(jsonPath("$.name").value(ownerDto.getName()))
                    .andExpect(jsonPath("$.email").value(ownerDto.getEmail()));
        }

        @DisplayName("Удаляет пользователя")
        @Test
        @SneakyThrows
        void shouldDeleteExistingUser() {
            mockMvc.perform(delete("/users/" + createdUserId))
                    .andExpect(status().is(200));
        }

        @DisplayName("При удалении несуществующего пользователя выдает ошибку")
        @Test
        @SneakyThrows
        void shouldDeleteNotExistingUser() {
            mockMvc.perform(delete("/users/" + 666))
                    .andExpect(status().is(404));
        }
    }
}
