package io.dataease.doc;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.apache.commons.lang3.RandomUtils;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@SuppressWarnings("deprecation")
public class SwaggerConfig {

    @Value("${dataease.version}")
    private String version;

    @Bean
    public GlobalOpenApiCustomizer orderGlobalOpenApiCustomizer() {
        return openApi -> {
            if (openApi.getTags() != null) {
                openApi.getTags().forEach(tag -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("x-order", RandomUtils.nextInt(0, 100));
                    tag.setExtensions(map);
                });
            }
        };
    }

    @Bean
    public OpenAPI customOpenAPI() {
        Contact contact = new Contact();
        contact.setName("DataEase");
        contact.setUrl("https://dataease.io");
        return new OpenAPI()
                .info(new Info()
                        .title("DataEaseAPI")
                        .description("人人可用的开源 BI 工具")
                        .termsOfService("https://dataease.io")
                        .contact(contact)
                        .version(version));
    }


    @Bean
    public GroupedOpenApi visualizationApi() {
        return GroupedOpenApi.builder().group("1-visualization").displayName("可视化管理").packagesToScan("io.dataease.visualization", "io.dataease.share").build();
    }

    @Bean
    public GroupedOpenApi chartApi() {
        return GroupedOpenApi.builder().group("2-view").displayName("图表管理").packagesToScan("io.dataease.chart").build();
    }

    @Bean
    public GroupedOpenApi datasetApi() {
        return GroupedOpenApi.builder().group("3-dataset").displayName("数据集管理").packagesToScan("io.dataease.dataset").build();
    }

    @Bean
    public GroupedOpenApi dsApi() {
        return GroupedOpenApi.builder().group("4-datasource").displayName("数据源管理").packagesToScan("io.dataease.datasource").build();
    }

    @Bean
    public GroupedOpenApi basicSettingApi() {
        String[] packageArray = {
                "io.dataease.system",
        };
        return GroupedOpenApi.builder().group("5-permission").displayName("系统设置").packagesToScan(packageArray).build();
    }

    @Bean
    public GroupedOpenApi baseApi() {
        return GroupedOpenApi.builder().group("6-base").displayName("基础功能").packagesToScan("io.dataease.base").build();
    }

    @Bean
    public GroupedOpenApi systemApi() {
        return GroupedOpenApi.builder().group("7-permission").displayName("权限相关").packagesToScan("io.dataease.permissions").build();
    }

    @Bean
    public GroupedOpenApi syncApi() {
        return GroupedOpenApi.builder().group("8-sync").displayName("同步管理").packagesToScan("io.dataease.sync.task").build();
    }


}
