package xyz.lingview.astra.service;

import xyz.lingview.astra.dto.request.MenuCreateDTO;
import xyz.lingview.astra.dto.request.MenuSortItemDTO;
import xyz.lingview.astra.dto.request.MenuUpdateDTO;
import xyz.lingview.astra.dto.request.MenusDTO;
import java.util.List;

public interface MenusService {
    List<MenusDTO> getAllMenus();
    void addMenus(MenuCreateDTO dto, String username);
    void updateMenus(MenuUpdateDTO dto, String username);
    void deleteMenus(String menusId);
    void updateSortOrder(List<MenuSortItemDTO> items);
}