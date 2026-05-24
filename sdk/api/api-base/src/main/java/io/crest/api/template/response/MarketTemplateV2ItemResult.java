package io.crest.api.template.response;

import io.crest.api.template.vo.MarketApplicationVO;
import io.crest.api.template.vo.MarketLatestReleaseVO;
import lombok.Data;

/**
 * @author : WangJiaHao
 * @date : 2023/11/17 13:41
 */
@Data
public class MarketTemplateV2ItemResult {

    private MarketApplicationVO application;

    private MarketLatestReleaseVO latestRelease;

}
