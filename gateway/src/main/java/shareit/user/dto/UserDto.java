package shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(of = {"id"})
@Setter
@Getter
public class UserDto {
    private Integer id;
    @NotBlank(message = "Не указано имя пользователя")
    private String name;
    @NotEmpty(message = "Не указан e-mail")
    @Email(message = "Некорректный e-mail")
    private String email;
}
