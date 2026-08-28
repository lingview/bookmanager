package xyz.lingview.astra.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import xyz.lingview.astra.common.ApiResponse;
import xyz.lingview.astra.domain.SystematicNotification;
import xyz.lingview.astra.dto.request.NotificationActionDTO;
import xyz.lingview.astra.dto.request.PageResult;
import xyz.lingview.astra.service.CurrentUserService;
import xyz.lingview.astra.service.NotificationService;

/**
 * @Author: lingview
 * @Date: 2026/01/31 12:52:01
 * @Description: 系统通知控制器
 * @Version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class SystematicNotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CurrentUserService currentUserService;

    // 获取系统通知列表
    @GetMapping("/getsystematicNotification")
    public ApiResponse<PageResult<SystematicNotification>> getSystematicNotification(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            String username = currentUserService.getCurrentUsername();
            PageResult<SystematicNotification> notifications =
                notificationService.getSystematicNotificationsByUsername(username, pageNum, pageSize);
            return ApiResponse.success(notifications);
        } catch (Exception e) {
            log.error("获取系统通知失败", e);
            return ApiResponse.error(500, "获取系统通知失败");
        }
    }

    // 获取已读通知列表
    @GetMapping("/getReadNotifications")
    public ApiResponse<PageResult<SystematicNotification>> getReadNotifications(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            String username = currentUserService.getCurrentUsername();
            PageResult<SystematicNotification> notifications =
                notificationService.getReadNotificationsByUsername(username, pageNum, pageSize);
            return ApiResponse.success(notifications);
        } catch (Exception e) {
            log.error("获取已读通知失败", e);
            return ApiResponse.error(500, "获取已读通知失败");
        }
    }

    // 获取未读通知列表
    @GetMapping("/getUnreadNotifications")
    public ApiResponse<PageResult<SystematicNotification>> getUnreadNotifications(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            String username = currentUserService.getCurrentUsername();
            PageResult<SystematicNotification> notifications =
                notificationService.getUnreadNotificationsByUsername(username, pageNum, pageSize);
            return ApiResponse.success(notifications);
        } catch (Exception e) {
            log.error("获取未读通知失败", e);
            return ApiResponse.error(500, "获取未读通知失败");
        }
    }

    // 标记通知为已读
    @PostMapping("/readSystematicNotification")
    public ApiResponse<String> readSystematicNotification(
            @RequestBody @Valid NotificationActionDTO dto) {
        try {
            String username = currentUserService.getCurrentUsername();
            boolean success = notificationService.updateNotificationStatusByUsername(dto.getId(), username);
            return success ? ApiResponse.success("操作成功") : ApiResponse.error(400, "操作失败或无权限");
        } catch (Exception e) {
            log.error("设置通知为已读失败", e);
            return ApiResponse.error(500, "设置通知为已读失败");
        }
    }

    // 删除通知
    @PostMapping("/deleteSystematicNotification")
    public ApiResponse<String> deleteSystematicNotification(
            @RequestBody @Valid NotificationActionDTO dto) {
        try {
            String username = currentUserService.getCurrentUsername();
            boolean success = notificationService.deleteSystematicNotificationByUsername(dto.getId(), username);
            return success ? ApiResponse.success("删除成功") : ApiResponse.error(400, "删除失败或无权限");
        } catch (Exception e) {
            log.error("删除通知失败", e);
            return ApiResponse.error(500, "删除通知失败");
        }
    }
}