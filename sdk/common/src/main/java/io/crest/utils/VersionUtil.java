package io.crest.utils;

import org.springframework.core.env.Environment;

public class VersionUtil {


    public static String getRandomVersion() {
        Environment environment = CommonBeanFactory.getBean(Environment.class);
        assert environment != null;
        return environment.getProperty("crest.version", "1.2.0");
    }

}
