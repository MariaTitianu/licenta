package dsrl.mariatitianu.security.config;

import dsrl.mariatitianu.security.entity.User;
import dsrl.mariatitianu.security.enums.UserRole;
import dsrl.mariatitianu.security.repository.UserRepository;
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
            user = new User("admin", "$2a$12$p.tKXqd03n5XG8bRlCAAq.SRUUg2et4yp.8wNiulr11ZrD8Y7YKDi", UserRole.ADMIN);
            userRepository.save(user);
        }
        if (userRepository.findByUsername("captain_oreo").isEmpty()) {
            user = new User("captain_oreo", "$2a$12$4DH.uZH/PdQlgagkHQ7nq.6BQtl6JGnDLvPIskNlOeYUXqUUUHZia", UserRole.CLIENT);
            userRepository.save(user);
        }
        if (userRepository.findByUsername("carol8").isEmpty()) {
            user = new User("carol8", "$2a$12$aGLXUBEkJ/KWndRvFXXEHubOoeHKi3XeA7yc94HUv9EZhJxJ6dYaa", UserRole.CLIENT);
            userRepository.save(user);
        }
        if (userRepository.findByUsername("laur").isEmpty()) {
            user = new User("laur", "$2a$12$TBP/X4k6Nc206H25.A8EGuC/.lXCQQVehA2/PT1zxojTErpQhcoIG", UserRole.CLIENT);
            userRepository.save(user);
        }
    }
}
