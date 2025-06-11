package dsrl.mariatitianu.security.service;


import dsrl.mariatitianu.security.dto.auth.JwtDTO;
import dsrl.mariatitianu.security.dto.auth.UserLoginDTO;

import java.util.Optional;

public interface AuthService {
    Optional<JwtDTO> login(UserLoginDTO dto);
}
