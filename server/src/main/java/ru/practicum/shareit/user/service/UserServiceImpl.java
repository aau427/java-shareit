package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.CustomValidationException;
import ru.practicum.shareit.exceptions.ResourceNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.SimpleUserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;
    private final SimpleUserMapper userMapper;

    @Override
    public User getUserById(int userId) {
        return userStorage.findById(userId).orElseThrow(() -> {
            log.error("Не нашел пользователя с Id = {}", userId);
            throw new ResourceNotFoundException(String.format("Не нашел пользователя с Id = %d", userId));
        });

    }

    @Override
    public UserDto getUserDtoById(int userId) {
        return userMapper.userToDto(getUserById(userId));
    }

    @Override
    public List<UserDto> getUserList() {
        return userStorage.findAll().stream()
                .map(userMapper::userToDto)
                .toList();
    }

    @Transactional()
    @Override
    public UserDto createUser(UserDto userDto) {
        if (userDto.getId() != null) {
            log.error("Ошибка при создании пользователя: указан Id = {}", userDto.getId());
            throw new CustomValidationException(String.format("Ошибка при создании пользователя: указан Id = %d", userDto.getId()));
        }
        User user = userMapper.dtoToUser(userDto);
        return userMapper.userToDto(userStorage.save(user));
    }

    @Transactional
    @Override
    public UserDto updateUser(UserDto userDto) {
        User oldUser = getUserById(userDto.getId());
        User newUser = userMapper.dtoToUser(userDto);
        /*изменять можно только имя и e-mail, причем во входящем DTO
        в наличии только те поля, которые действительно изменяются*/
        if (newUser.getEmail() == null) {
            newUser.setEmail(oldUser.getEmail());
        }
        if (newUser.getName() == null) {
            newUser.setName(oldUser.getName());
        }
        return userMapper.userToDto(userStorage.save(newUser));
    }

    @Transactional
    @Override
    public void deleteUser(int userId) {
        User user = getUserById(userId);
        userStorage.deleteById(userId);
    }
}
