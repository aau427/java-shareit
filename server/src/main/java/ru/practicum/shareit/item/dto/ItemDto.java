package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class ItemDto {
    @Setter
    @Getter
    private Integer id;
    @NotBlank(message = "Не указано наименование вещи")
    @Size(max = 50)
    private String name;
    @NotBlank(message = "Не указано описание вещи")
    @Size(max = 50)
    private String description;
    @NotNull(message = "Не указана доступна ли вещь")
    private Boolean available;
    @Setter
    private Integer owner;
    @Setter
    private Integer requestId;
}
