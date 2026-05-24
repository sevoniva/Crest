package io.crest.api.template.response;

import io.crest.api.template.vo.MarketMetaDataVO;
import lombok.Data;

import java.util.List;

/**
 * Author: wangjiahao
 * Date: 2022/7/15
 * Description:
 */
@Data
public class MarketMetaDataBaseResponse {

    private List<MarketMetaDataVO> deVersion;

    private List<MarketMetaDataVO> templateTypes;

    private List<MarketMetaDataVO> labels;
}
