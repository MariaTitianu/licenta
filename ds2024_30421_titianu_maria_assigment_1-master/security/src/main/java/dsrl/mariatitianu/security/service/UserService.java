package dsrl.mariatitianu.security.service;


import dsrl.mariatitianu.security.dto.user.*;
import dsrl.mariatitianu.security.enums.UserRole;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface UserService {
    ResponseEntity<List<UserDTO>> findAll();
    ResponseEntity<List<UserDTO>> findUsersWithRole(UserRole userRole);
    ResponseEntity<UserDTO> findUser(String username);
    List<UserRole> findUserRoles();
    ResponseEntity<UserDTO> createUser(UserCreateDTO dto);
    ResponseEntity<UserDTO> updateUser(String username, UserUpdateDTO dto);
    ResponseEntity<UserDTO> deleteUser(String username);
}
