package dsrl.mariatitianu.monitoring.dto.device;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitoringDeviceDTO {
    private UUID deviceUuid;
    private UUID userUuid;
    private double maxHourConsumption;
}
