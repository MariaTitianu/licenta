package dsrl.mariatitianu.security.controller;

import dsrl.mariatitianu.security.dto.auth.JwtDTO;
import dsrl.mariatitianu.security.dto.auth.UserLoginDTO;
import dsrl.mariatitianu.security.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    @PostMapping("/login")
    public ResponseEntity<JwtDTO> login(@RequestBody UserLoginDTO dto) {
        Optional<JwtDTO> jwtDTOOptional = authService.login(dto);
        return jwtDTOOptional
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
    }
}
