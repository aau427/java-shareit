package ru.practicum.shareit.booking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.InputBookingDto;
import ru.practicum.shareit.booking.dto.OutputBookingDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;


@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping()
    public OutputBookingDto createBooking(@Valid @RequestBody InputBookingDto bookingDto, @RequestHeader(value = "X-Sharer-User-Id") Integer bookerId) {
        bookingDto.setBookerId(bookerId);
        return bookingService.createBooking(bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public OutputBookingDto approveBooking(@RequestHeader(value = "X-Sharer-User-Id") Integer userId,
                                           @PathVariable("bookingId") Integer bookingId, @RequestParam(name = "approved") Boolean isApprove) {
        return bookingService.updateBooking(userId, bookingId, isApprove);
    }

    @GetMapping("/{bookingId}")
    public OutputBookingDto getBooking(@RequestHeader(value = "X-Sharer-User-Id") Integer userId,
                                       @PathVariable("bookingId") Integer bookingId) {
        return bookingService.getBookingById(bookingId, userId);
    }

    @GetMapping
    public List<OutputBookingDto> getUsersBookings(@RequestHeader(value = "X-Sharer-User-Id") Integer userId,
                                                   @RequestParam(name = "state", defaultValue = "ALL") String state) {
        return bookingService.getUsersBooking(userId, state);
    }

    @GetMapping("/owner")
    public List<OutputBookingDto> getOwnersBookings(@RequestHeader(value = "X-Sharer-User-Id") Integer userId,
                                                    @RequestParam(name = "state", defaultValue = "ALL") String state) {
        return bookingService.getOwnersBookings(userId, state);
    }

}
