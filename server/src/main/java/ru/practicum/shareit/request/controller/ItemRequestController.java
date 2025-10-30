package ru.practicum.shareit.request.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.common.Common;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithItems;
import ru.practicum.shareit.request.dto.ItemRequestInDto;
import ru.practicum.shareit.request.dto.ItemRequestOutDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestOutDto createRequest(@RequestHeader(Common.USER_HEADER) Integer userId,
                                           @Valid @RequestBody ItemRequestInDto itemRequestDto) {
        itemRequestDto.setUserId(userId);
        return itemRequestService.createRequest(itemRequestDto);
    }

    //получить список своих запросов вместе с данными об ответах на них.
    @GetMapping
    public List<ItemRequestDtoWithItems> getRequest(@RequestHeader(Common.USER_HEADER) Integer userId) {
        return itemRequestService.getRequestByUserId(userId);
    }

    //получить список запросов, созданных другими пользователями.
    @GetMapping("/all")
    public List<ItemRequestDtoWithItems> getAll() {
        return itemRequestService.getAllRequests();
    }

    // Получить данные об одном конкретном запросе вместе с данными об ответах на него. Доступ любого пользователя.
    @GetMapping("/{requestId}")
    public ItemRequestDtoWithItems getById(@PathVariable Integer requestId) {
        return itemRequestService.getRequestDtoById(requestId);
    }
}
