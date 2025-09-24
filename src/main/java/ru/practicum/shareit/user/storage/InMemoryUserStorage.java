package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> usersMap;
    private final Set<String> mailSet;

    public InMemoryUserStorage() {
        this.usersMap = new HashMap<>();
        this.mailSet = new HashSet<>();
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
        usersMap.put(user.getId(), user);
        mailSet.add(user.getEmail());
        return user;
    }

    @Override
    public User updateUser(User user) {
        String oldMail = usersMap.get(user.getId()).getEmail();
        usersMap.put(user.getId(), user);
        if (!oldMail.equals(user.getEmail())) {
            mailSet.remove(oldMail);
            mailSet.add(user.getEmail());
        }
        return user;
    }

    @Override
    public boolean isMailExists(String eMail) {
        return mailSet.contains(eMail);
    }

    @Override
    public void deleteUser(int userId) {
        String userMail = usersMap.get(userId).getEmail();
        usersMap.remove(userId);
        mailSet.remove(userMail);
    }
}
