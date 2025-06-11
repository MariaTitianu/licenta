package dsrl.mariatitianu.security.controller;


import dsrl.mariatitianu.security.dto.user.UserCreateDTO;
import dsrl.mariatitianu.security.dto.user.UserDTO;
import dsrl.mariatitianu.security.dto.user.UserUpdateDTO;
import dsrl.mariatitianu.security.enums.UserRole;
import dsrl.mariatitianu.security.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("CallToPrintStackTrace")
@CrossOrigin
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    ResponseEntity<List<UserDTO>> readAll(@RequestParam Optional<UserRole> role) {
        if (role.isPresent()) {
            return userService.findUsersWithRole(role.get());
        }
        else {
            return userService.findAll();
        }
    }

    @GetMapping("/{username}")
    ResponseEntity<UserDTO> read(@PathVariable String username) {
        return userService.findUser(username);
    }

    @GetMapping("/roles")
    ResponseEntity<List<UserRole>> readUserRoles(){
        List<UserRole> userRoleDTOList = userService.findUserRoles();
        return ResponseEntity.ok(userRoleDTOList);
    }

    @PostMapping
    ResponseEntity<UserDTO> createUser(@RequestBody UserCreateDTO dto) {
        return userService.createUser(dto);
    }

    @PatchMapping("/{username}")
    ResponseEntity<UserDTO> updateUser(@PathVariable String username, @RequestBody UserUpdateDTO dto) {
        return userService.updateUser(username, dto);
    }

    @DeleteMapping("/{username}")
    ResponseEntity<UserDTO> deleteUser(@PathVariable String username) {
        return userService.deleteUser(username);
    }
}
