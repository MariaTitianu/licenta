package dsrl.mariatitianu.security.service.impl;

import dsrl.mariatitianu.security.dto.auth.JwtDTO;
import dsrl.mariatitianu.security.dto.auth.UserLoginDTO;
import dsrl.mariatitianu.security.entity.User;
import dsrl.mariatitianu.security.repository.UserRepository;
import dsrl.mariatitianu.security.service.AuthService;
import dsrl.mariatitianu.security.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public Optional<JwtDTO> login(UserLoginDTO dto) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()));
        Optional<User> user = userRepository.findById(dto.getUsername());
        return user.map(value -> JwtDTO.builder().token(jwtService.generateToken(value)).build());
    }
}
