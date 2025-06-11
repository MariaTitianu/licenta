package dsrl.mariatitianu.devicemanagement.service;


import dsrl.mariatitianu.devicemanagement.dto.device.DeviceCreateDTO;
import dsrl.mariatitianu.devicemanagement.dto.device.DeviceDTO;
import dsrl.mariatitianu.devicemanagement.dto.device.DeviceUpdateDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceService {
    Optional<DeviceDTO> create(DeviceCreateDTO dto);
    Optional<DeviceDTO> update(UUID uuid, DeviceUpdateDTO dto);
    List<DeviceDTO> findAll();
    Optional<DeviceDTO> find(UUID uuid);
    Optional<DeviceDTO> delete(UUID uuid);
    Optional<List<DeviceDTO>> deleteDevicesAndUser(UUID userUuid);
}

