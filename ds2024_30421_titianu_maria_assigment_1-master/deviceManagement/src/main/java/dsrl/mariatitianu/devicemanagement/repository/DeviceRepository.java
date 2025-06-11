package dsrl.mariatitianu.devicemanagement.repository;

import dsrl.mariatitianu.devicemanagement.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID> {
    List<Device> findAllByUserId(UUID userId);
}
