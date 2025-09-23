package ru.practicum.shareit.user.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Builder
@EqualsAndHashCode(of = {"id"})
@Getter
@Setter
public class User implements Cloneable {
    private Integer id;
    private String name;
    private String email;

    @Override
    public User clone() {
        return User.builder()
                .id(this.id)
                .name(this.name)
                .email(this.email)
                .build();
    }
}
