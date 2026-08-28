package xyz.lingview.astra.mapper;

import org.apache.ibatis.annotations.Mapper;
import xyz.lingview.astra.domain.Menus;
import xyz.lingview.astra.dto.request.MenusDTO;
import java.util.List;

@Mapper
public interface MenusMapper {
    List<MenusDTO> selectAllMenus();
    void insertMenus(Menus menus);
    void updateMenus(Menus menus);
    void deleteMenus(String menusId);
    void updateSortOrder(Menus menu);
}