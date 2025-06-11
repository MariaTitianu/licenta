package dsrl.mariatitianu.devicemanagement.repository;

import dsrl.mariatitianu.devicemanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>{
}
