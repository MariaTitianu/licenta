package dsrl.mariatitianu.security.controller;

import dsrl.mariatitianu.security.dto.device.DeviceCreateDTO;
import dsrl.mariatitianu.security.dto.device.DeviceDTO;
import dsrl.mariatitianu.security.dto.device.DeviceUpdateDTO;
import dsrl.mariatitianu.security.dto.device.UserDevicesDTO;
import dsrl.mariatitianu.security.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@CrossOrigin
@RestController
@RequestMapping("/devices")
@RequiredArgsConstructor
public class DeviceController {
    private final DeviceService deviceService;

    @GetMapping
    ResponseEntity<List<DeviceDTO>> readAll(){
        return deviceService.findAll();
    }

    @GetMapping("/users/{uuid}")
    ResponseEntity<UserDevicesDTO> readAllByUsername(@PathVariable UUID uuid){
        return deviceService.findAllByUsername(uuid);
    }

    @GetMapping("/{uuid}")
    ResponseEntity<DeviceDTO> read(@PathVariable UUID uuid){
        ResponseEntity<DeviceDTO> responseEntity = deviceService.find(uuid);
        System.out.println("responseEntity = " + responseEntity);
        return responseEntity;
    }

    @PostMapping
    ResponseEntity<DeviceDTO> create(@RequestBody DeviceCreateDTO dto){
        return deviceService.create(dto);
    }

    @PatchMapping("/{uuid}")
    ResponseEntity<DeviceDTO> update(@PathVariable UUID uuid, @RequestBody DeviceUpdateDTO dto){
        return deviceService.update(uuid, dto);
    }

    @DeleteMapping("/{uuid}")
    ResponseEntity<DeviceDTO> delete(@PathVariable UUID uuid){
        return deviceService.delete(uuid);
    }
}
