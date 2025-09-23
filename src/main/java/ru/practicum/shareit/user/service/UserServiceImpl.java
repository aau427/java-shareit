package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.CustomValidationException;
import ru.practicum.shareit.exceptions.ResourceNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    @Override
    public User getUserById(int userId) {
        Optional<User> userOptional = userStorage.getUserById(userId);
        if (userOptional.isEmpty()) {
            String msg = "Не нашел пользователя с Id = " + userId;
            log.error(msg);
            throw new ResourceNotFoundException(msg);
        }
        return userOptional.get().clone();
    }

    @Override
    public UserDto getUserDtoById(int userId) {
        return UserMapper.userToDto(getUserById(userId));
    }

    @Override
    public List<UserDto> getUserList() {
        return userStorage.getUserList().stream()
                .map(UserMapper::userToDto)
                .toList();
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        if (userDto.getId() != null) {
            String message = String.format("Ошибка при создании пользователя: указан Id = %d", userDto.getId());
            log.error(message);
            throw new CustomValidationException(message);
        }
        User user = UserMapper.dtoToUser(userDto);
        validateNewUser(user);
        return UserMapper.userToDto(userStorage.createUser(user));
    }

    @Override
    public UserDto updateUser(UserDto userDto) {
        User user = getUserById(userDto.getId());
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
            validateExistingUser(user);
        }
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        return UserMapper.userToDto(userStorage.updateUser(user));
    }

    @Override
    public void deleteUser(int userId) {
        User user = getUserById(userId);
        userStorage.deleteUser(userId);
    }

    private void validateNewUser(final User user) {
        Optional<User> optionalUser = userStorage.getUserList().stream()
                .filter(user1 -> user1.getEmail().equals(user.getEmail()))
                .findFirst();
        if (optionalUser.isPresent()) {
            String message = String.format("Два и более пользователя не могут иметь один и тот же адрес электронной почты (%s)",
                    user.getEmail());
            log.error(message);
            throw new CustomValidationException(message);
        }
    }

    private void validateExistingUser(final User user) {
        Optional<User> optionalUser = userStorage.getUserList().stream()
                .filter(user1 -> user1.getEmail().equals(user.getEmail()) && !user1.getId().equals(user.getId()))
                .findFirst();
        if (optionalUser.isPresent()) {
            String message = String.format("Два и более пользователя не могут иметь один и тот же адрес электронной почты (%s)",
                    user.getEmail());
            log.error(message);
            throw new CustomValidationException(message);
        }
    }
}
