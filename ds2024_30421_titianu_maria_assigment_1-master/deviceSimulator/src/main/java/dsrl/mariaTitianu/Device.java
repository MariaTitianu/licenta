package dsrl.mariaTitianu;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor

public class Device {
    private long timestamp;
    private String deviceId;
    private double value;
}