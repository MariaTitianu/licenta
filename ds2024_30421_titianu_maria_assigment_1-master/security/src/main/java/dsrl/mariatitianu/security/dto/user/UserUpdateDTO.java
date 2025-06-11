package dsrl.mariatitianu.security.dto.user;

import dsrl.mariatitianu.security.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDTO {
    private String password;
    private String name;
    private UserRole role;
}
