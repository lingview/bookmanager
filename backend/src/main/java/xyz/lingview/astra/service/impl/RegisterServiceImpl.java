package xyz.lingview.astra.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.lingview.astra.domain.Register;
import xyz.lingview.astra.domain.UserInformation;
import xyz.lingview.astra.dto.request.RegisterDTO;
import xyz.lingview.astra.dto.response.RegisterResponseDTO;
import xyz.lingview.astra.mapper.RegisterMapper;
import xyz.lingview.astra.mapper.SiteConfigMapper;
import xyz.lingview.astra.mapper.UserInformationMapper;
import xyz.lingview.astra.mapper.UserRoleMapper;
import xyz.lingview.astra.service.CacheService;
import xyz.lingview.astra.service.MailService;
import xyz.lingview.astra.service.NotificationService;
import xyz.lingview.astra.service.RegisterService;
import xyz.lingview.astra.util.CaptchaUtil;
import xyz.lingview.astra.util.PasswordUtil;
import xyz.lingview.astra.util.RandomUtil;
import xyz.lingview.astra.util.SiteConfigUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
@Slf4j
public class RegisterServiceImpl implements RegisterService {

    @Autowired
    private RegisterMapper registerMapper;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private SiteConfigMapper siteConfigMapper;

    @Autowired
    private SiteConfigUtil siteConfigUtil;

    @Autowired
    private MailService mailService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserInformationMapper userInformationMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Override
    public RegisterResponseDTO register(RegisterDTO requestDTO) {
        Integer enableRegister = siteConfigMapper.getEnableRegister();
        if (enableRegister == null || enableRegister != 1) {
            throw new RuntimeException("管理员未启用注册功能");
        }

        String username = requestDTO.getUsername();
        String email = requestDTO.getEmail();
        String phone = requestDTO.getPhone();
        String password = requestDTO.getPassword();
        String captcha = requestDTO.getCaptcha();
        String captchaKey = requestDTO.getCaptchaKey();

        if (isBlank(username)) throw new RuntimeException("用户名不能为空");
        if (isBlank(password)) throw new RuntimeException("密码不能为空");
        if (isBlank(captcha)) throw new RuntimeException("验证码不能为空");
        if (isBlank(captchaKey)) throw new RuntimeException("验证码无效");

        // Validate captcha
        String redisCaptcha = cacheService.get("captcha_" + captchaKey, String.class);
        if (redisCaptcha == null) {
            throw new RuntimeException("验证码已过期，请重新获取");
        }
        if (!CaptchaUtil.validateCaptcha(redisCaptcha, captcha)) {
            cacheService.delete("captcha_" + captchaKey);
            throw new RuntimeException("验证码错误");
        }
        cacheService.delete("captcha_" + captchaKey);

        log.info("注册用户名：{} 手机号：{} 邮箱：{}", username, phone, email);

        int userExists = registerMapper.selectUser(username);
        if (userExists > 0) {
            throw new RuntimeException("账号已存在！");
        }

        if (phone != null && !phone.trim().isEmpty()) {
            int phoneExists = userInformationMapper.selectUserByPhone(phone);
            if (phoneExists > 0) {
                throw new RuntimeException("手机号已存在！");
            }
        }

        if (email != null && !email.trim().isEmpty()) {
            int emailExists = userInformationMapper.selectUserByEmail(email);
            if (emailExists > 0) {
                throw new RuntimeException("邮箱已存在！");
            }
        }

        Register register = new Register();
        register.setUsername(username);
        register.setEmail(email);
        register.setPhone(phone);
        register.setUuid(RandomUtil.generateUUID());
        register.setPassword(PasswordUtil.hashPassword(password));

        int insertResult = registerMapper.insertUser(register);
        if (insertResult <= 0) {
            throw new RuntimeException("注册失败，请稍后再试");
        }

        // Assign default role
        UserInformation newUser = userInformationMapper.getUserByUsername(username);
        Integer newUserId = newUser != null ? newUser.getId() : null;
        Integer defaultRoleId = siteConfigMapper.getRegisterUserPermission();
        if (defaultRoleId != null && newUserId != null) {
            userRoleMapper.insertUserRole(newUserId, defaultRoleId);
        }

        // Send notifications
        if (siteConfigUtil.isNotificationEnabled()) {
            Date date = new Date();
            String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
            String siteName = siteConfigUtil.getSiteName();
            try {
                mailService.sendSimpleMail(email, siteName + " 注册成功",
                        "用户：" + username + " 于 " + formattedDate + " 注册成功");
            } catch (Exception e) {
                log.warn("发送注册邮件失败", e);
            }
            notificationService.sendSystemNotification(username, "系统通知",
                    "用户：" + username + " 于 " + formattedDate + " 注册成功");
        }

        RegisterResponseDTO data = new RegisterResponseDTO();
        data.setSuccess(true);
        data.setMessage("注册成功！");
        return data;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}