package dsrl.mariatitianu.security.service.impl;

import dsrl.mariatitianu.security.dto.user.*;
import dsrl.mariatitianu.security.dto.user.auth.UserAuthDTO;
import dsrl.mariatitianu.security.dto.user.extra.UserExtraDTO;
import dsrl.mariatitianu.security.entity.User;
import dsrl.mariatitianu.security.enums.UserRole;
import dsrl.mariatitianu.security.mapper.UserAuthMapper;
import dsrl.mariatitianu.security.mapper.UserMapper;
import dsrl.mariatitianu.security.repository.UserRepository;
import dsrl.mariatitianu.security.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@SuppressWarnings("SpringQualifierCopyableLombok")
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserAuthMapper userAuthMapper;

    @Qualifier("userRestClient")
    private final RestClient restClient;

    @Override
    public ResponseEntity<List<UserDTO>> findAll(){
        Map<String, UserAuthDTO> userAuthDTOMap = userAuthMapper.toUserDTOMap(userRepository.findAll());
        ResponseEntity<List<UserExtraDTO>> userExtraDTOListResponseEntity = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/users")
                        .build())
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {});
        return ResponseEntity
                .status(userExtraDTOListResponseEntity.getStatusCode())
                .body(userMapper.toCompleteUserDTOList(userAuthDTOMap, Objects.requireNonNull(userExtraDTOListResponseEntity.getBody())));
    }

    @Override
    public ResponseEntity<List<UserDTO>> findUsersWithRole(UserRole userRole) {
        ResponseEntity<List<UserDTO>> userDTOListResponseEntity = findAll();
        return ResponseEntity
                .status(userDTOListResponseEntity.getStatusCode())
                .body(userDTOListResponseEntity.getBody().stream()
                        .filter(userDTO -> userDTO.getRole().equals(userRole))
                        .toList()
                );
    }

    @Override
    public ResponseEntity<UserDTO> findUser(String username) {
        Optional<User> userOptional = userRepository.findById(username);

        if(userOptional.isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        UserAuthDTO userAuthDTO = userAuthMapper.toUserAuthDTO(userOptional.get());
        ResponseEntity<UserExtraDTO> userExtraDTOResponseEntity = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/users/" + username)
                        .build())
                .retrieve()
                .toEntity(UserExtraDTO.class);
        return ResponseEntity
                .status(userExtraDTOResponseEntity.getStatusCode())
                .body(userMapper.toCompleteUserDTO(userAuthDTO, Objects.requireNonNull(userExtraDTOResponseEntity.getBody())));
    }

    @Override
    public List<UserRole> findUserRoles() {
        return List.of(UserRole.values());
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public ResponseEntity<UserDTO> createUser(UserCreateDTO dto) {
        if (userRepository.existsById(dto.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        UserAuthDTO userAuthDTO = userAuthMapper.toUserAuthDTO(userRepository.save(userMapper.toUser(dto)));

        ResponseEntity<UserExtraDTO> userExtraDTOResponseEntity = restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/users")
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .body(dto)
                .retrieve()
                .toEntity(UserExtraDTO.class);

        return ResponseEntity
                .status(userExtraDTOResponseEntity.getStatusCode())
                .body(userMapper.toCompleteUserDTO(userAuthDTO, Objects.requireNonNull(userExtraDTOResponseEntity.getBody())));
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public ResponseEntity<UserDTO> updateUser(String username, UserUpdateDTO dto) {
        Optional<User> userOptional = userRepository.findById(username);

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        User updatedUser = userMapper.updateUserFromDTO(userOptional.get(), dto);
        userRepository.save(updatedUser);

        ResponseEntity<UserExtraDTO> userExtraDTOResponseEntity = restClient.patch()
                .uri(uriBuilder -> uriBuilder
                        .path("/users/" + username)
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .body(userMapper.toUserExtraUpdateDTO(dto))
                .retrieve()
                .toEntity(UserExtraDTO.class);

        return ResponseEntity
                .status(userExtraDTOResponseEntity.getStatusCode())
                .body(userMapper.toCompleteUserDTO(userAuthMapper.toUserAuthDTO(updatedUser), Objects.requireNonNull(userExtraDTOResponseEntity.getBody())));
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public ResponseEntity<UserDTO> deleteUser(String username){
        Optional<User> userOptional = userRepository.findById(username);

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        ResponseEntity<UserExtraDTO> userExtraDTOResponseEntity = restClient.delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/users/" + username)
                        .build())
                .retrieve()
                .toEntity(UserExtraDTO.class);

        User user = userOptional.get();
        userRepository.delete(user);

        return ResponseEntity
                .status(userExtraDTOResponseEntity.getStatusCode())
                .body(userMapper.toCompleteUserDTO(userAuthMapper.toUserAuthDTO(user), Objects.requireNonNull(userExtraDTOResponseEntity.getBody())));

    }
}