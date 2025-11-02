package ru.practicum.shareit.item;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemShortOutDto;
import ru.practicum.shareit.item.mapper.SimpleItemMapperImpl;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class SimpleItemMapperImlTest {
    @Autowired
    private SimpleItemMapperImpl itemMapper;

    @Autowired
    UserStorage userStorage;
    @Autowired
    ItemRequestService itemRequestService;

    private User owner;
    private Item item;
    private ItemDto itemDto;

    @BeforeEach
    void beforeEach() {
        owner = new User();
        owner.setId(1);
        owner.setName("name");
        owner.setEmail("name@mail.ru");

        item = new Item();
        item.setId(1);
        item.setName("itemname");
        item.setDescription("itemdescription");
        item.setOwner(owner);
        item.setAvailable(true);

        itemDto = ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .owner(owner.getId())
                .available(item.getAvailable())
                .build();


    }

    @AfterEach
    void afterEach() {
        userStorage.deleteAll();
    }

    @Test
    @DisplayName("Корректно конвертирует item в DTO")
    void shouldConvertItemToDto() {
        ItemDto actualDto = itemMapper.itemToDto(item);
        ItemDto expectedDto = itemDto;

        assertNotNull(actualDto);
        assertEquals(actualDto.getId(), expectedDto.getId());
        assertEquals(actualDto.getName(), expectedDto.getName());
        assertEquals(actualDto.getDescription(), expectedDto.getDescription());
        assertEquals(actualDto.getOwner(), expectedDto.getOwner());
        assertEquals(actualDto.getAvailable(), expectedDto.getAvailable());
    }

    @Test
    @DisplayName("Корректно конвертирует DTO в Item")
    @Transactional
    void shouldConvertDtoToItem() {
        userStorage.save(owner);
        Item actualItem = itemMapper.dtoToItem(itemDto, itemRequestService);
        Item expectedItem = item;

        assertNotNull(actualItem);
        assertEquals(actualItem.getId(), expectedItem.getId());
        assertEquals(actualItem.getName(), expectedItem.getName());
        assertEquals(actualItem.getDescription(), expectedItem.getDescription());
        assertEquals(actualItem.getOwner(), expectedItem.getOwner());
        assertEquals(actualItem.getAvailable(), expectedItem.getAvailable());
    }

    @DisplayName("Корректно конвертирует в shortDto")
    @Test
    void shouldConvertItemToShortDto() {
        ItemShortOutDto expectedDto = new ItemShortOutDto();
        expectedDto.setId(item.getId());
        expectedDto.setName(item.getName());
        expectedDto.setDescription(item.getDescription());
        expectedDto.setAvailable(item.getAvailable());

        ItemShortOutDto actualDto = itemMapper.toItemShortDto(item);

        assertEquals(actualDto.getId(), expectedDto.getId());
        assertEquals(actualDto.getName(), expectedDto.getName());
        assertEquals(actualDto.getDescription(), expectedDto.getDescription());
        assertEquals(actualDto.getAvailable(), expectedDto.getAvailable());
    }
}
