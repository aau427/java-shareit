package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ItemRequestInDto {
    private int userId;
    @NotBlank(message = "Не указано описание запроса!")
    private String description;
}
