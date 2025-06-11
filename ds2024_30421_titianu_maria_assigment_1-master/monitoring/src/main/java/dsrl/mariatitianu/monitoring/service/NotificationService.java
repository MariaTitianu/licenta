package dsrl.mariatitianu.monitoring.service;

import dsrl.mariatitianu.monitoring.dto.device.HourlyMeasurementDTO;
import dsrl.mariatitianu.monitoring.entity.HourlyMeasurement;

import java.util.UUID;

public interface NotificationService {
    void sendNotification(UUID userUuid, HourlyMeasurementDTO hourlyMeasurementDTO);
}
