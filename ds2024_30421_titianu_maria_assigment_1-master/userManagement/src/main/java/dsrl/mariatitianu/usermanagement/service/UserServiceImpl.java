package dsrl.mariatitianu.usermanagement.service;

import dsrl.mariatitianu.usermanagement.dto.UserCreateDTO;
import dsrl.mariatitianu.usermanagement.dto.UserExtraDTO;
import dsrl.mariatitianu.usermanagement.dto.UserUpdateDTO;
import dsrl.mariatitianu.usermanagement.entity.User;
import dsrl.mariatitianu.usermanagement.mapper.UserMapper;
import dsrl.mariatitianu.usermanagement.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RestClient restClient;

    @Override
    public List<UserExtraDTO> findAll() {
        return userMapper.toUserExtraDTOs(userRepository.findAll());
    }

    @Override
    public Optional<UserExtraDTO> find(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        return userOptional.map(userMapper::toUserExtraDTO);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public Optional<UserExtraDTO> create(UserCreateDTO dto) {
        if (userRepository.findByUsername(dto.getName()).isPresent()) {
            return Optional.empty();
        }
        User user = userRepository.save(userMapper.toUser(dto));
        restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/users")
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .body(userMapper.toUserCreateUuidDTO(user))
                .retrieve();
        return Optional.of(userMapper.toUserExtraDTO(user));
    }


    @Override
    public Optional<UserExtraDTO> update(String username, UserUpdateDTO dto) {
        Optional<User> oldUser = userRepository.findByUsername(username);
        if (oldUser.isEmpty()) {
            return Optional.empty();
        }
        User newUser = userRepository.save(userMapper.updateUser(oldUser.get(), dto));
        return Optional.of(userMapper.toUserExtraDTO(newUser));
    }

    @Transactional(rollbackOn = Exception.class)
    @Override
    public Optional<UserExtraDTO> delete(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            return Optional.empty();
        }
        restClient.delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/users/" + user.get().getId())
                        .build())
                .retrieve();
        userRepository.delete(user.get());
        return Optional.of(userMapper.toUserExtraDTO(user.get()));
    }
}
