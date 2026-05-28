package io.crest.config;

import io.crest.constant.AuthConstant;
import io.crest.share.interceptor.LinkInterceptor;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static io.crest.constant.StaticResourceConstants.*;
import static io.crest.utils.StaticResourceUtils.ensureBoth;
import static io.crest.utils.StaticResourceUtils.ensureSuffix;
@Configuration
public class DeMvcConfig implements WebMvcConfigurer {

    @Resource
    private LinkInterceptor linkInterceptor;

    @Value("${crest.swagger-ui.version:5.32.6}")
    private String swaggerUiVersion;

    /**
     * Configuring static resource path
     *
     * @param registry registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String workDir = FILE_PROTOCOL + ensureSuffix(WORK_DIR, FILE_SEPARATOR);
        String uploadUrlPattern = ensureBoth(URL_SEPARATOR + UPLOAD_URL_PREFIX, AuthConstant.DE_API_PREFIX, URL_SEPARATOR) + "**";
        registry.addResourceHandler(uploadUrlPattern).addResourceLocations(workDir);

        String i18nDir = FILE_PROTOCOL + ensureSuffix(I18N_DIR, FILE_SEPARATOR);
        String i18nUrlPattern = ensureBoth(I18N_URL, AuthConstant.DE_API_PREFIX, URL_SEPARATOR) + "**";
        registry.addResourceHandler(i18nUrlPattern).addResourceLocations(i18nDir);

        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/" + swaggerUiVersion + "/");

    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(linkInterceptor).addPathPatterns("/**");
    }
}
