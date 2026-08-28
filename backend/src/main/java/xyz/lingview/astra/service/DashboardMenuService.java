package xyz.lingview.astra.service;

import xyz.lingview.astra.dto.request.DashboardMenuDTO;

public interface DashboardMenuService {
    DashboardMenuDTO getDashboardMenus(String username);
}