package dsrl.mariatitianu.security.mapper;

import dsrl.mariatitianu.security.dto.user.*;
import dsrl.mariatitianu.security.dto.user.auth.UserAuthDTO;
import dsrl.mariatitianu.security.dto.user.extra.UserExtraDTO;
import dsrl.mariatitianu.security.dto.user.extra.UserExtraUpdateDTO;
import dsrl.mariatitianu.security.entity.User;
import dsrl.mariatitianu.security.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserMapper {
    private final PasswordEncoder passwordEncoder;

    public User toUser(UserCreateDTO userCreateDTO){
        return User.builder()
                .username(userCreateDTO.getUsername())
                .password(passwordEncoder.encode(userCreateDTO.getPassword()))
                .role(userCreateDTO.getRole())
                .build();
    }

    public UserDTO toUserDTO(User user){
        return UserDTO.builder()
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

    public List<UserDTO> toUserDTOList(List<User> userList){
        return userList.stream()
                .map(this::toUserDTO)
                .toList();
    }

    public UserRoleDTO toUserRoleDTO(UserRole userRole){
        return UserRoleDTO.builder()
                .role(userRole)
                .build();
    }

    public List<UserRoleDTO> toUserRoleDTOList(List<UserRole> userRoleList){
        return userRoleList.stream()
                .map(this::toUserRoleDTO)
                .toList();
    }

    public Map<String,UserDTO> toUserDTOMap(List<User> userList) {
        return this.toUserDTOList(userList).stream()
                .collect(Collectors.toMap(UserDTO::getUsername, Function.identity()));
    }

    public UserDTO toCompleteUserDTO(UserAuthDTO userAuthDTO, UserExtraDTO userExtraDTO){
        return UserDTO.builder()
                .uuid(userExtraDTO.getUuid())
                .username(userAuthDTO.getUsername())
                .name(userExtraDTO.getName())
                .role(userAuthDTO.getUserRole())
                .build();
    }

    public List<UserDTO> toCompleteUserDTOList(Map<String, UserAuthDTO> authDTOMap, List<UserExtraDTO> extraDTOList){
        return extraDTOList.stream()
                .map(userExtraDTO ->{
                    UserAuthDTO userAuthDTO = authDTOMap.get(userExtraDTO.getUsername());
                    return toCompleteUserDTO(userAuthDTO, userExtraDTO);
                })
                .toList();
    }

    public User updateUserFromDTO(User user, UserUpdateDTO dto){
        if(!dto.getPassword().isEmpty()){
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        if(dto.getRole() != null){
            user.setRole(dto.getRole());
        }
        return user;
    }

    public UserExtraUpdateDTO toUserExtraUpdateDTO(UserUpdateDTO dto) {
        return UserExtraUpdateDTO.builder()
                .name(dto.getName())
                .build();
    }
}
