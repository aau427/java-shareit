package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class UserDto {
    private Integer id;
    @NotBlank(message = "Не указано имя пользователя")
    private String name;
    @NotEmpty(message = "Не указан e-mail")
    @Email(message = "Некорректный e-mail")
    private String email;
}
