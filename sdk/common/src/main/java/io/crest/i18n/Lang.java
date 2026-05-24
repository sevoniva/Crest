package io.crest.i18n;

import io.crest.utils.CacheUtils;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import static io.crest.constant.CacheConstant.UserCacheConstant.USER_COMMUNITY_LANGUAGE;

@Getter
public enum Lang {

    zh_CN("zh-CN"), zh_TW("zh-TW"), en_US("en-US");

    private final String desc;

    Lang(String desc) {
        this.desc = desc;
    }

    public static Lang getLang(String lang) {
        Lang result = getLangWithoutDefault(lang);
        if (result == null) {
            result = zh_CN;
        }
        return result;
    }

    public static Lang getLangWithoutDefault(String lang) {
        if (StringUtils.isBlank(lang)) {
            return null;
        }
        for (Lang lang1 : values()) {
            if (Strings.CI.equals(lang1.getDesc(), lang)) {
                return lang1;
            }
        }
        if (Strings.CI.startsWith(lang, "zh-CN")) {
            return zh_CN;
        }
        if (Strings.CI.startsWith(lang, "zh-HK") || Strings.CI.startsWith(lang, "zh-TW")) {
            return zh_TW;
        }
        if (Strings.CI.startsWith(lang, "en")) {
            return en_US;
        }
        return null;
    }

    public static boolean isChinese() {
        String lang = null;
        Object langObj = CacheUtils.get(USER_COMMUNITY_LANGUAGE, "de");
        if (ObjectUtils.isNotEmpty(langObj) && StringUtils.isNotBlank(langObj.toString())) {
            lang = langObj.toString();
        }

        if (StringUtils.isBlank(lang)) {
            return true;
        }
        if (Strings.CI.startsWith(lang, "zh")) {
            return true;
        }
        return false;
    }

}
