package io.crest.utils;

import io.crest.constant.SystemSettingConstants;

import java.util.List;

public class SystemSettingUtils {

    public static boolean restrictedSetting(String pkey) {

        List<String> restrictedSettingList = List.of(SystemSettingConstants.AUTO_CREATE_USER,
                SystemSettingConstants.LOG_LIVE_TIME,
                SystemSettingConstants.PLATFORM_OID,
                SystemSettingConstants.DIP,
                SystemSettingConstants.PVP,
                SystemSettingConstants.PLATFORM_RID,
                SystemSettingConstants.DEFAULT_LOGIN,
                SystemSettingConstants.THRESHOLD_LOG_LIVE_TIME,
                SystemSettingConstants.DATA_FILLING_LOG_LIVE_TIME,
                SystemSettingConstants.LOGIN_LIMIT,
                SystemSettingConstants.LOGIN_LIMIT_RATE,
                SystemSettingConstants.LOGIN_LIMIT_TIME,
                SystemSettingConstants.THRESHOLD_LIMIT);
        return restrictedSettingList.contains(pkey);
    }
}
