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
import java.util.UUID;

@ConditionalOnMissingBean(name = "loginServer")
@Configuration
@SuppressWarnings("unchecked")
public class SubstituleLoginConfig {
    private static final String PWD_KEY = "pwd";
    private static final String TOKEN_SECRET_KEY = "tokenSecret";

    @Value("${crest.path.substitule:classpath:substitule.json}")
    private String jsonFilePath;

    private static volatile String pwd;
    private static volatile String tokenSecret;

    private static volatile boolean ready = false;


    @ConditionalOnMissingBean(name = "loginServer")
    @Bean
    public Map<String, Object> substituleLoginData(ResourceLoader resourceLoader) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File jsonFile = new File(jsonFilePath);
        Map<String, Object> data = jsonFile.exists() ? objectMapper.readValue(jsonFile, Map.class) : new HashMap<>();
        boolean updated = false;
        String configuredPwd = CommonBeanFactory.getBean(Environment.class).getProperty("crest.default-pwd", "admin");
        pwd = readString(data, PWD_KEY);
        if (StringUtils.isBlank(pwd)) {
            pwd = configuredPwd;
            data.put(PWD_KEY, pwd);
            updated = true;
        }
        tokenSecret = readString(data, TOKEN_SECRET_KEY);
        if (StringUtils.isBlank(tokenSecret)) {
            tokenSecret = generateSecret();
            data.put(TOKEN_SECRET_KEY, tokenSecret);
            updated = true;
        }
        if (updated) {
            writeConfig(jsonFile, data);
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
                String configuredPwd = readString(substituleLoginData, PWD_KEY);
                if (StringUtils.isNotBlank(configuredPwd)) {
                    pwd = configuredPwd;
                }
                String configuredTokenSecret = readString(substituleLoginData, TOKEN_SECRET_KEY);
                if (StringUtils.isNotBlank(configuredTokenSecret)) {
                    tokenSecret = configuredTokenSecret;
                }
            }
            if (StringUtils.isBlank(pwd)) {
                pwd = CommonBeanFactory.getBean(Environment.class).getProperty("crest.default-pwd", "admin");
            }
            return pwd;
        }
    }

    public static String getTokenSecret() {
        getPwd();
        if (StringUtils.isBlank(tokenSecret)) {
            tokenSecret = generateSecret();
        }
        return tokenSecret;
    }

    public void modifyPwd(String pwd) {
        File file = new File(jsonFilePath);
        Map<String, String> myObject = new HashMap<>();
        myObject.put(PWD_KEY, pwd);
        myObject.put(TOKEN_SECRET_KEY, StringUtils.defaultIfBlank(tokenSecret, generateSecret()));
        SubstituleLoginConfig.pwd = pwd;
        SubstituleLoginConfig.tokenSecret = myObject.get(TOKEN_SECRET_KEY);
        try {
            writeConfig(file, myObject);
        } catch (IOException e) {
            LogUtil.error(e.getCause(), new Throwable(e));
        }
    }

    private static void writeConfig(File file, Map<?, ?> config) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            mapper.writeValue(fos, config);
        }
    }

    private static String readString(Map<String, Object> config, String key) {
        Object value = config.get(key);
        return ObjectUtils.isEmpty(value) ? null : value.toString();
    }

    private static String generateSecret() {
        return UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
    }
}
