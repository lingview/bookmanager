package xyz.lingview.astra.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.theme")
@Slf4j
public class ThemeProperties {

    private String activeTheme = "default";
    private String themesPath = "themes";

    public String getActiveTheme() {
        return activeTheme;
    }

    public void setActiveTheme(String activeTheme) {
        this.activeTheme = activeTheme;
    }

    public String getThemesPath() {
        return themesPath;
    }

    public void setThemesPath(String themesPath) {
        this.themesPath = themesPath;
    }
}