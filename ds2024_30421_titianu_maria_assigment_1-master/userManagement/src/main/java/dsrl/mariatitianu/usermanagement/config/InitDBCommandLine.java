package dsrl.mariatitianu.usermanagement.config;

import dsrl.mariatitianu.usermanagement.entity.User;
import dsrl.mariatitianu.usermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@RequiredArgsConstructor
@Configuration
public class InitDBCommandLine implements CommandLineRunner {
    private final UserRepository userRepository;
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
        if (userRepository.findByUsername("admin").isEmpty()) {
            user = new User(UUID.fromString("018ec439-7d15-7625-9457-21d672a63725"), "admin", "Admin");
            userRepository.save(user);
        }
        if (userRepository.findByUsername("captain_oreo").isEmpty()) {
            user = new User(UUID.fromString("018ec446-12c2-74d3-8fd4-324ae0c3f3be"), "captain_oreo", "Maria");
            userRepository.save(user);
        }
        if (userRepository.findByUsername("carol8").isEmpty()) {
            user = new User(UUID.fromString("018ec446-3399-746e-82a5-fc4a65bbbf92"), "carol8", "Cristi");
            userRepository.save(user);
        }
        if (userRepository.findByUsername("laur").isEmpty()) {
            user = new User(UUID.fromString("018ec446-49e6-776d-bc1b-82838cc6516f"), "laur", "Laur");
            userRepository.save(user);
        }
    }
}
