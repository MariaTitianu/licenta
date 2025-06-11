package dsrl.mariatitianu.monitoring.service;

import dsrl.mariatitianu.monitoring.dto.device.DeviceDTO;
import dsrl.mariatitianu.monitoring.dto.device.MonitoringDeviceDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceService {
    DeviceDTO create(MonitoringDeviceDTO dto);
    Optional<DeviceDTO> update(UUID uuid, MonitoringDeviceDTO dto);
    Optional<DeviceDTO> delete(UUID uuid);
}
