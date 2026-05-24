package io.crest.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crest.utils.CommonBeanFactory;
import io.crest.utils.LogUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@ConditionalOnMissingBean(name = "loginServer")
@Configuration
@SuppressWarnings("unchecked")
public class SubstituleLoginConfig {

    @Value("${crest.path.substitule:classpath:substitule.json}")
    private String jsonFilePath;

    private static volatile String pwd;

    private static volatile boolean ready = false;


    @ConditionalOnMissingBean(name = "loginServer")
    @Bean
    public Map<String, Object> substituleLoginData(ResourceLoader resourceLoader) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File jsonFile = new File(jsonFilePath);
        if (!jsonFile.exists()) {
            pwd = CommonBeanFactory.getBean(Environment.class).getProperty("crest.default-pwd", "admin");
            modifyPwd(pwd);
        }
        Map<String, Object> data = objectMapper.readValue(jsonFile, Map.class);
        if (ObjectUtils.isNotEmpty(data.get("pwd"))) {
            pwd = data.get("pwd").toString();
        }
        ready = true;
        return data;
    }

    public static String getPwd() {
        if (ready && StringUtils.isNotBlank(pwd)) {
            return pwd;
        }
        synchronized (SubstituleLoginConfig.class) {
            if (ready && StringUtils.isNotBlank(pwd)) {
                return pwd;
            }
            ready = true;
            Object substituleLoginDataObject = CommonBeanFactory.getBean("substituleLoginData");
            if (substituleLoginDataObject != null) {
                Map<String, Object> substituleLoginData = (Map<String, Object>) substituleLoginDataObject;
                if (ObjectUtils.isNotEmpty(substituleLoginData.get("pwd"))) {
                    pwd = substituleLoginData.get("pwd").toString();
                }
            }
            if (StringUtils.isBlank(pwd)) {
                pwd = CommonBeanFactory.getBean(Environment.class).getProperty("crest.default-pwd", "admin");
            }
            return pwd;
        }
    }

    public void modifyPwd(String pwd) {
        File file = new File(jsonFilePath);
        Map<String, String> myObject = new HashMap<>();
        myObject.put("pwd", pwd);
        SubstituleLoginConfig.pwd = pwd;
        ObjectMapper mapper = new ObjectMapper();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            // 将对象写入文件
            mapper.writeValue(fos, myObject);
        } catch (IOException e) {
            LogUtil.error(e.getCause(), new Throwable(e));
        }
    }
}
