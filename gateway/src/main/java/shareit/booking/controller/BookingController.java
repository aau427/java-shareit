package shareit.booking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shareit.booking.client.BookingClient;
import shareit.booking.dto.BookingDto;
import shareit.common.Common;


@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingClient bookingClient;

    @PostMapping()
    public ResponseEntity<Object> createBooking(@Valid @RequestBody BookingDto bookingDto,
                                                @RequestHeader(value = Common.USER_HEADER) Long bookerId) {

        return bookingClient.createBooking(bookerId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approveBooking(@RequestHeader(value = Common.USER_HEADER) Long userId,
                                                 @PathVariable("bookingId") Long bookingId,
                                                 @RequestParam(name = "approved") Boolean isApprove) {
        return bookingClient.updateBooking(userId, bookingId, isApprove);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBooking(@RequestHeader(value = Common.USER_HEADER) Long userId,
                                             @PathVariable("bookingId") Long bookingId) {
        return bookingClient.getBookingById(bookingId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getUsersBookings(@RequestHeader(value = Common.USER_HEADER) Long userId,
                                                   @RequestParam(name = "state", defaultValue = "ALL") String state) {
        return bookingClient.getUsersBooking(userId, state);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getOwnersBookings(@RequestHeader(value = Common.USER_HEADER) Long userId,
                                                    @RequestParam(name = "state", defaultValue = "ALL") String state) {
        return bookingClient.getBookingsForOwner(userId, state);
    }
}
