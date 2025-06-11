package dsrl.mariatitianu.devicemanagement.service;

import dsrl.mariatitianu.devicemanagement.dto.user.UserDTO;

import java.util.Optional;
import java.util.UUID;

public interface UserService {
    Optional<UserDTO> addUser(UserDTO dto);
    Optional<UserDTO> find(UUID uuid);
}

