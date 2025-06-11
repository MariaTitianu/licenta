package dsrl.mariatitianu.monitoring.dto.device;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HourlyMeasurementDTO implements Comparable<HourlyMeasurementDTO> {
    private UUID deviceUuid;
    private LocalDateTime dateTime;
    private Double measurement;

    @Override
    public int compareTo(HourlyMeasurementDTO o) {
        return this.getDateTime().compareTo(o.getDateTime());
    }
}
