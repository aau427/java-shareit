package ru.practicum.shareit.item.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

public interface ItemStorage extends JpaRepository<Item, Integer> {
    @Query(nativeQuery = true, value = "SELECT * FROM ITEMS " +
            "WHERE (UPPER(NAME) LIKE '%' ||  ?1 || '%' OR " +
            "UPPER(DESCRIPTION) LIKE '%' ||  ?1 || '%') AND " +
            "IS_AVIALABLE = TRUE")
    List<Item> contextSearch(String context);

    List<Item> findAllByOwner(User userById);

    @Query(value = "SELECT B.item FROM Booking B WHERE B.item.id = ?1 AND B.status = 'APPROVED' AND B.booker.id = ?2 and B.end < ?3")
    List<Item> getItemsWasCompleteBookingByUser(Integer itemId, Integer userId, LocalDateTime created);

    List<Item> findAllByRequestInOrderById(List<ItemRequest> itemRequestList);

    List<Item> findAllByRequestOrderById(ItemRequest itemRequest);
}
