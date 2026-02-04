package me.ihqqq.identity_service.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.identity_service.dto.request.ApiResponse;
import me.ihqqq.identity_service.dto.request.UserCreationRequest;
import me.ihqqq.identity_service.dto.request.UserUpdateRequest;
import me.ihqqq.identity_service.dto.response.UserResponse;
import me.ihqqq.identity_service.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserService userService;

    @PostMapping
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request) {
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.createUser(request));

        return apiResponse;
    }

    @GetMapping
    List<UserResponse> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/{userId}")
    UserResponse getUser(@PathVariable("userId") String userId) {
        return userService.getUserById(userId);

    }

    @PutMapping("/{userId}")
    UserResponse updateUser(@PathVariable String userId, @RequestBody UserUpdateRequest request) {
        return userService.updateUser(userId, request);
    }

    @DeleteMapping("/{userId}")
    String deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return "User deleted successfully";
    }




}
