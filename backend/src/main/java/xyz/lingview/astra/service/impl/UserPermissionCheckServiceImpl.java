package xyz.lingview.astra.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.lingview.astra.mapper.UserPermissionMapper;
import xyz.lingview.astra.service.UserPermissionCheckService;

import java.util.Set;

@Service
public class UserPermissionCheckServiceImpl implements UserPermissionCheckService {

    @Autowired
    private UserPermissionMapper userPermissionMapper;

    @Override
    public boolean hasAllPermissions(String username, String[] permissions) {
        Set<String> userPermissions = getUserPermissions(username);
        for (String permission : permissions) {
            if (!userPermissions.contains(permission)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean hasAnyPermission(String username, String[] permissions) {
        Set<String> userPermissions = getUserPermissions(username);
        for (String permission : permissions) {
            if (userPermissions.contains(permission)) {
                return true;
            }
        }
        return false;
    }

    private Set<String> getUserPermissions(String username) {
        return Set.copyOf(userPermissionMapper.findPermissionCodesByUserName(username));
    }
}