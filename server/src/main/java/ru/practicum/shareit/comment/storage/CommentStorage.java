package ru.practicum.shareit.comment.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface CommentStorage extends JpaRepository<Comment, Integer> {

    List<Comment> findAllByItemInOrderByCreatedDesc(List<Item> itemList);
}
