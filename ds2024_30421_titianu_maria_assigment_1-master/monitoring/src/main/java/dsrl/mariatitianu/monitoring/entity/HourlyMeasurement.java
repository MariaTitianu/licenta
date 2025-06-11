package dsrl.mariatitianu.monitoring.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "hourly_measurement")
public class HourlyMeasurement {
    @Id
    @GeneratedValue
    private UUID uuid;
    private UUID deviceUuid;
    private LocalDateTime dateTime;
    private Double measurement;
}
