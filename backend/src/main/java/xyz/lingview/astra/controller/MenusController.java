package xyz.lingview.astra.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import xyz.lingview.astra.annotation.RequiresPermission;
import xyz.lingview.astra.common.ApiResponse;
import xyz.lingview.astra.dto.request.MenuCreateDTO;
import xyz.lingview.astra.dto.request.MenuDeleteDTO;
import xyz.lingview.astra.dto.request.MenuSortItemDTO;
import xyz.lingview.astra.dto.request.MenuUpdateDTO;
import xyz.lingview.astra.dto.request.MenusDTO;
import xyz.lingview.astra.service.CurrentUserService;
import xyz.lingview.astra.service.MenusService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
public class MenusController {

    @Autowired
    private MenusService menusService;

    @Autowired
    private CurrentUserService currentUserService;

    // 获取前端导航菜单
    @GetMapping("/frontendgetmenus")
    public ApiResponse<List<MenusDTO>> frontendGetMenus() {
        return ApiResponse.success(menusService.getAllMenus());
    }

    // 获取后台菜单管理列表
    @GetMapping("/getmenus")
    @RequiresPermission("system:menus:management")
    public ApiResponse<List<MenusDTO>> getMenus() {
        return ApiResponse.success(menusService.getAllMenus());
    }

    // 新增导航菜单
    @PostMapping("/addmenus")
    @RequiresPermission("system:menus:management")
    public ApiResponse<String> addMenus(@RequestBody @Valid MenuCreateDTO dto) {
        String username = currentUserService.getCurrentUsername();
        if (username == null) {
            return ApiResponse.error(401, "用户未登录");
        }
        menusService.addMenus(dto, username);
        return ApiResponse.success("操作成功");
    }

    // 编辑导航菜单
    @PostMapping("/editmenus")
    @RequiresPermission("system:menus:management")
    public ApiResponse<String> editMenus(@RequestBody @Valid MenuUpdateDTO dto) {
        String username = currentUserService.getCurrentUsername();
        if (username == null) {
            return ApiResponse.error(401, "用户未登录");
        }
        menusService.updateMenus(dto, username);
        return ApiResponse.success("操作成功");
    }

    // 删除导航菜单
    @PostMapping("/deletemenus")
    @RequiresPermission("system:menus:management")
    public ApiResponse<String> deleteMenus(@RequestBody @Valid MenuDeleteDTO dto) {
        menusService.deleteMenus(dto.getMenus_id());
        return ApiResponse.success("操作成功");
    }

    // 更新菜单排序
    @PostMapping("/updatesortorder")
    @RequiresPermission("system:menus:management")
    public ApiResponse<String> updateSortOrder(@RequestBody List<@Valid MenuSortItemDTO> items) {
        menusService.updateSortOrder(items);
        return ApiResponse.success("操作成功");
    }
}