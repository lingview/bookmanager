package xyz.lingview.astra.config;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.flyway.autoconfigure.FlywayProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @Author: lingview
 * @Date: 2026/01/27 11:47:27
 * @Description: Flyway配置
 * @Version: 1.0
 */
@Configuration
@EnableConfigurationProperties(FlywayProperties.class)
@Slf4j
public class FlywayConfig {

    @Bean
    public Flyway flyway(DataSource dataSource, FlywayProperties flywayProperties,
                         @Value("${spring.datasource.url:}") String datasourceUrl) {
        DatabaseType databaseType = DatabaseType.fromUrl(datasourceUrl);
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .baselineOnMigrate(true)
                .baselineVersion("1")
                .locations(databaseType.migrationLocation())
                .encoding("UTF-8")
                .validateOnMigrate(true)
                .outOfOrder(false)
                .load();

        log.info("开始数据库验证及更新，数据库类型: {}", databaseType);
        flyway.migrate();
        log.info("数据库验证更新完毕");

        return flyway;
    }
}