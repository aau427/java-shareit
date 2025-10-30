package shareit.request.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shareit.common.Common;
import shareit.request.client.RequestClient;
import shareit.request.dto.ItemRequestInDto;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final RequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> createRequest(@RequestHeader(Common.USER_HEADER) Long userId,
                                                @Valid @RequestBody ItemRequestInDto itemRequestDto) {
        return requestClient.createRequest(userId, itemRequestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getRequest(@RequestHeader(Common.USER_HEADER) Long userId) {
        return requestClient.getRequestsByUser(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAll(@RequestHeader(Common.USER_HEADER) Long userId) {
        return requestClient.getAll(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getById(@PathVariable Long requestId,
                                          @RequestHeader(Common.USER_HEADER) Long userId) {
        return requestClient.getItemRequestDtoById(requestId, userId);
    }
}
