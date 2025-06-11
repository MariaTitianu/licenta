package dsrl.mariatitianu.security.repository;

import dsrl.mariatitianu.security.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String admin);
}
