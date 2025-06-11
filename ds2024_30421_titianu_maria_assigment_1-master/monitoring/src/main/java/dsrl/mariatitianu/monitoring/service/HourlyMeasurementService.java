package dsrl.mariatitianu.monitoring.service;

import dsrl.mariatitianu.monitoring.dto.device.HourlyMeasurementDTO;
import dsrl.mariatitianu.monitoring.dto.graph.GraphRequestDTO;

import java.util.List;

public interface HourlyMeasurementService {
    List<HourlyMeasurementDTO> getDeviceHourlyMeasurements(GraphRequestDTO dto);
}
