package xyz.lingview.astra.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.lingview.astra.domain.SiteConfig;
import xyz.lingview.astra.dto.request.HeroDTO;
import xyz.lingview.astra.mapper.SiteConfigMapper;
import xyz.lingview.astra.service.CacheService;
import xyz.lingview.astra.service.SiteConfigService;

@Service
@Slf4j
public class SiteConfigServiceImpl implements SiteConfigService {

    @Autowired
    private SiteConfigMapper siteConfigMapper;

    @Autowired
    private CacheService cacheService;


    @Override
    public SiteConfig getSiteConfig() {
        try {
            SiteConfig siteConfig = cacheService.get("astra:site_config", SiteConfig.class);
            if (siteConfig != null) {
                return siteConfig;
            }
        } catch (Exception e) {
            log.warn("从缓存读取站点配置失败，回退到数据库查询", e);
        }
        return siteConfigMapper.getSiteConfig();
    }

    @Override
    public HeroDTO getHeroConfig() {
        SiteConfig config = getSiteConfig();
        if (config != null) {
            HeroDTO heroDTO = new HeroDTO();
            heroDTO.setTitle(config.getHero_title());
            heroDTO.setSubtitle(config.getHero_subtitle());
            heroDTO.setImage(config.getHero_image());
            return heroDTO;
        }
        return null;
    }

    @Override
    public String getCopyright() {
        SiteConfig config = getSiteConfig();
        if (config != null) {
            return config.getCopyright();
        }
        return null;
    }

    @Override
    public String getSiteName() {
        SiteConfig config = getSiteConfig();
        if (config != null) {
            return config.getSite_name();
        }
        return null;
    }

    @Override
    public String getSiteIcon() {
        SiteConfig config = getSiteConfig();
        if (config != null) {
            return config.getSite_icon();
        }
        return null;
    }

    @Override
    public String getSiteTheme() {
        SiteConfig config = getSiteConfig();
        if (config != null) {
            return config.getSite_theme();
        }
        return null;
    }

    @Override
    public String getIcpRecordNumber() {
        SiteConfig config = getSiteConfig();
        if (config != null) {
            return config.getIcp_record_number();
        }
        return null;
    }

    @Override
    public String getMpsRecordNumber() {
        SiteConfig config = getSiteConfig();
        if (config != null) {
            return config.getMps_record_number();
        }
        return null;
    }

    @Override
    public boolean updateSiteTheme(String themeName) {
        try {
            SiteConfig config = getSiteConfig();
            if (config != null) {
                config.setSite_theme(themeName);
                int result = siteConfigMapper.updateSiteConfig(config);
                if (result > 0) {
                    cacheService.set("astra:site_config", config);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.error("更新站点主题失败", e);
            return false;
        }
    }

    @Override
    public boolean updateSiteConfig(SiteConfig siteConfig) {
        try {
            int result = siteConfigMapper.updateSiteConfig(siteConfig);
            if (result > 0) {
                cacheService.set("astra:site_config", siteConfig);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("更新站点配置失败", e);
            return false;
        }
    }

    @Override
    public Integer getEnableRegister() {
        SiteConfig config = getSiteConfig();
        if (config != null) {
            return config.getEnable_register();
        }
        return null;
    }

    @Override
    public Integer getRegisterUserPermission() {
        SiteConfig config = getSiteConfig();
        if (config != null) {
            return config.getRegister_user_permission() == null
                    ? null : Integer.valueOf(config.getRegister_user_permission().toString());
        }
        return siteConfigMapper.getRegisterUserPermission();
    }

    @Override
    public boolean updateRegisterSettings(Integer enableRegister, Integer registerRoleId) {
        try {
            int rows = siteConfigMapper.updateRegisterSettings(
                    enableRegister != null && enableRegister == 1 ? 1 : 0, registerRoleId);
            cacheService.delete("astra:site_config");
            getSiteConfig();
            return rows >= 0;
        } catch (Exception e) {
            log.error("更新注册设置失败", e);
            return false;
        }
    }

    @Override
    public Integer getEnableLlm() {
        SiteConfig config = getSiteConfig();
        if (config != null) {
            return config.getEnable_llm();
        }
        return null;
    }

    @Override
    public String getGlobalHeadCode() {
        SiteConfig config = getSiteConfig();
        if (config != null) {
            return config.getGlobal_head_code();
        }
        return null;
    }

    @Override
    public String getContentHeadCode() {
        SiteConfig config = getSiteConfig();
        if (config != null) {
            return config.getContent_head_code();
        }
        return null;
    }

    @Override
    public String getFooterCode() {
        SiteConfig config = getSiteConfig();
        if (config != null) {
            return config.getFooter_code();
        }
        return null;
    }

    @Override
    public Integer getEnableImageCompression() {
        SiteConfig config = getSiteConfig();
        if (config != null) {
            return config.getEnable_image_compression();
        }
        return null;
    }

    @Override
    public Integer getImageCompressionThreads() {
        SiteConfig config = getSiteConfig();
        if (config != null) {
            return config.getImage_compression_threads();
        }
        return 5;
    }

    @Override
    public Integer getProxyResourceDownload() {
        SiteConfig config = getSiteConfig();
        if (config != null) {
            return config.getProxy_resource_download();
        }
        return 0;
    }
}