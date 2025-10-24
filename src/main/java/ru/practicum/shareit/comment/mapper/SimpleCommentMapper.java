package ru.practicum.shareit.comment.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.service.UserService;

@Mapper(componentModel = "spring")
public abstract class SimpleCommentMapper {
    @Mapping(target = "item", expression = "java(itemService.getItemById(commentDto.getItemId()))")
    @Mapping(target = "author", expression = "java(userService.getUserById(commentDto.getAuthorId()))")
    public abstract Comment dtoToComment(CommentDto commentDto, ItemService itemService, UserService userService);

    @Mapping(target = "itemId", expression = "java(comment.getItem().getId())")
    @Mapping(target = "authorId", expression = "java(comment.getAuthor().getId())")
    @Mapping(target = "authorName", expression = "java(comment.getAuthor().getName())")
    public abstract CommentDto commentToDto(Comment comment);
}
