package dsrl.mariatitianu.devicemanagement.dto.user;

import dsrl.mariatitianu.devicemanagement.dto.device.DeviceDTO;
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
public class UserDTO {
    private UUID uuid;
    private List<DeviceDTO> devices;
}
