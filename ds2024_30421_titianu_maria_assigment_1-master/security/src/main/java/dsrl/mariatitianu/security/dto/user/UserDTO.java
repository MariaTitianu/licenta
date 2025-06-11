package dsrl.mariatitianu.security.dto.user;

import dsrl.mariatitianu.security.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private UUID uuid;
    private String username;
    private String name;
    private UserRole role;
}
