package dsrl.mariatitianu.security.service.impl;

import dsrl.mariatitianu.security.dto.device.DeviceCreateDTO;
import dsrl.mariatitianu.security.dto.device.DeviceDTO;
import dsrl.mariatitianu.security.dto.device.DeviceUpdateDTO;
import dsrl.mariatitianu.security.dto.device.UserDevicesDTO;
import dsrl.mariatitianu.security.service.DeviceService;
import dsrl.mariatitianu.security.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

@SuppressWarnings("SpringQualifierCopyableLombok")
@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {
    @Qualifier("deviceRestClient")
    private final RestClient restClient;
    private final UserService userService;


    @Override
    public ResponseEntity<List<DeviceDTO>> findAll() {
        ResponseEntity<List<DeviceDTO>> responseEntity = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/devices")
                        .build())
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {});
        return ResponseEntity
                .status(responseEntity.getStatusCode())
                .body(responseEntity.getBody());
    }

    @Override
    public ResponseEntity<DeviceDTO> find(UUID uuid){
        ResponseEntity<DeviceDTO> responseEntity = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/devices/" + uuid.toString())
                        .build())
                .retrieve()
                .toEntity(DeviceDTO.class);
        return ResponseEntity
                .status(responseEntity.getStatusCode())
                .body(responseEntity.getBody());
    }

    @Override
    public ResponseEntity<DeviceDTO> create(DeviceCreateDTO dto){
        ResponseEntity<DeviceDTO> responseEntity = restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/devices")
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .body(dto)
                .retrieve()
                .toEntity(DeviceDTO.class);
        return ResponseEntity
                .status(responseEntity.getStatusCode())
                .body(responseEntity.getBody());
    }

    @Override
    public ResponseEntity<DeviceDTO> update(UUID uuid, DeviceUpdateDTO dto){
        ResponseEntity<DeviceDTO> responseEntity = restClient.patch()
                .uri(uriBuilder -> uriBuilder
                        .path("/devices/" + uuid.toString())
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .body(dto)
                .retrieve()
                .toEntity(DeviceDTO.class);
        return ResponseEntity
                .status(responseEntity.getStatusCode())
                .body(responseEntity.getBody());
    }

    @Override
    public ResponseEntity<DeviceDTO> delete(UUID uuid){
        ResponseEntity<DeviceDTO> responseEntity = restClient.delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/devices/" + uuid.toString())
                        .build())
                .retrieve()
                .toEntity(DeviceDTO.class);
        return ResponseEntity
                .status(responseEntity.getStatusCode())
                .body(responseEntity.getBody());
    }

    @Override
    public ResponseEntity<UserDevicesDTO> findAllByUsername(UUID uuid) {
        ResponseEntity<UserDevicesDTO> responseEntity = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/users/" + uuid.toString())
                        .build())
                .retrieve()
                .toEntity(UserDevicesDTO.class);
        return ResponseEntity
                .status(responseEntity.getStatusCode())
                .body(responseEntity.getBody());
    }
}
