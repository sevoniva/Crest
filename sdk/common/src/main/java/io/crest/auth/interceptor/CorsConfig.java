package io.crest.auth.interceptor;

import io.crest.constant.AuthConstant;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${crest.cors-strict:false}")
    private boolean corsStrict;


    @Value("#{'${crest.origin-list:http://127.0.0.1:8100}'.split(',')}")
    private List<String> originList;

    private CorsRegistration operateCorsRegistration;

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(AuthConstant.DE_API_PREFIX, c -> c.isAnnotationPresent(RestController.class) && c.getPackageName().startsWith("io.crest"));
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        operateCorsRegistration = registry.addMapping("/**")
                .allowCredentials(false)
                .allowedHeaders("*")
                .maxAge(3600)
                .allowedMethods("GET", "POST", "DELETE");
        if (corsStrict) {
            operateCorsRegistration.allowedOrigins(originList.toArray(new String[0]));
            return;
        }
        operateCorsRegistration.allowedOrigins("*");
    }

    public void addAllowedOrigins(List<String> origins) {
        if (!corsStrict || CollectionUtils.isEmpty(origins)) {
            return;
        }
        origins.addAll(originList);
        List<String> newOrigins = origins.stream().distinct().toList();
        String[] originArray = newOrigins.toArray(new String[0]);
        operateCorsRegistration.allowedOrigins(originArray);
    }
}
