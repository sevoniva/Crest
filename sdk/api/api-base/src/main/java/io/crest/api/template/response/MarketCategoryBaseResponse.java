package io.crest.api.template.response;

import io.crest.api.template.vo.TemplateCategoryVO;
import lombok.Data;

import java.util.List;

/**
 * Author: wangjiahao
 * Date: 2022/7/15
 * Description:
 */
@Data
public class MarketCategoryBaseResponse {
    private List<TemplateCategoryVO> data;
}
