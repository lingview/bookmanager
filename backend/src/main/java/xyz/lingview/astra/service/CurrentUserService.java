package xyz.lingview.astra.service;

import xyz.lingview.astra.security.AuthType;
import xyz.lingview.astra.security.AuthenticatedUser;


public interface CurrentUserService {

    // 检查请求是否认证
    boolean isAuthenticated();

    // 获取当前请求用户名
    String getCurrentUsername();

    // 获取当前请求用户uuid
    String getCurrentUserUuid();

    // 获取当前认证主体，未认证返回 null
    AuthenticatedUser getCurrentUser();

    // 获取当前认证方式，未认证返回 null
    AuthType getCurrentAuthType();
}