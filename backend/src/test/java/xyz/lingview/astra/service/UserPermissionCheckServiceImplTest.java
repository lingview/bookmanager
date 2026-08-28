package xyz.lingview.astra.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.lingview.astra.mapper.UserPermissionMapper;
import xyz.lingview.astra.service.impl.UserPermissionCheckServiceImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserPermissionCheckServiceImplTest {

    @Mock
    private UserPermissionMapper userPermissionMapper;

    @InjectMocks
    private UserPermissionCheckServiceImpl permissionCheckService;

    @Test
    void hasAllPermissions_shouldReturnTrueWhenHasAll() {
        when(userPermissionMapper.findPermissionCodesByUserName("admin"))
                .thenReturn(List.of("system:user:management", "system:config:management", "system:role:management"));
        assertTrue(permissionCheckService.hasAllPermissions("admin", new String[]{"system:user:management", "system:config:management"}));
    }

    @Test
    void hasAllPermissions_shouldReturnFalseWhenMissingOne() {
        when(userPermissionMapper.findPermissionCodesByUserName("operator"))
                .thenReturn(List.of("system:config:management"));
        assertFalse(permissionCheckService.hasAllPermissions("operator", new String[]{"system:user:management", "system:config:management"}));
    }

    @Test
    void hasAnyPermission_shouldReturnTrueWhenHasOne() {
        when(userPermissionMapper.findPermissionCodesByUserName("user"))
                .thenReturn(List.of("attachment:add"));
        assertTrue(permissionCheckService.hasAnyPermission("user", new String[]{"system:user:management", "attachment:add"}));
    }

    @Test
    void hasAnyPermission_shouldReturnFalseWhenHasNone() {
        when(userPermissionMapper.findPermissionCodesByUserName("guest"))
                .thenReturn(List.of());
        assertFalse(permissionCheckService.hasAnyPermission("guest", new String[]{"system:user:management", "system:config:management"}));
    }
}