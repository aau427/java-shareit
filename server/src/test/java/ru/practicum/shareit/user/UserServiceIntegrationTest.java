package ru.practicum.shareit.user;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.SimpleUserMapperImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;
import ru.practicum.shareit.user.storage.UserStorage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@Import({UserServiceImpl.class, SimpleUserMapperImpl.class})
@ActiveProfiles("test")
public class UserServiceIntegrationTest {
    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserStorage userStorage;

    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userDto = new UserDto();
        userDto.setName("Андрей");
        userDto.setEmail("somel@mail.ru");
    }

    @AfterEach
    void afterEach() {
        userStorage.deleteAll();
    }


    @DisplayName("Пользователь успешно создается")
    @Test
    void shouldCreateUser() {

        UserDto createdUserDto = userService.createUser(userDto);

        assertNotNull(createdUserDto.getId());
        assertEquals(userDto.getName(), createdUserDto.getName());
        assertEquals(userDto.getEmail(), createdUserDto.getEmail());
    }

    @DisplayName("Пользователь успешно возвращается по Id")
    @Test
    void shouldGetUserById() {

        UserDto createdUserDto = userService.createUser(userDto);
        User retrievedUser = userService.getUserById(createdUserDto.getId());

        assertNotNull(retrievedUser);
        assertEquals(createdUserDto.getId(), retrievedUser.getId());
        assertEquals(createdUserDto.getName(), retrievedUser.getName());
    }

    @DisplayName("Пользователь обновляется")
    @Test
    void shouldUpdateUser() {
        UserDto createdUserDto = userService.createUser(userDto);
        Integer userId = createdUserDto.getId();

        UserDto updateDto = new UserDto();
        updateDto.setId(userId);
        updateDto.setName("Меняем имя");

        UserDto updatedUserDto = userService.updateUser(updateDto);
        User userInDb = userStorage.findById(userId).orElseThrow();

        assertNotNull(updatedUserDto);
        assertEquals("Меняем имя", updatedUserDto.getName());
        assertEquals(createdUserDto.getEmail(), updatedUserDto.getEmail()); // Email остался прежним
        assertEquals("Меняем имя", userInDb.getName());
    }
}