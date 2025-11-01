package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.BaseUtility;
import ru.practicum.shareit.exceptions.CustomValidationException;
import ru.practicum.shareit.exceptions.ResourceNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.SimpleUserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest extends BaseUtility {

    @Mock
    private UserStorage userStorage;

    @Mock
    private SimpleUserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private User user1;
    private UserDto userDto;
    private UserDto userDto1;

    @BeforeEach
    void beforeEach() {
        user = getUser(1, "Андрей", "some@mail.ru");
        user1 = getUser(2, "Анастасия", "some@yandex.ru");
        userDto = createUserDto(1, "Андрей", "some@mail.ru");
        userDto1 = createUserDto(2, "Анастасия", "some@yandex.ru");
    }

    @DisplayName("Возвращает существующего пользователя")
    @Test
    void getUserByIdWhenUserFound() {
        when(userStorage.findById(1)).thenReturn(Optional.of(user));

        User result = userService.getUserById(1);

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        verify(userStorage, times(1)).findById(1);
    }

    @DisplayName("Выбрасывает исключительную ситуацию, если пользователь не найден")
    @Test
    void getUserById_whenUserNotFound() {
        when(userStorage.findById(666)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(666));
        verify(userStorage, times(1)).findById(666);
    }

    @DisplayName("Возвращает UserDto, если пользователь найден")
    @Test
    void getUserDtoById_whenUserFound() {
        when(userStorage.findById(1)).thenReturn(Optional.of(user));
        when(userMapper.userToDto(user)).thenReturn(userDto);

        UserDto result = userService.getUserDtoById(1);

        assertNotNull(result);
        assertEquals(userDto.getEmail(), result.getEmail());
        verify(userStorage, times(1)).findById(1);
        verify(userMapper, times(1)).userToDto(user);
    }

    @DisplayName("Возвращает список пользователей при их наличии")
    @Test
    void getUserList_whenUsersExist() {
        when(userStorage.findAll()).thenReturn(List.of(user, user1));
        when(userMapper.userToDto(user)).thenReturn(userDto);
        when(userMapper.userToDto(user1)).thenReturn(userDto1);

        List<UserDto> result = userService.getUserList();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(userDto.getEmail(), result.getFirst().getEmail());
        verify(userStorage, times(1)).findAll();
    }


    @DisplayName("Возвращает пустой список пользователей")
    @Test
    void getUserList_whenNoOneUsers() {
        when(userStorage.findAll()).thenReturn(Collections.emptyList());

        List<UserDto> result = userService.getUserList();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userStorage, times(1)).findAll();
    }


    @DisplayName("Создает пользователя")
    @Test
    void createUser() {
        User savedUser = user;
        User userToSave = getUserCopy(user);
        userToSave.setId(null);

        UserDto expectedDto = userDto;
        UserDto dtoToCreate = getUserDtoCopy(userDto);
        dtoToCreate.setId(null);

        when(userMapper.dtoToUser(dtoToCreate)).thenReturn(userToSave);
        when(userStorage.save(userToSave)).thenReturn(savedUser);
        when(userMapper.userToDto(savedUser)).thenReturn(expectedDto);

        UserDto result = userService.createUser(dtoToCreate);

        assertNotNull(result);
        assertEquals(1, result.getId(), "Вернули не того пользователя");
        verify(userStorage, times(1)).save(userToSave);
    }

    @DisplayName("Выбрасывает исключением, если создается пользователь с Id")
    @Test
    void shouldNotCreateUser_whenDtoHasId() {

        assertThrows(CustomValidationException.class, () -> userService.createUser(userDto));
        verify(userStorage, never()).save(any(User.class));
    }

    @DisplayName("Выбрасывает исключение при изменении несуществуюшего пользователя")
    @Test
    void updateUser_whenUserNotFound() {
        userDto.setId(666);
        when(userStorage.findById(666)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(userDto));
        verify(userStorage, never()).save(any(User.class));
    }

    @DisplayName("Обновляется имя и email")
    @Test
    void updateUser_whenUpdateNameAndEmail() {

        when(userStorage.findById(1)).thenReturn(Optional.of(user));
        when(userMapper.dtoToUser(userDto)).thenReturn(user);
        when(userStorage.save(any(User.class))).thenReturn(user);
        when(userMapper.userToDto(user)).thenReturn(userDto);

        UserDto result = userService.updateUser(userDto);

        assertNotNull(result);
        assertEquals("Андрей", result.getName());
        assertEquals("some@mail.ru", result.getEmail());
        verify(userStorage, times(1)).save(any(User.class));
    }

    @DisplayName("Обновляется только имя")
    @Test
    void updateOnlyName() {
        UserDto dtoForUpdate = getUserDtoCopy(userDto);
        dtoForUpdate.setEmail(null);


        User userWithoutEmail = getUserCopy(user);
        userWithoutEmail.setEmail(null);

        when(userStorage.findById(1)).thenReturn(Optional.of(user));
        when(userMapper.dtoToUser(dtoForUpdate)).thenReturn(userWithoutEmail);
        when(userStorage.save(any(User.class))).thenReturn(user);
        when(userMapper.userToDto(any(User.class))).thenReturn(userDto);

        UserDto result = userService.updateUser(dtoForUpdate);

        assertNotNull(result);
        assertEquals("Андрей", result.getName(), "при апдейте не изменили имя");
        assertEquals("some@mail.ru", result.getEmail(), "при апдейте потеряли email");
    }

    @DisplayName("Обновляется только email")
    @Test
    void updateOnlyEmail() {
        UserDto dtoForUpdate = getUserDtoCopy(userDto);
        dtoForUpdate.setName(null);


        User userWithoutName = getUserCopy(user);
        userWithoutName.setName(null);

        when(userStorage.findById(1)).thenReturn(Optional.of(user));
        when(userMapper.dtoToUser(dtoForUpdate)).thenReturn(userWithoutName);
        when(userStorage.save(any(User.class))).thenReturn(user);
        when(userMapper.userToDto(any(User.class))).thenReturn(userDto);

        UserDto result = userService.updateUser(dtoForUpdate);

        assertNotNull(result);
        assertEquals("Андрей", result.getName(), "при апдейте потеряли имя");
        assertEquals("some@mail.ru", result.getEmail(), "при апдейте не изменили email");
    }


    @DisplayName("Удаление существующего пользователя")
    @Test
    void deleteUser_whenUserExists() {
        doNothing().when(userStorage).deleteById(1);

        userService.deleteUser(1);

        verify(userStorage, times(1)).deleteById(1);
    }

    @DisplayName("Удаление несуществуюшего пользователя")
    @Test
    void deleteUser_whenUserDoesNotExist() {

        doNothing().when(userStorage).deleteById(666);

        assertDoesNotThrow(() -> userService.deleteUser(666));
        verify(userStorage, times(1)).deleteById(666);
    }

    private User getUser(int id, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    private User getUserCopy(User iUser) {
        User user = new User();
        user.setId(iUser.getId());
        user.setName(iUser.getName());
        user.setEmail(iUser.getEmail());
        return user;
    }

}