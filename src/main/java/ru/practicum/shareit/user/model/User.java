package ru.practicum.shareit.user.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(of = {"id"})
@Getter
@Setter
public class User {
    private Integer id;
    private String name;
    private String email;
}
