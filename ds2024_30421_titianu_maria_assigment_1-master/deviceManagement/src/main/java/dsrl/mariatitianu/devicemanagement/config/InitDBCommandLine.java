package dsrl.mariatitianu.devicemanagement.config;


import dsrl.mariatitianu.devicemanagement.entity.Device;
import dsrl.mariatitianu.devicemanagement.entity.User;
import dsrl.mariatitianu.devicemanagement.repository.DeviceRepository;
import dsrl.mariatitianu.devicemanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.*;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.random.RandomGenerator;


@RequiredArgsConstructor
@Configuration
public class InitDBCommandLine implements CommandLineRunner {
    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    @Value("${database.ddl_auto}")
    private String ddlMode;
    @Value("${database.ip}")
    private String dbIp;

    @Override
    public void run(String... args) {
        if (!ddlMode.equals("none")) {
            initializeData();
            if(!dbIp.equals("localhost")) {
                System.exit(0);
            }
        }
    }

    public void initializeData() {
        User user;
        Device device;
        if (userRepository.findById(UUID.fromString("018ec439-7d15-7625-9457-21d672a63725")).isEmpty()) {
            user = new User(UUID.fromString("018ec439-7d15-7625-9457-21d672a63725"), new ArrayList<>());
            userRepository.save(user);
            if (deviceRepository.findById(UUID.fromString("733cf39c-cc83-4183-8d0d-1a69929cb344")).isEmpty()){
                device = new Device(UUID.fromString("733cf39c-cc83-4183-8d0d-1a69929cb344"),user,"Device 1", "A device that does stuff", "Str. Somewhere, Nr. 69", 10);
                deviceRepository.save(device);
            }
        }
        if (userRepository.findById(UUID.fromString("018ec446-12c2-74d3-8fd4-324ae0c3f3be")).isEmpty())  {
            user = new User(UUID.fromString("018ec446-12c2-74d3-8fd4-324ae0c3f3be"), new ArrayList<>());
            userRepository.save(user);
            if (deviceRepository.findById(UUID.fromString("f7a7bb46-611f-4fdc-9688-7695ffad65c3")).isEmpty()){
                device = new Device(UUID.fromString("f7a7bb46-611f-4fdc-9688-7695ffad65c3"),user,"Device 2", "Another device that does stuff", "Str. Somewhere, Nr. 169", 121);
                deviceRepository.save(device);
            }
        }
        if (userRepository.findById(UUID.fromString("018ec446-3399-746e-82a5-fc4a65bbbf92")).isEmpty())  {
            user = new User(UUID.fromString("018ec446-3399-746e-82a5-fc4a65bbbf92"), new ArrayList<>());
            userRepository.save(user);
            if (deviceRepository.findById(UUID.fromString("3d7e4ae2-caee-4143-a881-e9dbc0cb7ff7")).isEmpty()){
                device = new Device(UUID.fromString("3d7e4ae2-caee-4143-a881-e9dbc0cb7ff7"),user,"Device 3", "A third device that does stuff", "Str. Somewhere, Nr. 269", 145);
                deviceRepository.save(device);
            }
            if (deviceRepository.findById(UUID.fromString("2a60da93-902a-4f44-842d-496e2f11704d")).isEmpty()){
                device = new Device(UUID.fromString("2a60da93-902a-4f44-842d-496e2f11704d"),user,"Device 4", "Yet another device that does stuff", "Str. Somewhere, Nr. 369", 10);
                deviceRepository.save(device);
            }
        }
        if (userRepository.findById(UUID.fromString("018ec446-49e6-776d-bc1b-82838cc6516f")).isEmpty()) {
            user = new User(UUID.fromString("018ec446-49e6-776d-bc1b-82838cc6516f"), new ArrayList<>());
            userRepository.save(user);
        }
    }
}
