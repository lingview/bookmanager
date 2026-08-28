package xyz.lingview.astra.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import xyz.lingview.astra.domain.SystematicNotification;
import xyz.lingview.astra.dto.request.PageResult;
import xyz.lingview.astra.mapper.SystematicNotificationMapper;
import xyz.lingview.astra.mapper.UserInformationMapper;
import xyz.lingview.astra.service.NotificationService;

import java.util.List;

/**
 * @Author: lingview
 * @Date: 2026/01/31 16:07:35
 * @Description: 系统通知服务实现
 * @Version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private SystematicNotificationMapper systematicNotificationMapper;

    @Autowired
    private UserInformationMapper userInformationMapper;

    @Async
    @Override
    public void sendSystemNotification(String username, String type, String content) {
        String uuid = userInformationMapper.selectUserUUID(username);
        if (uuid == null) {
            log.warn("用户 {} 不存在，无法发送通知", username);
            return;
        }

        var notification = new SystematicNotification();
        notification.setUuid(uuid);
        notification.setType(type);
        notification.setContent(content);
        notification.setStatus(2);

        int result = systematicNotificationMapper.insertSystematicNotification(notification);
        if (result > 0) {
            log.info("已向用户 {} 发送【{}】: {}", username, type, content);
        } else {
            log.warn("发送【{}】失败，用户: {}, 内容: {}", type, username, content);
        }
    }

    @Override
    public PageResult<SystematicNotification> getSystematicNotifications(String uuid, int pageNum, int pageSize) {
        int total = systematicNotificationMapper.getCountByUuid(uuid);

        int offset = (pageNum - 1) * pageSize;

        List<SystematicNotification> notifications = systematicNotificationMapper.getSystematicNotificationsWithPagination(uuid, offset, pageSize);

        PageResult<SystematicNotification> result = new PageResult<>();
        result.setData(notifications);
        result.setTotal(total);
        result.setPage(pageNum);
        result.setSize(pageSize);
        result.setTotal_pages((int) Math.ceil((double) total / pageSize));

        return result;
    }

    @Override
    public PageResult<SystematicNotification> getReadNotifications(String uuid, int pageNum, int pageSize) {

        int total = systematicNotificationMapper.getReadCountByUuid(uuid);

        int offset = (pageNum - 1) * pageSize;

        List<SystematicNotification> notifications = systematicNotificationMapper.getReadNotificationsWithPagination(uuid, offset, pageSize);

        PageResult<SystematicNotification> result = new PageResult<>();
        result.setData(notifications);
        result.setTotal(total);
        result.setPage(pageNum);
        result.setSize(pageSize);
        result.setTotal_pages((int) Math.ceil((double) total / pageSize));

        return result;
    }

    @Override
    public PageResult<SystematicNotification> getUnreadNotifications(String uuid, int pageNum, int pageSize) {
        int total = systematicNotificationMapper.getUnreadCountByUuid(uuid);
        int offset = (pageNum - 1) * pageSize;

        List<SystematicNotification> notifications = systematicNotificationMapper.getUnreadNotificationsWithPagination(uuid, offset, pageSize);

        PageResult<SystematicNotification> result = new PageResult<>();
        result.setData(notifications);
        result.setTotal(total);
        result.setPage(pageNum);
        result.setSize(pageSize);
        result.setTotal_pages((int) Math.ceil((double) total / pageSize));

        return result;
    }

    @Override
    public boolean updateNotificationStatus(Integer id, String uuid) {

        SystematicNotification notification = systematicNotificationMapper.selectByIdAndUuid(id, uuid);
        if (notification == null) {
            return false;
        }

        int result = systematicNotificationMapper.updateNotificationStatus(id, 1);
        return result > 0;
    }

    @Override
    public PageResult<SystematicNotification> getSystematicNotificationsByUsername(String username, int pageNum, int pageSize) {
        String uuid = userInformationMapper.selectUserUUID(username);
        if (uuid == null) return new PageResult<>();
        return getSystematicNotifications(uuid, pageNum, pageSize);
    }

    @Override
    public PageResult<SystematicNotification> getReadNotificationsByUsername(String username, int pageNum, int pageSize) {
        String uuid = userInformationMapper.selectUserUUID(username);
        if (uuid == null) return new PageResult<>();
        return getReadNotifications(uuid, pageNum, pageSize);
    }

    @Override
    public PageResult<SystematicNotification> getUnreadNotificationsByUsername(String username, int pageNum, int pageSize) {
        String uuid = userInformationMapper.selectUserUUID(username);
        if (uuid == null) return new PageResult<>();
        return getUnreadNotifications(uuid, pageNum, pageSize);
    }

    @Override
    public boolean updateNotificationStatusByUsername(Integer id, String username) {
        String uuid = userInformationMapper.selectUserUUID(username);
        if (uuid == null) return false;
        return updateNotificationStatus(id, uuid);
    }

    @Override
    public boolean deleteSystematicNotificationByUsername(Integer id, String username) {
        String uuid = userInformationMapper.selectUserUUID(username);
        if (uuid == null) return false;
        return deleteSystematicNotification(id, uuid);
    }

    @Override
    public boolean deleteSystematicNotification(Integer id, String uuid) {
        SystematicNotification notification = systematicNotificationMapper.selectByIdAndUuid(id, uuid);
        if (notification == null) {
            return false;
        }

        int result = systematicNotificationMapper.deleteSystematicNotification(id);
        return result > 0;
    }
}