package io.crest.listener;

import io.crest.utils.LogUtil;
import io.crest.template.manage.TemplateLocalParseManage;
import jakarta.annotation.Resource;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(value = 3)
public class TemplateInitListener implements ApplicationListener<ApplicationReadyEvent> {

    @Resource
    private TemplateLocalParseManage templateLocalParseManage;

    @Value("${crest.internal-lite.enabled:false}")
    private boolean internalLiteEnabled;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        if (internalLiteEnabled) {
            LogUtil.info("=====Template init skipped in internal lite mode=====");
            return;
        }
        LogUtil.info("=====Template init from code [Start]=====");
        try{
            templateLocalParseManage.doInit();
        }catch (Exception e){
            LogUtil.error("=====Template init from code ERROR=====");
        }
        LogUtil.info("=====Template init from code [End]=====");
    }
}
