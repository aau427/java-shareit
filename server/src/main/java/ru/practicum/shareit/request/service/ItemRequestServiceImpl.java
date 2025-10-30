package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.ResourceNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithItems;
import ru.practicum.shareit.request.dto.ItemRequestInDto;
import ru.practicum.shareit.request.dto.ItemRequestOutDto;
import ru.practicum.shareit.request.mapper.SimpleItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestStorage itemRequestStorage;
    private final ItemStorage itemStorage;
    private final SimpleItemRequestMapper itemRequestMapper;
    private final UserService userService;

    @Transactional
    @Override
    public ItemRequestOutDto createRequest(ItemRequestInDto itemRequestDto) {
        ItemRequest itemRequest = itemRequestMapper.dtoToItemRequest(itemRequestDto);
        return itemRequestMapper.itemRequestToDto(itemRequestStorage.save(itemRequest));
    }

    //получить список своих запросов вместе с данными об ответах на них.
    @Override
    public List<ItemRequestDtoWithItems> getRequestByUserId(Integer userId) {
        User user = userService.getUserById(userId);
        List<ItemRequest> itemRequestList = itemRequestStorage.findAllByRequesterOrderByCreatedDesc(user);
        Map<Integer, List<Item>> itemMapByRequestId = getItemsMap(itemRequestList);
        return getListOfItemsRequestsDto(itemRequestList, itemMapByRequestId);
    }

    //Получить список запросов, созданных другими пользователями.
    @Override
    public List<ItemRequestDtoWithItems> getAllRequests() {
        List<ItemRequest> itemRequestList = itemRequestStorage.findAll(Sort.by("created").descending());
        Map<Integer, List<Item>> itemMapByRequestId = getItemsMap(itemRequestList);
        return getListOfItemsRequestsDto(itemRequestList, itemMapByRequestId);
    }

    @Override
    public ItemRequest getRequestById(Integer requestId) {
        return itemRequestStorage.findById(requestId)
                .orElseThrow(() -> {
                    log.warn("Не нашел запрос с Id = {}", requestId);
                    throw new ResourceNotFoundException("Не нашел запрос с Id = " + requestId);
                });
    }

    @Override
    public ItemRequestDtoWithItems getRequestDtoById(Integer requestId) {
        ItemRequest itemRequest = getRequestById(requestId);
        List<Item> itemsList = itemStorage.findAllByRequestOrderById(itemRequest);
        return itemRequestMapper.itemToItemRequestWithItems(itemRequest, itemsList);
    }

    private Map<Integer, List<Item>> getItemsMap(List<ItemRequest> itemRequestList) {
        List<Item> itemList = itemStorage.findAllByRequestInOrderById(itemRequestList);
        return itemList.stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getRequester().getId()));
    }

    private List<ItemRequestDtoWithItems> getListOfItemsRequestsDto(List<ItemRequest> itemRequestList,
                                                                    Map<Integer, List<Item>> itemMapByRequestId) {
        return itemRequestList.stream()
                .map(itemRequest -> {
                    List<Item> itemList = itemMapByRequestId.get(itemRequest.getId());
                    if (itemList == null) {
                        itemList = new ArrayList<>();
                    }
                    return itemRequestMapper.itemToItemRequestWithItems(itemRequest, itemList);

                })
                .collect(Collectors.toList());
    }
}
