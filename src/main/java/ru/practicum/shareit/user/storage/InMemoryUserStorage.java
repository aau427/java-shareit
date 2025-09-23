package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class InMemoryUserStorage implements UserStorage {
    private Map<Integer, User> usersMap;

    public InMemoryUserStorage() {
        this.usersMap = new HashMap<>(100);
    }

    @Override
    public Optional<User> getUserById(Integer userId) {
        return Optional.ofNullable(usersMap.get(userId));
    }

    @Override
    public List<User> getUserList() {
        return usersMap.values().stream().toList();
    }

    @Override
    public User createUser(User user) {
        user.setId(usersMap.size() + 1);
        usersMap.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        usersMap.put(user.getId(), user);
        return user;
    }

    @Override
    public void deleteUser(int userId) {
        usersMap.remove(userId);
    }
}
