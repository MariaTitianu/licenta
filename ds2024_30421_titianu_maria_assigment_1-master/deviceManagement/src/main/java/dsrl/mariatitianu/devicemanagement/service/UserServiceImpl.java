package dsrl.mariatitianu.devicemanagement.service;

import dsrl.mariatitianu.devicemanagement.dto.user.UserDTO;
import dsrl.mariatitianu.devicemanagement.entity.User;
import dsrl.mariatitianu.devicemanagement.mapper.UserMapper;
import dsrl.mariatitianu.devicemanagement.repository.DeviceRepository;
import dsrl.mariatitianu.devicemanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    @Override
    public Optional<UserDTO> addUser(UserDTO dto) {
        if (userRepository.findById(dto.getUuid()).isPresent()) {
            return Optional.empty();
        }
        User user = userRepository.save(userMapper.toUser(dto));
        return Optional.of(userMapper.toUserDTO(user));
    }

    @Override
    public Optional<UserDTO> find(UUID uuid) {
        Optional<User> userOptional = userRepository.findById(uuid);
        return userOptional.map(userMapper::toUserDTO);
    }
}
