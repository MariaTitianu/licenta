package dsrl.mariatitianu.monitoring.repository;

import dsrl.mariatitianu.monitoring.entity.RabbitMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.UUID;

@Repository
public interface RabbitMeasurementRepository extends JpaRepository<RabbitMeasurement, UUID> {
    LinkedList<RabbitMeasurement> findAllByDeviceUuidAndDateTimeBetweenOrderByDateTime(UUID deviceUuid, LocalDateTime startTime, LocalDateTime endTime);
    boolean existsByDeviceUuid(UUID deviceUuid);
}
