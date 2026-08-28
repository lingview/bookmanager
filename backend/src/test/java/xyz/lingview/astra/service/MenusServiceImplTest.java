package xyz.lingview.astra.service;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.lingview.astra.dto.request.MenuCreateDTO;
import xyz.lingview.astra.dto.request.MenuSortItemDTO;
import xyz.lingview.astra.dto.request.MenusDTO;
import xyz.lingview.astra.mapper.MenusMapper;
import xyz.lingview.astra.mapper.UserInformationMapper;
import xyz.lingview.astra.service.impl.MenusServiceImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenusServiceImplTest {

    @Mock
    private MenusMapper menusMapper;

    @Mock
    private UserInformationMapper userInformationMapper;

    @Mock
    private SqlSessionFactory sqlSessionFactory;

    @InjectMocks
    private MenusServiceImpl menusService;

    @Test
    void getAllMenus_shouldReturnList() {
        MenusDTO menu = new MenusDTO();
        menu.setMenus_name("首页");
        when(menusMapper.selectAllMenus()).thenReturn(List.of(menu));

        List<MenusDTO> result = menusService.getAllMenus();
        assertEquals(1, result.size());
        assertEquals("首页", result.get(0).getMenus_name());
    }

    @Test
    void addMenus_shouldInsert() {
        when(userInformationMapper.selectUserUUID("admin")).thenReturn("uuid-xxx");

        MenuCreateDTO dto = new MenuCreateDTO();
        dto.setMenus_name("新菜单");
        dto.setMenus_url("/new");

        menusService.addMenus(dto, "admin");

        verify(menusMapper).insertMenus(argThat(menu ->
                menu.getMenus_name().equals("新菜单") &&
                menu.getMenus_url().equals("/new") &&
                menu.getUser_id().equals("uuid-xxx") &&
                menu.getStatus() == 1
        ));
    }

    @Test
    void deleteMenus_shouldCallMapper() {
        menusService.deleteMenus("menu_xxx");
        verify(menusMapper).deleteMenus("menu_xxx");
    }

    @Test
    void updateSortOrder_shouldBatchUpdate() {
        MenuSortItemDTO item = new MenuSortItemDTO();
        item.setMenus_id("menu_1");
        item.setSort_order(1);

        SqlSession sqlSession = mock(SqlSession.class);
        MenusMapper batchMapper = mock(MenusMapper.class);
        when(sqlSessionFactory.openSession(ExecutorType.BATCH)).thenReturn(sqlSession);
        when(sqlSession.getMapper(MenusMapper.class)).thenReturn(batchMapper);

        assertDoesNotThrow(() -> menusService.updateSortOrder(List.of(item)));
    }
}