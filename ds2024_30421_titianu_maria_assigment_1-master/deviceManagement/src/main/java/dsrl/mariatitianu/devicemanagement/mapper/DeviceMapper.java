package dsrl.mariatitianu.devicemanagement.mapper;

import dsrl.mariatitianu.devicemanagement.dto.device.DeviceCreateDTO;
import dsrl.mariatitianu.devicemanagement.dto.device.DeviceDTO;
import dsrl.mariatitianu.devicemanagement.dto.device.DeviceUpdateDTO;
import dsrl.mariatitianu.devicemanagement.dto.device.MonitoringDeviceDTO;
import dsrl.mariatitianu.devicemanagement.entity.Device;
import dsrl.mariatitianu.devicemanagement.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Component
@RequiredArgsConstructor
public class DeviceMapper {

    public Device toDevice(DeviceCreateDTO dto, User user) {
        return Device.builder()
                .id(UUID.randomUUID())
                .user(user)
                .name(dto.getName())
                .description(dto.getDescription())
                .maxHourConsumption(dto.getMaxHourConsumption())
                .address(dto.getAddress())
                .build();
    }

    public DeviceDTO toDeviceDTO(Device device) {
        return DeviceDTO.builder()
                .uuid(device.getId())
                .userUuid(device.getUser().getId())
                .name(device.getName())
                .address(device.getAddress())
                .description(device.getDescription())
                .maxHourConsumption(device.getMaxHourConsumption())
                .build();
    }
    public MonitoringDeviceDTO toMonitoringDeviceDTO(Device device){
        return MonitoringDeviceDTO.builder()
                .deviceUuid(device.getId())
                .userUuid(device.getUser().getId())
                .maxHourConsumption(device.getMaxHourConsumption())
                .build();
    }

    public Device updateDevice(Device device, DeviceUpdateDTO dto, Optional<User> user) {
        user.ifPresent(device::setUser);
        if (dto.getName() != null && !dto.getName().isEmpty()) {
            device.setName(dto.getName());
        }
        if (dto.getDescription() != null && !dto.getDescription().isEmpty()) {
            device.setDescription(dto.getDescription());
        }
        if (dto.getAddress() != null && !dto.getAddress().isEmpty()) {
            device.setAddress(dto.getAddress());
        }
        if (0 < dto.getMaxHourConsumption()) {
            device.setMaxHourConsumption(dto.getMaxHourConsumption());
        }
        return device;
    }

    public List<DeviceDTO> toDeviceDTOs(Collection<Device> all) {
        return all.stream()
                .map(this::toDeviceDTO)
                .toList();
    }
}
