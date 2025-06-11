package dsrl.mariatitianu.monitoring.config;


import dsrl.mariatitianu.monitoring.entity.Device;

import dsrl.mariatitianu.monitoring.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;


import java.util.UUID;


@RequiredArgsConstructor
@Configuration
public class InitDBConfig implements CommandLineRunner {
    private final DeviceRepository deviceRepository;
    @Value("${database.ddl_auto}")
    private String ddlMode;
    @Value("${database.ip}")
    private String dbIp;

    @Override
    public void run(String... args) {
        if (!ddlMode.equals("none")) {
            initializeData();
        }
    }

    public void initializeData() {
        Device device;
        if (deviceRepository.findById(UUID.fromString("733cf39c-cc83-4183-8d0d-1a69929cb344")).isEmpty()){
                device = new Device(UUID.fromString("733cf39c-cc83-4183-8d0d-1a69929cb344"),UUID.fromString("018ec439-7d15-7625-9457-21d672a63725"), 10);
                deviceRepository.save(device);
        }
        if (deviceRepository.findById(UUID.fromString("f7a7bb46-611f-4fdc-9688-7695ffad65c3")).isEmpty()){
                device = new Device(UUID.fromString("f7a7bb46-611f-4fdc-9688-7695ffad65c3"),UUID.fromString("018ec446-12c2-74d3-8fd4-324ae0c3f3be"), 121);
                deviceRepository.save(device);
        }
        if (deviceRepository.findById(UUID.fromString("3d7e4ae2-caee-4143-a881-e9dbc0cb7ff7")).isEmpty()){
                device = new Device(UUID.fromString("3d7e4ae2-caee-4143-a881-e9dbc0cb7ff7"),UUID.fromString("018ec446-3399-746e-82a5-fc4a65bbbf92"), 145);
                deviceRepository.save(device);
        }
        if (deviceRepository.findById(UUID.fromString("2a60da93-902a-4f44-842d-496e2f11704d")).isEmpty()){
                device = new Device(UUID.fromString("2a60da93-902a-4f44-842d-496e2f11704d"),UUID.fromString("018ec446-3399-746e-82a5-fc4a65bbbf92"), 10);
                deviceRepository.save(device);

        }
    }
}
