package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    Optional<User> getUserById(Integer userId);

    List<User> getUserList();

    User createUser(User user);

    void deleteUser(int userId);

    User updateUser(User user);
}
