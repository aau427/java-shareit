package ru.practicum.shareit.request.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface ItemRequestStorage extends JpaRepository<ItemRequest, Integer> {
    List<ItemRequest> findAllByRequesterOrderByCreatedDesc(User user);

    @Query(value = "SELECT IR FROM ItemRequest IR WHERE IR.requester.id <> ?1 ORDER BY IR.created DESC")
    List<ItemRequest> findAll(Integer userId);
}
