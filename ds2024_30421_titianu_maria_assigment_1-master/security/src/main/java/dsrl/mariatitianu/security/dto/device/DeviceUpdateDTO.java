package dsrl.mariatitianu.security.dto.device;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceUpdateDTO {
    private UUID userUuid;
    private String name;
    private String description;
    private String address;
    private double maxHourConsumption;
}
