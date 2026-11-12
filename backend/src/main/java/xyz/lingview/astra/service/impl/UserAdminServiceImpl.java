package xyz.lingview.astra.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.lingview.astra.domain.Role;
import xyz.lingview.astra.domain.UserInformation;
import xyz.lingview.astra.dto.request.PageResult;
import xyz.lingview.astra.dto.response.UserAdminRowDTO;
import xyz.lingview.astra.mapper.UserInformationMapper;
import xyz.lingview.astra.mapper.UserRoleMapper;
import xyz.lingview.astra.service.UserAdminService;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserAdminServiceImpl implements UserAdminService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    @Autowired
    private UserInformationMapper userInformationMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Override
    public PageResult<UserAdminRowDTO> listUsers(int pageNum, int pageSize) {
        int page = Math.max(pageNum, 1);
        int size = pageSize <= 0 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);
        int total = userInformationMapper.countUsers();
        List<UserInformation> users = userInformationMapper.selectUsersPage((page - 1) * size, size);
        List<UserAdminRowDTO> rows = new ArrayList<>(users.size());
        for (UserInformation user : users) {
            UserAdminRowDTO row = new UserAdminRowDTO();
            row.setId(user.getId());
            row.setUuid(user.getUuid());
            row.setUsername(user.getUsername());
            row.setEmail(user.getEmail());
            row.setPhone(user.getPhone());
            row.setCreate_time(user.getCreate_time() == null
                    ? null : user.getCreate_time().toString().replace('T', ' '));
            List<Role> roles = userRoleMapper.selectRolesByUserId(user.getId());
            row.setRoles(roles);
            row.setRole_ids(roles.stream().map(Role::getId).collect(Collectors.toList()));
            rows.add(row);
        }

        PageResult<UserAdminRowDTO> result = new PageResult<>();
        result.setData(rows);
        result.setTotal(total);
        result.setPage(page);
        result.setSize(size);
        result.setTotal_pages((int) Math.ceil((double) total / size));
        return result;
    }

    @Override
    public List<Role> listRoles() {
        return userRoleMapper.selectAllRoles();
    }

    @Override
    @Transactional
    public void updateRoles(Integer userId, List<Integer> roleIds) {
        if (userId == null) {
            throw new RuntimeException("用户不存在");
        }
        userRoleMapper.deleteUserRolesByUserId(userId);
        if (roleIds != null) {
            for (Integer roleId : new LinkedHashSet<>(roleIds)) {
                if (roleId != null) {
                    userRoleMapper.insertUserRole(userId, roleId);
                }
            }
        }
    }

    @Override
    public boolean isAdmin(String username) {
        if (username == null) {
            return false;
        }
        UserInformation user = userInformationMapper.getUserByUsername(username);
        if (user == null) {
            return false;
        }
        return userRoleMapper.selectRolesByUserId(user.getId()).stream()
                .anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getCode()));
    }
}
