package dsrl.mariatitianu.monitoring.repository;

import dsrl.mariatitianu.monitoring.entity.HourlyMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface HourlyMeasurementRepository extends JpaRepository<HourlyMeasurement, UUID> {
    List<HourlyMeasurement> findAllByDeviceUuidAndDateTimeBetween(UUID deviceUuid, LocalDateTime startTime, LocalDateTime endTime);
}
