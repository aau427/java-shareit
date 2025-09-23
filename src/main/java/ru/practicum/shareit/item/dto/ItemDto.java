package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.request.model.ItemRequest;

@Getter
@Builder
public class ItemDto {
    @Setter
    @Getter
    private Integer id;
    @NotBlank(message = "Не указано наименование вещи")
    private String name;
    @NotBlank(message = "Не указано описание вещи")
    private String description;
    @NotNull(message = "Не указана доступна ли вещь")
    private Boolean available;
    @Setter
    private Integer owner;
    @Setter
    private Integer request;
}
