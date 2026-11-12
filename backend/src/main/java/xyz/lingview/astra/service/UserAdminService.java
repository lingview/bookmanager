package xyz.lingview.astra.service;

import xyz.lingview.astra.domain.Role;
import xyz.lingview.astra.dto.request.PageResult;
import xyz.lingview.astra.dto.response.UserAdminRowDTO;

import java.util.List;

public interface UserAdminService {

    PageResult<UserAdminRowDTO> listUsers(int pageNum, int pageSize);

    List<Role> listRoles();

    void updateRoles(Integer userId, List<Integer> roleIds);

    boolean isAdmin(String username);
}
