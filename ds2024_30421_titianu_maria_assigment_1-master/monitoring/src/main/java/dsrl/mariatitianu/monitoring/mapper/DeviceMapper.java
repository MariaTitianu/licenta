package dsrl.mariatitianu.monitoring.mapper;

import dsrl.mariatitianu.monitoring.dto.device.DeviceDTO;
import dsrl.mariatitianu.monitoring.dto.device.MonitoringDeviceDTO;
import dsrl.mariatitianu.monitoring.entity.Device;
import dsrl.mariatitianu.monitoring.entity.HourlyMeasurement;
import dsrl.mariatitianu.monitoring.entity.RabbitMeasurement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeviceMapper {
    public Device toDevice(MonitoringDeviceDTO dto){
        return Device.builder()
                .uuid(dto.getDeviceUuid())
                .userUuid(dto.getUserUuid())
                .maxHour(dto.getMaxHourConsumption())
                .build();
    }

    public DeviceDTO toDeviceDTO(Device device){
        return DeviceDTO.builder()
                .uuid(device.getUuid())
                .userUuid(device.getUserUuid())
                .maxHour(device.getMaxHour())
                .build();
    }

    public Device updateDevice(Device device, MonitoringDeviceDTO dto) {
        if(dto.getUserUuid() != null && !dto.getUserUuid().toString().isEmpty()){
            device.setUserUuid(dto.getUserUuid());
        }
        if(0 < dto.getMaxHourConsumption()){
            device.setMaxHour(dto.getMaxHourConsumption());
        }
        return device;
    }
}
