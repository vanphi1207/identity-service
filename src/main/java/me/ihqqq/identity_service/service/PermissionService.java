package me.ihqqq.identity_service.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.identity_service.dto.request.PermissionRequest;
import me.ihqqq.identity_service.dto.response.PermissionResponse;
import me.ihqqq.identity_service.entity.Permission;
import me.ihqqq.identity_service.mapper.PermissionMapper;
import me.ihqqq.identity_service.repository.PermissionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionService {

    PermissionRepository permissionRepository;
    PermissionMapper permissionMapper;

    public PermissionResponse create(PermissionRequest request) {
        Permission permission = permissionMapper.toPermission(request);
        permissionRepository.save(permission);

        return permissionMapper.toPermissionResponse(permission);
    }

    public List<PermissionResponse> getAll() {
        List<Permission> permissions = permissionRepository.findAll();
        return permissions.stream().map(permissionMapper::toPermissionResponse).toList();

    }

    public void delete(String permission) {
        permissionRepository.deleteById(permission);
    }

}

