package xyz.lingview.astra.service.impl;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.lingview.astra.domain.Menus;
import xyz.lingview.astra.dto.request.MenuCreateDTO;
import xyz.lingview.astra.dto.request.MenuSortItemDTO;
import xyz.lingview.astra.dto.request.MenuUpdateDTO;
import xyz.lingview.astra.dto.request.MenusDTO;
import xyz.lingview.astra.mapper.MenusMapper;
import xyz.lingview.astra.mapper.UserInformationMapper;
import xyz.lingview.astra.service.MenusService;

import java.util.List;
import java.util.UUID;

@Service
public class MenusServiceImpl implements MenusService {

    @Autowired
    private MenusMapper menusMapper;

    @Autowired
    private UserInformationMapper userInformationMapper;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Override
    public List<MenusDTO> getAllMenus() {
        return menusMapper.selectAllMenus();
    }

    @Override
    public void addMenus(MenuCreateDTO dto, String username) {
        Menus menus = new Menus();
        menus.setMenus_id("menu_" + UUID.randomUUID().toString().replace("-", ""));
        menus.setMenus_name(dto.getMenus_name());
        menus.setMenus_url(dto.getMenus_url());

        String userUuid = userInformationMapper.selectUserUUID(username);
        if (userUuid != null) {
            menus.setUser_id(userUuid);
        }

        if (dto.getSort_order() != null) {
            menus.setSort_order(dto.getSort_order());
        } else {
            List<MenusDTO> allMenus = menusMapper.selectAllMenus();
            int maxOrder = allMenus.stream()
                    .mapToInt(menu -> menu.getSort_order() != null ? menu.getSort_order() : 0)
                    .max()
                    .orElse(0);
            menus.setSort_order(maxOrder + 1);
        }

        menus.setStatus(1);
        menusMapper.insertMenus(menus);
    }

    @Override
    public void updateMenus(MenuUpdateDTO dto, String username) {
        Menus menus = new Menus();
        menus.setMenus_id(dto.getMenus_id());
        menus.setMenus_name(dto.getMenus_name());
        menus.setMenus_url(dto.getMenus_url());
        menus.setSort_order(dto.getSort_order());
        menus.setStatus(dto.getStatus());

        String userUuid = userInformationMapper.selectUserUUID(username);
        if (userUuid != null) {
            menus.setUser_id(userUuid);
        }

        menusMapper.updateMenus(menus);
    }

    @Override
    public void deleteMenus(String menusId) {
        menusMapper.deleteMenus(menusId);
    }

    @Override
    public void updateSortOrder(List<MenuSortItemDTO> items) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            MenusMapper mapper = sqlSession.getMapper(MenusMapper.class);

            for (MenuSortItemDTO item : items) {
                Menus menus = new Menus();
                menus.setMenus_id(item.getMenus_id());
                menus.setSort_order(item.getSort_order());
                mapper.updateSortOrder(menus);
            }

            sqlSession.commit();
        } catch (Exception e) {
            throw new RuntimeException("批次更新排序失败", e);
        }
    }
}