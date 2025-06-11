package dsrl.mariatitianu.monitoring.service;

import dsrl.mariatitianu.monitoring.dto.device.DeviceDTO;
import dsrl.mariatitianu.monitoring.dto.device.MonitoringDeviceDTO;
import dsrl.mariatitianu.monitoring.entity.Device;
import dsrl.mariatitianu.monitoring.mapper.DeviceMapper;
import dsrl.mariatitianu.monitoring.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService{
    private final DeviceMapper deviceMapper;
    private final DeviceRepository deviceRepository;

    @Override
    public DeviceDTO create(MonitoringDeviceDTO dto) {
        Device device = deviceRepository.save(deviceMapper.toDevice(dto));
        return deviceMapper.toDeviceDTO(device);
    }

    @Override
    public Optional<DeviceDTO> update(UUID uuid, MonitoringDeviceDTO dto) {
        Optional<Device> oldDevice = deviceRepository.findById(uuid);
        if (oldDevice.isEmpty()) {
            return Optional.empty();
        }
        Device newDevice = deviceRepository.save(deviceMapper.updateDevice(oldDevice.get(), dto));
        return Optional.of(deviceMapper.toDeviceDTO(newDevice));
    }

    @Override
    public Optional<DeviceDTO> delete(UUID uuid) {
        Optional<Device> device = deviceRepository.findById(uuid);
        if (device.isEmpty()) {
            return Optional.empty();
        }
        deviceRepository.deleteById(device.get().getUuid());
        return Optional.of(deviceMapper.toDeviceDTO(device.get()));
    }
}
