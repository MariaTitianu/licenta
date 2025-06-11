package dsrl.mariatitianu.security.service;

import dsrl.mariatitianu.security.dto.device.DeviceCreateDTO;
import dsrl.mariatitianu.security.dto.device.DeviceDTO;
import dsrl.mariatitianu.security.dto.device.DeviceUpdateDTO;
import dsrl.mariatitianu.security.dto.device.UserDevicesDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

public interface DeviceService {
    ResponseEntity<List<DeviceDTO>> findAll();
    ResponseEntity<DeviceDTO> find(UUID uuid);
    ResponseEntity<DeviceDTO> create(DeviceCreateDTO dto);
    ResponseEntity<DeviceDTO> update(UUID uuid, DeviceUpdateDTO dto);
    ResponseEntity<DeviceDTO> delete(UUID uuid);
    ResponseEntity<UserDevicesDTO> findAllByUsername(UUID uuid);
}
