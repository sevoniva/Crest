package io.crest.rsa.starter;

import io.crest.rsa.manage.RsaManage;
import io.crest.utils.LogUtil;
import jakarta.annotation.Resource;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class RsaStarter implements ApplicationRunner {

    @Resource
    private RsaManage rsaManage;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        checkRsa();
    }

    private void checkRsa() {
        try {
            rsaManage.check();
        } catch (Exception e) {
            LogUtil.error(e.getMessage());
        }
    }
}
