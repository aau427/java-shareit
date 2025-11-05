package ru.practicum.shareit.booking;

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
import ru.practicum.shareit.booking.dto.InputBookingDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.common.Common;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.BaseControllerHelper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.storage.UserStorage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@SpringBootTest
public class BookingControllerTest extends BaseControllerHelper {
    @Autowired
    UserStorage userStorage;
    @Autowired
    ItemStorage itemStorage;
    @Autowired
    BookingStorage bookingStorage;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    private int createdOwnerId;
    private int createdBookerId;

    private ItemDto itemDto;
    private Integer createdItemId;

    private InputBookingDto inputBookingDto;

    private LocalDateTime start;
    private LocalDateTime end;


    @BeforeEach
    @SneakyThrows
    void beforeEach() {
        UserDto ownerDto = createFirstUserDto();

        createdOwnerId = createUser(ownerDto);

        UserDto bookerDto = createSecondUserDto();
        createdBookerId = createUser(bookerDto);


        itemDto = createFirstInputItemDto(createdOwnerId);
        createdItemId = createItem(createdOwnerId, itemDto);

        start = LocalDateTime.now().minusDays(10);
        end = LocalDateTime.now().minusDays(5);


        inputBookingDto = new InputBookingDto();
        inputBookingDto.setStart(start);
        inputBookingDto.setEnd(end);
        inputBookingDto.setItemId(createdItemId);
        inputBookingDto.setBookerId(createdBookerId);
    }

    @AfterEach
    void afterEach() {
        bookingStorage.deleteAll();
        itemStorage.deleteAll();
        userStorage.deleteAll();
    }

    @DisplayName("Бронирование действительно создается")
    @Test
    @SneakyThrows
    void shouldBookingCreate() {
        String expectedStart = inputBookingDto.getStart().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String expectedEnd = inputBookingDto.getEnd().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        mockMvc.perform(post("/bookings")
                        .header(Common.USER_HEADER, createdBookerId)
                        .content(objectMapper.writeValueAsString(inputBookingDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.start").value(expectedStart))
                .andExpect(jsonPath("$.end").value(expectedEnd))
                .andExpect(jsonPath("$.status").value(BookingStatus.WAITING.name()))
                .andExpect(jsonPath("$.item.id").value(createdItemId))
                .andExpect(jsonPath("$.item.name").value(itemDto.getName()))
                .andExpect(jsonPath("$.item.description").value(itemDto.getDescription()))
                .andExpect(jsonPath("$.item.available").value(itemDto.getAvailable()));
    }

    @DisplayName("Бронирование не создается, если владелец и букер одно лицо")
    @Test
    @SneakyThrows
    void shouldBookingNotCreateIfOwnerAndBookerEqual() {

        mockMvc.perform(post("/bookings")
                        .header(Common.USER_HEADER, createdOwnerId)
                        .content(objectMapper.writeValueAsString(inputBookingDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    private int createUser(UserDto userDto) throws Exception {
        MvcResult result = mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
    }

    private Integer createItem(int ownerId, ItemDto itemDto) throws Exception {
        MvcResult result = mockMvc.perform(post("/items")
                        .header(Common.USER_HEADER, ownerId)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
    }

    @Nested
    @DisplayName("Подтверждение бронирования и отчеты")
    class ApproveAndRepostsTestClass {

        private int createdBookingId;


        @SneakyThrows
        @BeforeEach
        void beforeEach() {
            String result = mockMvc.perform(post("/bookings")
                            .header(Common.USER_HEADER, createdBookerId)
                            .content(objectMapper.writeValueAsString(inputBookingDto))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            createdBookingId = JsonPath.read(result, "$.id");
        }

        @DisplayName("Владелец может подтвердить бронирование")
        @Test
        @SneakyThrows
        void shouldOwnerApproveBooking() {
            mockMvc.perform(patch("/bookings/{bookingId}", createdBookingId)
                            .header(Common.USER_HEADER, createdOwnerId)
                            .param("approved", "true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(BookingStatus.APPROVED.name()));
        }

        @DisplayName("Владелец может отклонить бронирование")
        @Test
        @SneakyThrows
        void shouldOwnerRejectBooking() {
            mockMvc.perform(patch("/bookings/{bookingId}", createdBookingId)
                            .header(Common.USER_HEADER, createdOwnerId)
                            .param("approved", "false"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(BookingStatus.REJECTED.name()));
        }

        @DisplayName("Левый пользователь  не может подтвердить бронирование")
        @Test
        @SneakyThrows
        void shouldNotNonOwnerApproveBooking() {
            mockMvc.perform(patch("/bookings/{bookingId}", createdBookingId)
                            .header(Common.USER_HEADER, 666) // Booker tries to approve
                            .param("approved", "true"))
                    .andExpect(status().is(403));
        }

        @DisplayName("Букер может получить свое бронирование по его ID")
        @Test
        @SneakyThrows
        void shouldBookerGetBookingById() {
            mockMvc.perform(get("/bookings/{bookingId}", createdBookingId)
                            .header(Common.USER_HEADER, createdBookerId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(createdBookingId));
        }

        @DisplayName("Владелец вещи может получить бронирование по Id")
        @Test
        @SneakyThrows
        void shouldOwnerGetBookingById() {
            mockMvc.perform(get("/bookings/{bookingId}", createdBookingId)
                            .header(Common.USER_HEADER, createdOwnerId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(createdBookingId));
        }

        @DisplayName("Левый пользователь не может получить бронирование по Id")
        @Test
        @SneakyThrows
        void shouldNotNotBookerGetBookingById() {
            mockMvc.perform(get("/bookings/{bookingId}", createdBookingId)
                            .header(Common.USER_HEADER, 666))
                    .andExpect(status().is(403));
        }

        @DisplayName("Арендатор может получить список всех бронирований")
        @Test
        @SneakyThrows
        void shouldListAllBookingsForBooker() {
            mockMvc.perform(get("/bookings")
                            .header(Common.USER_HEADER, createdBookerId)
                            .param("state", "ALL"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(createdBookingId));
        }

        @DisplayName("Владелец может получить список всех бронирований")
        @Test
        @SneakyThrows
        void shouldListAllBookingsForOwner() {
            mockMvc.perform(get("/bookings/owner")
                            .header(Common.USER_HEADER, createdOwnerId)
                            .param("state", "ALL"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(createdBookingId));
        }

        @DisplayName("Арендатор может получить список всех бронирований, ожидающих подтверждения")
        @Test
        @SneakyThrows
        void shouldBookerGetListWaitingBookings() {
            mockMvc.perform(get("/bookings")
                            .header(Common.USER_HEADER, createdBookerId)
                            .param("state", "WAITING"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(createdBookingId));
        }

        @DisplayName("Владелец может получить список всех бронирований, ожидающих подтверждения")
        @Test
        @SneakyThrows
        void shouldGetListWaitingBookingsForOwner() {
            mockMvc.perform(get("/bookings/owner")
                            .header(Common.USER_HEADER, createdOwnerId)
                            .param("state", "WAITING"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(createdBookingId));
        }

        @DisplayName("Арендатор может получить список всех отклоненных бронирований")
        @Test
        @SneakyThrows
        void shouldBookerGetListRejectedBookings() {
            //отклоним бронирование
            mockMvc.perform(patch("/bookings/{bookingId}", createdBookingId)
                    .header(Common.USER_HEADER, createdOwnerId)
                    .param("approved", "false"));


            mockMvc.perform(get("/bookings")
                            .header(Common.USER_HEADER, createdBookerId)
                            .param("state", "REJECTED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(createdBookingId));
        }

        @DisplayName("Владелец может получить список всех отклоненных бронирований")
        @Test
        @SneakyThrows
        void shouldOwnerGetListRejectedBookings() {
            //отклоним бронирование
            mockMvc.perform(patch("/bookings/{bookingId}", createdBookingId)
                    .header(Common.USER_HEADER, createdOwnerId)
                    .param("approved", "false"));


            mockMvc.perform(get("/bookings/owner")
                            .header(Common.USER_HEADER, createdOwnerId)
                            .param("state", "REJECTED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(createdBookingId));
        }

        @DisplayName("Арендатор может получить список всех завершенных бронирований")
        @Test
        @SneakyThrows
        void shouldBookerGetListPastBookings() {
            mockMvc.perform(patch("/bookings/{bookingId}", createdBookingId)
                    .header(Common.USER_HEADER, createdOwnerId)
                    .param("approved", "true"));


            mockMvc.perform(get("/bookings")
                            .header(Common.USER_HEADER, createdBookerId)
                            .param("state", "PAST"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(createdBookingId));
        }

        @DisplayName("Владелец может получить список всех завершенных бронирований")
        @Test
        @SneakyThrows
        void shouldOwnerGetListPastBookings() {
            mockMvc.perform(patch("/bookings/{bookingId}", createdBookingId)
                    .header(Common.USER_HEADER, createdOwnerId)
                    .param("approved", "true"));


            mockMvc.perform(get("/bookings/owner")
                            .header(Common.USER_HEADER, createdOwnerId)
                            .param("state", "PAST"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(createdBookingId));
        }

        @DisplayName("Арендатор может получить список всех текущих бронирований")
        @Test
        @SneakyThrows
        void shouldBookerGetListCurrentBookings() {
            inputBookingDto = new InputBookingDto();
            inputBookingDto.setStart(LocalDateTime.now().minusDays(1));
            inputBookingDto.setEnd(LocalDateTime.now().plusDays(2));
            inputBookingDto.setItemId(createdItemId);
            inputBookingDto.setBookerId(createdBookerId);

            String result = mockMvc.perform(post("/bookings")
                            .header(Common.USER_HEADER, createdBookerId)
                            .content(objectMapper.writeValueAsString(inputBookingDto))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            int currentBookingId = JsonPath.read(result, "$.id");


            mockMvc.perform(get("/bookings")
                            .header(Common.USER_HEADER, createdBookerId)
                            .param("state", "CURRENT"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(currentBookingId));
        }

        @DisplayName("Владелец может получить список всех текущих бронирований")
        @Test
        @SneakyThrows
        void shouldOwnerGetListCurrentBookings() {
            inputBookingDto = new InputBookingDto();
            inputBookingDto.setStart(LocalDateTime.now().minusDays(1));
            inputBookingDto.setEnd(LocalDateTime.now().plusDays(2));
            inputBookingDto.setItemId(createdItemId);
            inputBookingDto.setBookerId(createdBookerId);

            String result = mockMvc.perform(post("/bookings")
                            .header(Common.USER_HEADER, createdBookerId)
                            .content(objectMapper.writeValueAsString(inputBookingDto))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            int currentBookingId = JsonPath.read(result, "$.id");


            mockMvc.perform(get("/bookings/owner")
                            .header(Common.USER_HEADER, createdOwnerId)
                            .param("state", "CURRENT"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(currentBookingId));
        }


        @DisplayName("Арендатор может получить список всех будущих бронирований")
        @Test
        @SneakyThrows
        void shouldBookerGetListFutureBookings() {
            inputBookingDto = new InputBookingDto();
            inputBookingDto.setStart(LocalDateTime.now().plusDays(1));
            inputBookingDto.setEnd(LocalDateTime.now().plusDays(2));
            inputBookingDto.setItemId(createdItemId);
            inputBookingDto.setBookerId(createdBookerId);

            String result = mockMvc.perform(post("/bookings")
                            .header(Common.USER_HEADER, createdBookerId)
                            .content(objectMapper.writeValueAsString(inputBookingDto))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            int futureBookingId = JsonPath.read(result, "$.id");


            mockMvc.perform(get("/bookings")
                            .header(Common.USER_HEADER, createdBookerId)
                            .param("state", "FUTURE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(futureBookingId));
        }

        @DisplayName("Владелец может получить список всех будущих бронирований")
        @Test
        @SneakyThrows
        void shouldOwnerGetListFutureBookings() {
            inputBookingDto = new InputBookingDto();
            inputBookingDto.setStart(LocalDateTime.now().plusDays(1));
            inputBookingDto.setEnd(LocalDateTime.now().plusDays(2));
            inputBookingDto.setItemId(createdItemId);
            inputBookingDto.setBookerId(createdBookerId);

            String result = mockMvc.perform(post("/bookings")
                            .header(Common.USER_HEADER, createdBookerId)
                            .content(objectMapper.writeValueAsString(inputBookingDto))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            int futureBookingId = JsonPath.read(result, "$.id");


            mockMvc.perform(get("/bookings/owner")
                            .header(Common.USER_HEADER, createdOwnerId)
                            .param("state", "FUTURE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(futureBookingId));
        }
    }

}
