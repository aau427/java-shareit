package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDtoWithItems;
import ru.practicum.shareit.request.dto.ItemRequestInDto;
import ru.practicum.shareit.request.dto.ItemRequestOutDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestService {
    ItemRequestOutDto createRequest(ItemRequestInDto itemRequestDto);

    List<ItemRequestDtoWithItems> getRequestByUserId(Integer userId);

    List<ItemRequestDtoWithItems> getAllRequests();

    ItemRequestDtoWithItems getRequestDtoById(Integer requestId);

    ItemRequest getRequestById(Integer requestId);

}
