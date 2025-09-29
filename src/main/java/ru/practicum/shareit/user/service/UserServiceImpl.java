package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.CustomValidationException;
import ru.practicum.shareit.exceptions.ResourceNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.SimpleUserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;
    private final SimpleUserMapper userMapper;

    @Override
    public User getUserById(int userId) {
        Optional<User> userOptional = userStorage.getUserById(userId);
        if (userOptional.isEmpty()) {
            log.error("Не нашел пользователя с Id = {}", userId);
            throw new ResourceNotFoundException(String.format("Не нашел пользователя с Id = %d", userId));
        }
        return userOptional.get();
    }

    @Override
    public UserDto getUserDtoById(int userId) {
        return userMapper.userToDto(getUserById(userId));
    }

    @Override
    public List<UserDto> getUserList() {
        return userStorage.getUserList().stream()
                .map(userMapper::userToDto)
                .toList();
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        if (userDto.getId() != null) {
            log.error("Ошибка при создании пользователя: указан Id = {}", userDto.getId());
            throw new CustomValidationException(String.format("Ошибка при создании пользователя: указан Id = %d", userDto.getId()));
        }
        User user = userMapper.dtoToUser(userDto);
        user.setId(userStorage.getUserList().size() + 1);
        validateMail(user.getEmail());
        return userMapper.userToDto(userStorage.createUser(user));
    }

    @Override
    public UserDto updateUser(UserDto userDto) {
        User oldUser = getUserById(userDto.getId());
        User newUser = userMapper.dtoToUser(userDto);
        /*изменять можно только имя и e-mail, причем во входящем DTO
        в наличии только те поля, которые действительно изменяются*/
        if (newUser.getEmail() != null) {
            validateMail(newUser.getEmail());
        } else {
            newUser.setEmail(oldUser.getEmail());
        }
        if (newUser.getName() == null) {
            newUser.setName(oldUser.getName());
        }
        return userMapper.userToDto(userStorage.updateUser(newUser));
    }

    @Override
    public void deleteUser(int userId) {
        User user = getUserById(userId);
        userStorage.deleteUser(userId);
    }

    private void validateMail(final String eMail) {
        if (userStorage.isMailExists(eMail)) {
            log.error("Два и более пользователя не могут иметь один и тот же адрес электронной почты: {}", eMail);
            throw new CustomValidationException(String.format("Два и более пользователя не могут иметь один и тот же адрес электронной почты: %s", eMail));
        }
    }
}
