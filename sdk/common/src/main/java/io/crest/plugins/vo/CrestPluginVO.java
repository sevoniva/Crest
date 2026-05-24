package io.crest.plugins.vo;

import lombok.Data;

@Data
public class CrestPluginVO {
    private String moduleName;
    private String config;
    private String icon;
    private String name;
    private String version;
    private String description;
}
