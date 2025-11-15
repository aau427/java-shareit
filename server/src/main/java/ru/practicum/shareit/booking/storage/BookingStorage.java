package ru.practicum.shareit.booking.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingStorage extends JpaRepository<Booking, Integer> {
    List<Booking> findBookingByBooker(User user);

    List<Booking> findBookingByBookerAndStatus(User user, BookingStatus status);

    List<Booking> findBookingByBookerAndStartBeforeAndEndAfter(User user, LocalDateTime dateTime,
                                                               LocalDateTime dateTime1);

    List<Booking> findBookingByBookerAndEndBefore(User user, LocalDateTime dateTime);

    List<Booking> findBookingByBookerAndStartAfter(User user, LocalDateTime dateTime);

    @Query(value = "SELECT B FROM Booking B WHERE B.item.owner.id = ?1 AND B.status = ?2 order by B.start desc")
    List<Booking> getBookingsForOwnerByStatus(Integer ownerId, BookingStatus bookingStatus);

    @Query(value = "SELECT B FROM Booking B WHERE B.item.owner.id = ?1 ORDER BY B.start DESC")
    List<Booking> getAllBookingsForOwner(Integer ownerId);

    @Query(value = "SELECT B FROM Booking B WHERE B.item.owner.id = ?1 AND B.start < ?2 AND B.end > ?3 ORDER BY B.start DESC")
    List<Booking> getCurrentBookingForOwner(Integer ownerId, LocalDateTime date1, LocalDateTime date2);

    @Query(value = "SELECT B FROM Booking B WHERE B.item.owner.id = ?1 AND B.end < ?2 ORDER BY B.start DESC")
    List<Booking> getPastBookingForOwner(Integer ownerId, LocalDateTime date);

    @Query(value = "SELECT B FROM Booking B WHERE B.item.owner.id = ?1 AND B.start > ?2 ORDER BY B.start DESC")
    List<Booking> getFutureBookingForOwner(Integer ownerId, LocalDateTime date);

    @Query(value = "SELECT B FROM Booking B WHERE B.item.id = ?1 AND B.status = 'APPROVED' AND B.item.owner.id = ?2 " +
            "ORDER BY B.end")
    List<Booking> getBookingsByItemOwner(Integer itemId, Integer userId);

    List<Booking> findAllByItemInAndStatusOrderByStart(List<Item> itemList, BookingStatus status);
}
