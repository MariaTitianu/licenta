package dsrl.mariatitianu.devicemanagement.controller;

import dsrl.mariatitianu.devicemanagement.dto.device.DeviceDTO;
import dsrl.mariatitianu.devicemanagement.dto.user.UserDTO;
import dsrl.mariatitianu.devicemanagement.service.DeviceService;
import dsrl.mariatitianu.devicemanagement.service.UserService;
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
import java.util.UUID;

@CrossOrigin
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final DeviceService deviceService;

    @Operation(summary = "Add a new user", description = "Adds a new user based on the provided data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User added",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = DeviceDTO.class))}),
            @ApiResponse(responseCode = "409", description = "User already exists")
    })
    @PostMapping
    public ResponseEntity<UserDTO> add(@RequestBody UserDTO dto) {
        Optional<UserDTO> userDTOOptional = userService.addUser(dto);
        return userDTOOptional
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    @Operation(summary = "Get user devices by uuid", description = "Retrieves all the devices for a given user by uuid")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = DeviceDTO.class))}),
            @ApiResponse(responseCode = "404", description = "Device not found")
    })
    @GetMapping("/{uuid}")
    public ResponseEntity<UserDTO> read(@PathVariable UUID uuid) {
        Optional<UserDTO> userDTOOptional = userService.find(uuid);
        return userDTOOptional
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Operation(summary = "Delete the user by uuid", description = "Deletes a device with the provided data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = DeviceDTO.class))}),
            @ApiResponse(responseCode = "404", description = "User not found, cannot delete")
    })
    @DeleteMapping("/{uuid}")
    public ResponseEntity<List<DeviceDTO>> deleteUser(@PathVariable UUID uuid) {
        Optional<List<DeviceDTO>> userDTOOptional = deviceService.deleteDevicesAndUser(uuid);
        return userDTOOptional
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
