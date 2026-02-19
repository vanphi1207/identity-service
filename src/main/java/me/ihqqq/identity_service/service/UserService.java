package me.ihqqq.identity_service.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.identity_service.dto.request.UserCreationRequest;
import me.ihqqq.identity_service.dto.request.UserUpdateRequest;
import me.ihqqq.identity_service.dto.response.UserResponse;
import me.ihqqq.identity_service.entity.User;
import me.ihqqq.identity_service.enums.Role;
import me.ihqqq.identity_service.exception.AppException;
import me.ihqqq.identity_service.exception.ErrorCode;
import me.ihqqq.identity_service.mapper.UserMapper;
import me.ihqqq.identity_service.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {

    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    public UserResponse createUser(UserCreationRequest request) {

        if(userRepository.existsByUsername(request.getUsername()))
            throw new AppException(ErrorCode.USER_EXISTED);

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        HashSet<String> roles = new HashSet<>();
        roles.add(Role.USER.name());
        user.setRoles(roles);

        return userMapper.toUserResponse(userRepository.save(user));

    }

    public UserResponse updateUser(String userId ,UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        userMapper.updateUser(user, request);

        return userMapper.toUserResponse(userRepository.save(user));
    }

    public List<UserResponse> getUsers() {

        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }

    public UserResponse getUserById(String id) {
        return userMapper.toUserResponse(userRepository.findById(id).orElseThrow(()
                -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }

    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }
}
