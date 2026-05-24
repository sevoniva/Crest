package io.crest.extensions.datafilling.api;

import io.crest.extensions.datafilling.vo.PluginDataFillingVO;

import java.util.List;

public interface DfPluginManageApi {
    List<PluginDataFillingVO> queryPluginDf();
}
