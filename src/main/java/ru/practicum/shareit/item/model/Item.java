package ru.practicum.shareit.item.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

@Builder
@Getter
@Setter
@EqualsAndHashCode(of = {"id"})
public class Item implements Cloneable {
    private int id;
    private String name;
    private String description;
    private Boolean available;
    private User owner;
    private ItemRequest request;


    @Override
    public Item clone() {
        return Item.builder()
                .id(this.id)
                .name(this.name)
                .description(this.description)
                .available(this.available)
                .owner(this.owner)
                .request(this.request)
                .build();
    }
}
