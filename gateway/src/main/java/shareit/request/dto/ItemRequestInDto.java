package shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ItemRequestInDto {
    private int userId;
    @NotBlank(message = "Не указано описание запроса!")
    private String description;
}
