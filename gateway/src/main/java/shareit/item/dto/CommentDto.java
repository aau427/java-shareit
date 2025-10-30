package shareit.item.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentDto {
    private Integer id;
    @Size(max = 50)
    @NotEmpty(message = "комментарий не может быть пустым")
    private String text;
    private Integer itemId;
    private Integer authorId;
    private String authorName;
    private LocalDateTime created;
}
