package dsrl.mariatitianu.devicemanagement.controller;

import dsrl.mariatitianu.devicemanagement.dto.device.DeviceCreateDTO;
import dsrl.mariatitianu.devicemanagement.dto.device.DeviceDTO;
import dsrl.mariatitianu.devicemanagement.dto.device.DeviceUpdateDTO;
import dsrl.mariatitianu.devicemanagement.service.DeviceService;
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
@RequestMapping("/devices")
@RequiredArgsConstructor
public class DeviceController {
    private final DeviceService deviceService;

    @Operation(summary = "Get all devices", description = "Retrieves all devices")
    @ApiResponse(responseCode = "200", description = "List of devices",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = List.class))})
    @GetMapping
    public ResponseEntity<List<DeviceDTO>> readAll() {
        return ResponseEntity.ok(deviceService.findAll());
    }

    @Operation(summary = "Create a new device", description = "Creates a new device based on the provided data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Device created",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = DeviceDTO.class))}),
            @ApiResponse(responseCode = "409", description = "Device already exists")
    })
    @PostMapping
    public ResponseEntity<DeviceDTO> create(@RequestBody DeviceCreateDTO dto) {
        Optional<DeviceDTO> deviceDTOOptional = deviceService.create(dto);
        return deviceDTOOptional
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    @Operation(summary = "Get device by uuid", description = "Retrieves a device by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Device found",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = DeviceDTO.class))}),
            @ApiResponse(responseCode = "404", description = "Device not found")
    })
    @GetMapping("/{uuid}")
    public ResponseEntity<?> read(@PathVariable UUID uuid) {
        Optional<DeviceDTO> deviceDTOOptional = deviceService.find(uuid);
        return deviceDTOOptional
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Operation(summary = "Update device by uuid", description = "Updates a device with the provided data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Device updated",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = DeviceDTO.class))}),
            @ApiResponse(responseCode = "404", description = "Device not found, cannot update")
    })
    @PatchMapping("/{uuid}")
    public ResponseEntity<?> update(@PathVariable UUID uuid, @RequestBody DeviceUpdateDTO dto) {
        Optional<DeviceDTO> deviceDTOOptional = deviceService.update(uuid, dto);
        return deviceDTOOptional
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
    @Operation(summary = "Delete the device by uuid", description = "Deletes a device with the provided data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Device deleted",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = DeviceDTO.class))}),
            @ApiResponse(responseCode = "404", description = "Device not found, cannot delete")
    })
    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> delete(@PathVariable UUID uuid) {
        Optional<DeviceDTO> deviceDTOOptional = deviceService.delete(uuid);
        return deviceDTOOptional
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
