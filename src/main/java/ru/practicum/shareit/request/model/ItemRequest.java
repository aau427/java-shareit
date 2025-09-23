package ru.practicum.shareit.request.model;

import lombok.Getter;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDate;

@Getter
public class ItemRequest {
    private int id;
    private String description;
    private User requestor;
    private LocalDate created;
}
