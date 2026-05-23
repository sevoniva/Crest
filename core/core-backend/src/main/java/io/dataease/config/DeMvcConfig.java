package io.dataease.config;

import io.dataease.constant.AuthConstant;
import io.dataease.share.interceptor.LinkInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static io.dataease.constant.StaticResourceConstants.*;
import static io.dataease.utils.StaticResourceUtils.ensureBoth;
import static io.dataease.utils.StaticResourceUtils.ensureSuffix;
@Configuration
public class DeMvcConfig implements WebMvcConfigurer {

    @Resource
    private LinkInterceptor linkInterceptor;

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

    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(linkInterceptor).addPathPatterns("/**");
    }
}
