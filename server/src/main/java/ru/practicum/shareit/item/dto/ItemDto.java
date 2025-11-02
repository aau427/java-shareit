package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ItemDto {
    private Integer id;
    @NotBlank(message = "Не указано наименование вещи")
    @Size(max = 50)
    private String name;
    @NotBlank(message = "Не указано описание вещи")
    @Size(max = 50)
    private String description;
    @NotNull(message = "Не указана доступна ли вещь")
    private Boolean available;
    private Integer owner;
    private Integer requestId;
}
