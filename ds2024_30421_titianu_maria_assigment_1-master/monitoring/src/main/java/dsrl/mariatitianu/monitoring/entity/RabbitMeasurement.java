package dsrl.mariatitianu.monitoring.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "rabbit_measurement")
public class RabbitMeasurement {
    @Id
    @GeneratedValue
    private UUID uuid;
    private UUID deviceUuid;
    private LocalDateTime dateTime;
    private Double measurement;
}
