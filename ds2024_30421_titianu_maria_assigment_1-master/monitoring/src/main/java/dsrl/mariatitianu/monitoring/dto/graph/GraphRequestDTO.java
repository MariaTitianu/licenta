package dsrl.mariatitianu.monitoring.dto.graph;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphRequestDTO {
    private UUID userUuid;
    private UUID deviceUuid;
    private String date;
}
