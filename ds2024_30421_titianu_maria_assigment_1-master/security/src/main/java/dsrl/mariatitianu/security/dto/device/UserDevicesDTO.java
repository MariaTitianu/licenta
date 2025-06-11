package dsrl.mariatitianu.security.dto.device;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDevicesDTO {
    private UUID uuid;
    private List<DeviceDTO> devices;
}
