package dsrl.mariatitianu.monitoring.mapper;

import dsrl.mariatitianu.monitoring.dto.device.DeviceMeasurementDTO;
import dsrl.mariatitianu.monitoring.dto.device.MonitoringDeviceDTO;
import dsrl.mariatitianu.monitoring.entity.HourlyMeasurement;
import dsrl.mariatitianu.monitoring.entity.RabbitMeasurement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class RabbitMeasurementMapper {
    public RabbitMeasurement toRabbitMeasurement(DeviceMeasurementDTO dto){
        return RabbitMeasurement.builder()
                .dateTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(dto.getTimestamp()), ZoneId.systemDefault()))
                .deviceUuid(dto.getDeviceId())
                .measurement(dto.getValue())
                .build();
    }

    public HourlyMeasurement toHourlyMeasurement(RabbitMeasurement rabbitMeasurement, Double measurement){
        return HourlyMeasurement.builder()
                .deviceUuid(rabbitMeasurement.getDeviceUuid())
                .dateTime(rabbitMeasurement.getDateTime().truncatedTo(ChronoUnit.HOURS).minusHours(1))
                .measurement(measurement)
                .build();
    }
}
