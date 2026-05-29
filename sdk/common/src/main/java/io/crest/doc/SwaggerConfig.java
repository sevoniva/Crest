package io.crest.doc;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@SuppressWarnings("deprecation")
public class SwaggerConfig {

    @Value("${crest.version:1.3.0}")
    private String version;

    @Bean
    public GlobalOpenApiCustomizer orderGlobalOpenApiCustomizer() {
        return openApi -> {
            if (openApi.getTags() != null) {
                AtomicInteger order = new AtomicInteger(1);
                openApi.getTags().forEach(tag -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("x-order", order.getAndIncrement());
                    tag.setExtensions(map);
                });
            }
        };
    }

    @Bean
    public OpenAPI customOpenAPI() {
        Contact contact = new Contact();
        contact.setName("Crest");
        contact.setUrl("https://github.com/sevoniva/Crest");
        return new OpenAPI()
                .info(new Info()
                        .title("Crest API")
                        .description("看见数据，读懂业务")
                        .termsOfService("https://github.com/sevoniva/Crest")
                        .contact(contact)
                        .version(version));
    }


    @Bean
    public GroupedOpenApi visualizationApi() {
        return GroupedOpenApi.builder().group("1-visualization").displayName("可视化管理").packagesToScan("io.crest.visualization", "io.crest.share").build();
    }

    @Bean
    public GroupedOpenApi chartApi() {
        return GroupedOpenApi.builder().group("2-view").displayName("图表管理").packagesToScan("io.crest.chart").build();
    }

    @Bean
    public GroupedOpenApi datasetApi() {
        return GroupedOpenApi.builder().group("3-dataset").displayName("数据集管理").packagesToScan("io.crest.dataset").build();
    }

    @Bean
    public GroupedOpenApi dsApi() {
        return GroupedOpenApi.builder().group("4-datasource").displayName("数据源管理").packagesToScan("io.crest.datasource").build();
    }

    @Bean
    public GroupedOpenApi relationApi() {
        return GroupedOpenApi.builder().group("5-relation").displayName("数据血缘").packagesToScan("io.crest.relation").build();
    }

    @Bean
    public GroupedOpenApi exportApi() {
        return GroupedOpenApi.builder().group("6-export").displayName("导出中心").packagesToScan("io.crest.exportCenter").build();
    }

    @Bean
    public GroupedOpenApi basicSettingApi() {
        String[] packageArray = {
                "io.crest.system",
                "io.crest.font",
                "io.crest.menu",
        };
        return GroupedOpenApi.builder().group("7-system").displayName("系统管理").packagesToScan(packageArray).build();
    }

    @Bean
    public GroupedOpenApi baseApi() {
        return GroupedOpenApi.builder().group("8-base").displayName("基础功能").packagesToScan("io.crest.base", "io.crest.resource").build();
    }

    @Bean
    public GroupedOpenApi systemApi() {
        return GroupedOpenApi.builder().group("9-permission").displayName("权限管理").packagesToScan("io.crest.substitute.permissions").build();
    }

    @Bean
    public GroupedOpenApi syncApi() {
        return GroupedOpenApi.builder().group("10-sync").displayName("同步管理").packagesToScan("io.crest.sync.task").build();
    }


}
