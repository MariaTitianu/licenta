package dsrl.mariatitianu.monitoring.service;

import dsrl.mariatitianu.monitoring.dto.device.HourlyMeasurementDTO;
import dsrl.mariatitianu.monitoring.dto.graph.GraphRequestDTO;
import dsrl.mariatitianu.monitoring.entity.HourlyMeasurement;
import dsrl.mariatitianu.monitoring.mapper.HourlyMeasurementMapper;
import dsrl.mariatitianu.monitoring.repository.HourlyMeasurementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HourlyMeasurementServiceImpl implements HourlyMeasurementService{
    private final HourlyMeasurementRepository hourlyMeasurementRepository;
    private final HourlyMeasurementMapper hourlyMeasurementMapper;

    @Override
    public List<HourlyMeasurementDTO> getDeviceHourlyMeasurements(GraphRequestDTO dto) {
        LocalDateTime startLocalDateTime = LocalDateTime.of(LocalDate.parse(dto.getDate()), LocalTime.MIN);
        LocalDateTime endLocalDateTime = LocalDateTime.of(LocalDate.parse(dto.getDate()).plusDays(1), LocalTime.MIN);
        List<HourlyMeasurementDTO> hourlyMeasurementsDTOs =
                hourlyMeasurementMapper.toHourlyMeasurementDTOS(
                        hourlyMeasurementRepository.findAllByDeviceUuidAndDateTimeBetween(
                                dto.getDeviceUuid(),
                                startLocalDateTime,
                                endLocalDateTime
                        )
                );
        for (LocalDateTime localDateTime = startLocalDateTime; localDateTime.isBefore(endLocalDateTime); localDateTime = localDateTime.plusHours(1)) {
            LocalDateTime finalLocalDateTime = localDateTime;
            if (hourlyMeasurementsDTOs.stream().noneMatch(deviceMeasurement -> deviceMeasurement.getDateTime().equals(finalLocalDateTime))) {
                hourlyMeasurementsDTOs.add(HourlyMeasurementDTO.builder()
                        .deviceUuid(dto.getDeviceUuid())
                        .dateTime(localDateTime)
                        .measurement(-1.0)
                        .build()
                );
            }
        }
        Collections.sort(hourlyMeasurementsDTOs);

        return hourlyMeasurementsDTOs;
    }
}
