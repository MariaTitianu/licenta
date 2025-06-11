package dsrl.mariatitianu.security.dto.user.extra;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserExtraCreateDTO {
    private String name;
    private String username;
}
