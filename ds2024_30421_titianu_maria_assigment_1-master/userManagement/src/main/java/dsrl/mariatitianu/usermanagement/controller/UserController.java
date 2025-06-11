package dsrl.mariatitianu.usermanagement.controller;

import dsrl.mariatitianu.usermanagement.dto.UserCreateDTO;
import dsrl.mariatitianu.usermanagement.dto.UserExtraDTO;
import dsrl.mariatitianu.usermanagement.dto.UserUpdateDTO;
import dsrl.mariatitianu.usermanagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(summary = "Get all users (admin only)", description = "Retrieves all users (accessible only to admin)")
    @ApiResponse(responseCode = "200", description = "List of users",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = List.class))})
    @GetMapping
//    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<List<UserExtraDTO>> readAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @Operation(summary = "Get user by uuid", description = "Retrieves a user by uuid (accessible to everyone)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = UserExtraDTO.class))}),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{username}")
    public ResponseEntity<UserExtraDTO> read(@PathVariable String username) {
        Optional<UserExtraDTO> userDTOOptional = userService.find(username);
        return userDTOOptional
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Operation(summary = "Create a new user", description = "Creates a new user based on the provided data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = UserExtraDTO.class))}),
            @ApiResponse(responseCode = "409", description = "User already exists")
    })
    @PostMapping
    public ResponseEntity<UserExtraDTO> create(@RequestBody UserCreateDTO dto) {
        Optional<UserExtraDTO> userExtraDTOOptional = userService.create(dto);
        return userExtraDTOOptional
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    @Operation(summary = "Update user by uuid", description = "Updates a user with the provided data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = UserExtraDTO.class))}),
            @ApiResponse(responseCode = "404", description = "User not found, cannot update")
    })
    @PatchMapping("/{username}")
    public ResponseEntity<UserExtraDTO> update(@PathVariable String username, @RequestBody UserUpdateDTO dto) {
        Optional<UserExtraDTO> userDTOOptional = userService.update(username, dto);
        return userDTOOptional
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Operation(summary = "Delete user by uuid", description = "Delete a user with the provided data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = UserExtraDTO.class))}),
            @ApiResponse(responseCode = "404", description = "User not found, cannot delete")
    })
    @DeleteMapping("/{username}")
    public ResponseEntity<UserExtraDTO> delete(@PathVariable String username) {
        Optional<UserExtraDTO> userDTOOptional = userService.delete(username);
        return userDTOOptional
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
