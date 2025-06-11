package dsrl.mariatitianu.usermanagement.service;

import dsrl.mariatitianu.usermanagement.dto.UserCreateDTO;
import dsrl.mariatitianu.usermanagement.dto.UserExtraDTO;
import dsrl.mariatitianu.usermanagement.dto.UserUpdateDTO;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<UserExtraDTO> findAll();
    Optional<UserExtraDTO> find(String username);
    Optional<UserExtraDTO> create(UserCreateDTO dto);
    Optional<UserExtraDTO> update(String username, UserUpdateDTO dto);
    Optional<UserExtraDTO> delete(String username);
}
