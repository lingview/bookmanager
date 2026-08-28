package xyz.lingview.astra.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import xyz.lingview.astra.annotation.RequiresPermission;
import xyz.lingview.astra.common.ApiResponse;
import xyz.lingview.astra.service.AttachmentManagementService;
import xyz.lingview.astra.service.CurrentUserService;

import java.util.Map;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentManagementController {

    @Autowired
    private AttachmentManagementService attachmentManagementService;

    @Autowired
    private CurrentUserService currentUserService;

    // 管理员分页查询所有正常附件
    @GetMapping("/admin/page")
    @RequiresPermission({"system:attachment:view", "system:attachment:management"})
    public ApiResponse<Map<String, Object>> getAttachments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(attachmentManagementService.getPage(page, size));
    }

    // 管理员分页查询已删除附件
    @GetMapping("/admin/page-deleted-only")
    @RequiresPermission({"system:attachment:view", "system:attachment:management"})
    public ApiResponse<Map<String, Object>> getDeletedAttachmentsOnly(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(attachmentManagementService.getDeletedPageOnly(page, size));
    }

    // 管理员分页查询附件（含已删除）
    @GetMapping("/admin/page-with-deleted")
    @RequiresPermission({"system:attachment:view", "system:attachment:management"})
    public ApiResponse<Map<String, Object>> getAttachmentsWithDeleted(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(attachmentManagementService.getPageWithRecentDeleted(page, size));
    }

    // 管理员按用户分页查询附件
    @GetMapping("/admin/page-by-user")
    @RequiresPermission({"system:attachment:view", "system:attachment:management"})
    public ApiResponse<Map<String, Object>> getAttachmentsByUser(
            @RequestParam String userUuid,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(attachmentManagementService.getPageByUserUuid(userUuid, page, size));
    }

    // 管理员按用户分页查询附件（含已删除）
    @GetMapping("/admin/page-by-user-with-deleted")
    @RequiresPermission({"system:attachment:view", "system:attachment:management"})
    public ApiResponse<Map<String, Object>> getAttachmentsByUserWithDeleted(
            @RequestParam String userUuid,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(attachmentManagementService.getPageByUserUuidWithDeleted(userUuid, page, size));
    }

    // 管理员按用户分页查询已删除附件
    @GetMapping("/admin/page-deleted-only-by-user")
    @RequiresPermission({"system:attachment:view", "system:attachment:management"})
    public ApiResponse<Map<String, Object>> getDeletedAttachmentsByUserOnly(
            @RequestParam String userUuid,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(attachmentManagementService.getDeletedPageByUserUuidOnly(userUuid, page, size));
    }

    // 用户分页查询自己的附件
    @GetMapping("/page")
    public ApiResponse<Map<String, Object>> getAttachmentsByUuid(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        String uuid = currentUserService.getCurrentUserUuid();
        return ApiResponse.success(attachmentManagementService.getPageByUuid(uuid, page, size));
    }

    // 用户分页查询自己已删除的附件
    @GetMapping("/page-deleted-only")
    @RequiresPermission({"attachment:view", "attachment:edit"})
    public ApiResponse<Map<String, Object>> getDeletedAttachmentsByUuidOnly(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        String uuid = currentUserService.getCurrentUserUuid();
        return ApiResponse.success(attachmentManagementService.getDeletedPageByUuidOnly(uuid, page, size));
    }

    // 用户分页查询自己的附件（含已删除）
    @GetMapping("/page-with-deleted")
    @RequiresPermission({"attachment:view", "attachment:edit"})
    public ApiResponse<Map<String, Object>> getAttachmentsByUuidWithDeleted(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        String uuid = currentUserService.getCurrentUserUuid();
        return ApiResponse.success(attachmentManagementService.getPageByUuidWithRecentDeleted(uuid, page, size));
    }

    // 管理员删除附件
    @DeleteMapping("/admin/{attachmentId}")
    @RequiresPermission({"system:attachment:delete", "system:attachment:management"})
    public ApiResponse<String> deleteAttachmentAdmin(@PathVariable String attachmentId) {
        boolean success = attachmentManagementService.deleteAttachment(attachmentId);
        return success ? ApiResponse.success("删除成功") : ApiResponse.error(400, "删除失败");
    }

    // 管理员撤销删除附件
    @PostMapping("/admin/{attachmentId}/restore")
    @RequiresPermission({"system:attachment:undelete", "system:attachment:management"})
    public ApiResponse<String> restoreAttachmentAdmin(@PathVariable String attachmentId) {
        boolean success = attachmentManagementService.restoreAttachment(attachmentId);
        return success ? ApiResponse.success("撤销删除成功") : ApiResponse.error(400, "撤销删除失败");
    }

    // 用户删除自己的附件
    @DeleteMapping("/{attachmentId}")
    @RequiresPermission({"attachment:delete", "attachment:edit"})
    public ApiResponse<String> deleteAttachment(@PathVariable String attachmentId) {
        String username = currentUserService.getCurrentUsername();
        boolean success = attachmentManagementService.deleteAttachmentByUser(attachmentId, username);
        return success ? ApiResponse.success("删除成功") : ApiResponse.error(403, "删除失败或无权限");
    }

    // 用户撤销删除自己的附件
    @PostMapping("/{attachmentId}/restore")
    @RequiresPermission({"attachment:undelete", "attachment:edit"})
    public ApiResponse<String> restoreAttachment(@PathVariable String attachmentId) {
        String username = currentUserService.getCurrentUsername();
        boolean success = attachmentManagementService.restoreAttachmentByUser(attachmentId, username);
        return success ? ApiResponse.success("撤销删除成功") : ApiResponse.error(403, "撤销删除失败或无权限");
    }
}