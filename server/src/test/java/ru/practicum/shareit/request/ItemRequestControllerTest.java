package ru.practicum.shareit.request;

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
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.common.Common;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.request.dto.ItemRequestInDto;
import ru.practicum.shareit.request.storage.ItemRequestStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@SpringBootTest
public class ItemRequestControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserStorage userStorage;
    @Autowired
    private ItemRequestStorage itemRequestStorage;
    @Autowired
    private ItemStorage itemStorage;

    private ItemRequestInDto inDto;
    private User user;

    @Transactional
    @BeforeEach
    void beforeEach() {
        user = new User();
        user.setId(1);
        user.setName("name");
        user.setEmail("some@mail.ru");
        user = userStorage.save(user);

        inDto = new ItemRequestInDto();
        inDto.setUserId(user.getId());
        inDto.setDescription("хочу велосипед");
    }

    @AfterEach
    @Transactional
    void afterEach() {
        itemStorage.deleteAll();
        itemRequestStorage.deleteAll();
        userStorage.deleteAll();
    }

    @DisplayName("Запрос создается")
    @Test
    @SneakyThrows
    void shouldRequestCreate() {
        mockMvc.perform(post("/requests")
                        .header(Common.USER_HEADER, user.getId())
                        .content(objectMapper.writeValueAsString(inDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.description").value(inDto.getDescription()))
                .andExpect(jsonPath("$.created").isNotEmpty());
    }

    @DisplayName("Запрос от левого пользователя не создается")
    @Test
    @SneakyThrows
    void shouldRequestNotCreateIfUserNotExists() {
        mockMvc.perform(post("/requests")
                        .header(Common.USER_HEADER, 666)
                        .content(objectMapper.writeValueAsString(inDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404));
    }

    @Nested
    @DisplayName("Тестируем поиски")
    class FindRequestsTest {

        private int createdRequestId;
        private String createdRequestDescription;
        private int anotherRequestId;

        @BeforeEach
        @SneakyThrows
        void beforeEach() {
            MvcResult result = mockMvc.perform(post("/requests")
                            .header(Common.USER_HEADER, user.getId())
                            .content(objectMapper.writeValueAsString(inDto))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andReturn();
            String responseBody = result.getResponse().getContentAsString();
            createdRequestId = JsonPath.read(responseBody, "$.id");

            inDto.setDescription("хочу авто");
            result = mockMvc.perform(post("/requests")
                            .header(Common.USER_HEADER, user.getId())
                            .content(objectMapper.writeValueAsString(inDto))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andReturn();
            responseBody = result.getResponse().getContentAsString();
            anotherRequestId = JsonPath.read(responseBody, "$.id");
        }

        @DisplayName("Возвращает список своих запросов, если ответов нет")
        @Test
        @SneakyThrows
        void shouldReturnUserRequestsWhenNoOneResponse() {
            mockMvc.perform(get("/requests")
                            .header(Common.USER_HEADER, user.getId())
                    )
                    .andExpect(status().is(200))
                    .andExpect(jsonPath("$", hasSize(2)));
        }

        @DisplayName("Возвращает ошибку для левого пользователя")
        @Test
        @SneakyThrows
        void shouldReturnErrorForNotExistsUser() {

            mockMvc.perform(get("/requests")
                            .header(Common.USER_HEADER, 666)
                    )
                    .andExpect(status().is(404));
        }
    }
}
