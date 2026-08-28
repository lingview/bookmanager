package xyz.lingview.astra.service;

public interface UserPermissionCheckService {
    boolean hasAllPermissions(String username, String[] permissions);
    boolean hasAnyPermission(String username, String[] permissions);
}