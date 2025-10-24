package ru.practicum.shareit.user.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(of = {"id"})
@Getter
@Setter
@Entity
@Table(name = "USERS")
public class User {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "NAME", length = 50, nullable = false)
    private String name;

    @Column(name = "EMAIL", length = 50, nullable = false, unique = true)
    private String email;
}
