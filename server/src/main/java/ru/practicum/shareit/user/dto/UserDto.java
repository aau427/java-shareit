package ru.practicum.shareit.user.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(of = {"id"})
@Setter
@Getter
public class UserDto {
    private Integer id;
    private String name;
    private String email;
}
