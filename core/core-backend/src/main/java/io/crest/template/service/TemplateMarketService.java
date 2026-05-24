package io.crest.template.service;

import io.crest.api.template.TemplateMarketApi;
import io.crest.api.template.response.MarketBaseResponse;
import io.crest.api.template.response.MarketPreviewBaseResponse;
import io.crest.api.template.vo.MarketMetaDataVO;
import io.crest.template.manage.TemplateCenterManage;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author : WangJiaHao
 * @date : 2023/11/17 13:20
 */
@RestController
@RequestMapping("/templateMarket")
@ConditionalOnProperty(prefix = "crest.internal-lite", name = "enabled", havingValue = "false", matchIfMissing = true)
public class TemplateMarketService implements TemplateMarketApi {

    @Resource
    private TemplateCenterManage templateCenterManage;
    @Override
    public MarketBaseResponse searchTemplate() {
        return templateCenterManage.searchTemplate();
    }
    @Override
    public MarketBaseResponse searchTemplateRecommend() {
        return templateCenterManage.searchTemplateRecommend();
    }

    @Override
    public MarketPreviewBaseResponse searchTemplatePreview() {
        return templateCenterManage.searchTemplatePreview();
    }

    @Override
    public List<String> categories() {
        return templateCenterManage.getCategories();
    }

    @Override
    public List<MarketMetaDataVO> categoriesObject() {
        return templateCenterManage.getCategoriesObject();
    }
}
