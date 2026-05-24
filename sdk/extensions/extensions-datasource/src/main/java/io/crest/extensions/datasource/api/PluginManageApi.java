package io.crest.extensions.datasource.api;

import io.crest.extensions.datasource.vo.PluginDatasourceVO;

import java.util.List;

/**
 * @Author Junjun
 */
public interface PluginManageApi {
    List<PluginDatasourceVO> queryPluginDs();
}
