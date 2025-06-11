package dsrl.mariatitianu.monitoring.mapper;

import dsrl.mariatitianu.monitoring.dto.device.HourlyMeasurementDTO;
import dsrl.mariatitianu.monitoring.entity.HourlyMeasurement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class HourlyMeasurementMapper {
    public HourlyMeasurementDTO toHourlyMeasurementDTO(HourlyMeasurement hourlyMeasurement) {
        return HourlyMeasurementDTO.builder()
                .deviceUuid(hourlyMeasurement.getDeviceUuid())
                .dateTime(hourlyMeasurement.getDateTime())
                .measurement(hourlyMeasurement.getMeasurement())
                .build();
    }

    public List<HourlyMeasurementDTO> toHourlyMeasurementDTOS(List<HourlyMeasurement> hourlyMeasurements) {
        return new ArrayList<>(hourlyMeasurements.stream()
                .map(this::toHourlyMeasurementDTO)
                .toList());
    }
}
