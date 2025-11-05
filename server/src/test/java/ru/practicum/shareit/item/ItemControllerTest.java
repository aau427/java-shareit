package ru.practicum.shareit.item;

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
import ru.practicum.shareit.BaseControllerHelper;
import ru.practicum.shareit.common.Common;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.storage.UserStorage;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@SpringBootTest
class ItemControllerTest extends BaseControllerHelper {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserStorage userStorage;
    @Autowired
    private ItemStorage itemStorage;

    private int createdOwnerId;

    private ItemDto itemDto;


    @BeforeEach
    @SneakyThrows
    void beforeEach() {
        UserDto ownerDto = createFirstUserDto();
        MvcResult result = mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(ownerDto))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andReturn();
        String responseBody = result.getResponse().getContentAsString();
        createdOwnerId = JsonPath.read(responseBody, "$.id");
        itemDto = createFirstInputItemDto(createdOwnerId);
    }

    @AfterEach
    @Transactional
    void afterEach() {
        itemStorage.deleteAll();
        userStorage.deleteAll();
    }

    @SneakyThrows
    @DisplayName("Item действительно создается")
    @Test
    void shouldItemCreate() {

        mockMvc.perform(post("/items")
                        .header(Common.USER_HEADER, createdOwnerId)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value(itemDto.getName()))
                .andExpect(jsonPath("$.description").value(itemDto.getDescription()))
                .andExpect(jsonPath("$.available").value(itemDto.getAvailable()))
                .andExpect(jsonPath("$.owner").value(itemDto.getOwner()));
    }

    @SneakyThrows
    @DisplayName("Item не создается с несуществующим владельцем")
    @Test
    void shouldItemNotCreateWhenOwnerNotExists() {

        mockMvc.perform(post("/items")
                        .header(Common.USER_HEADER, 666)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is(404));
    }

    @SneakyThrows
    @DisplayName("Item не создается с незаполненным именем")
    @Test
    void shouldItemNotCreateWithNullName() {
        itemDto.setName(null);
        mockMvc.perform(post("/items")
                        .header(Common.USER_HEADER, 666)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is(400));
    }

    @SneakyThrows
    @DisplayName("Item не создается с незаполненным описанием")
    @Test
    void shouldItemNotCreateWithNullDescription() {
        itemDto.setDescription(null);
        mockMvc.perform(post("/items")
                        .header(Common.USER_HEADER, 666)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is(400));
    }

    @SneakyThrows
    @DisplayName("Item не создается с незаполненным available")
    @Test
    void shouldItemNotCreateWithNullAvailable() {
        itemDto.setAvailable(null);
        mockMvc.perform(post("/items")
                        .header(Common.USER_HEADER, createdOwnerId)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is(400));
    }

    @Nested
    @DisplayName("Обновление, поиск, удаление вещи")
    class CrudItemClassTest {
        private int createdItemId;
        private int anotherCreatedItemId;
        private ItemDto anotherItemDto;

        @BeforeEach
        @SneakyThrows
        void beforeEach() {
            MvcResult result = mockMvc.perform(post("/items")
                            .header(Common.USER_HEADER, createdOwnerId)
                            .content(objectMapper.writeValueAsString(itemDto))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andReturn();
            String responseBody = result.getResponse().getContentAsString();
            createdItemId = JsonPath.read(responseBody, "$.id");

            anotherItemDto = createSecondItemDto(createdOwnerId);
            result = mockMvc.perform(post("/items")
                            .header(Common.USER_HEADER, createdOwnerId)
                            .content(objectMapper.writeValueAsString(anotherItemDto))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andReturn();
            responseBody = result.getResponse().getContentAsString();
            anotherCreatedItemId = JsonPath.read(responseBody, "$.id");
        }

        @DisplayName("Item действительно обновляется")
        @Test
        @SneakyThrows
        void shouldUpdateItem() {
            itemDto.setDescription("изменено описание");
            itemDto.setAvailable(false);
            mockMvc.perform(patch("/items/" + createdItemId)
                            .header(Common.USER_HEADER, createdOwnerId)
                            .content(objectMapper.writeValueAsString(itemDto))
                            .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().is(200))
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.name").value(itemDto.getName()))
                    .andExpect(jsonPath("$.description").value(itemDto.getDescription()))
                    .andExpect(jsonPath("$.available").value(itemDto.getAvailable()))
                    .andExpect(jsonPath("$.owner").value(itemDto.getOwner()));
        }

        @DisplayName("Item обновляется (только имя)")
        @Test
        @SneakyThrows
        void shouldUpdateItemPartially() {
            itemDto.setName("Новое имя");

            mockMvc.perform(patch("/items/" + createdItemId)
                            .header(Common.USER_HEADER, createdOwnerId)
                            .content(objectMapper.writeValueAsString(itemDto))
                            .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(createdItemId))
                    .andExpect(jsonPath("$.name").value("Новое имя"))
                    .andExpect(jsonPath("$.description").value(itemDto.getDescription()))
                    .andExpect(jsonPath("$.available").value(itemDto.getAvailable()));
        }

        @DisplayName("Item не обновляется, если не существует")
        @Test
        @SneakyThrows
        void shouldNotUpdateItemIfItNotExists() {
            itemDto.setDescription("изменено описание");
            itemDto.setAvailable(false);

            mockMvc.perform(patch("/items/" + 666)
                            .header(Common.USER_HEADER, createdOwnerId)
                            .content(objectMapper.writeValueAsString(itemDto))
                            .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().is(404));
        }

        @DisplayName("Item может обновить только владелец")
        @Test
        @SneakyThrows
        void shouldNotUpdateItemIfWrongOwner() {

            itemDto.setDescription("Попытка обновления");

            UserDto anotherUser = createSecondUserDto();
            String result = mockMvc.perform(post("/users")
                            .content(objectMapper.writeValueAsString(anotherUser))
                            .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            int anotherUserId = JsonPath.read(result, "$.id");

            mockMvc.perform(patch("/items/" + createdItemId)
                            .header(Common.USER_HEADER, anotherUserId)
                            .content(objectMapper.writeValueAsString(itemDto))
                            .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().is(403));
        }

        @DisplayName("Item возвращается по Id")
        @Test
        @SneakyThrows
        void shouldItemGetByIdIfItExists() {
            mockMvc.perform(get("/items/" + createdItemId)
                            .header(Common.USER_HEADER, createdOwnerId)
                            .content(objectMapper.writeValueAsString(itemDto))
                            .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().is(200))
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.name").value(itemDto.getName()))
                    .andExpect(jsonPath("$.description").value(itemDto.getDescription()))
                    .andExpect(jsonPath("$.available").value(itemDto.getAvailable()));
        }

        @DisplayName("Item не возвращается, если не существует")
        @Test
        @SneakyThrows
        void shouldItemNotGetByIdIfItNotExists() {
            mockMvc.perform(get("/items/" + 666)
                            .header(Common.USER_HEADER, createdOwnerId)
                            .content(objectMapper.writeValueAsString(itemDto))
                            .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().is(404));
        }

        @DisplayName("Item не возвращается непонятным пользователем")
        @Test
        @SneakyThrows
        void shouldItemNotGetByIdIfUnauthorized() {
            mockMvc.perform(get("/items/" + createdItemId))
                    .andExpect(status().is(400));
        }

        @DisplayName("Возвращает вещи пользователя")
        @Test
        @SneakyThrows
        void shouldReturnUsersItems() {

            mockMvc.perform(get("/items")
                            .header(Common.USER_HEADER, createdOwnerId))
                    .andExpect(status().is(200))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(2))) // Проверяем, что в списке 1 элемент
                    .andExpect(jsonPath("$[0].id").value(createdItemId))
                    .andExpect(jsonPath("$[0].name").value(itemDto.getName()))
                    .andExpect(jsonPath("$[0].available").value(itemDto.getAvailable()))
                    .andExpect(jsonPath("$[1].id").value(anotherCreatedItemId))
                    .andExpect(jsonPath("$[1].name").value(anotherItemDto.getName()))
                    .andExpect(jsonPath("$[1].available").value(anotherItemDto.getAvailable()));
        }


        @DisplayName("Ищет вещи по контексту")
        @Test
        @SneakyThrows
        void shouldFindItemsByContextSearch() {
            mockMvc.perform(get("/items/search")
                            .param("text", "вещ")
                            .header(Common.USER_HEADER, createdOwnerId))
                    .andExpect(status().is(200))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(2))) // Проверяем, что в списке 1 элемент
                    .andExpect(jsonPath("$[0].id").value(createdItemId))
                    .andExpect(jsonPath("$[0].name").value(itemDto.getName()))
                    .andExpect(jsonPath("$[0].available").value(itemDto.getAvailable()));
        }

        @DisplayName("Поиск вещей с пустым текстом возвращает пустой список")
        @Test
        @SneakyThrows
        void shouldSearchItemsByEmptyText() {
            mockMvc.perform(get("/items/search")
                            .header(Common.USER_HEADER, createdOwnerId)
                            .param("text", ""))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }
}
