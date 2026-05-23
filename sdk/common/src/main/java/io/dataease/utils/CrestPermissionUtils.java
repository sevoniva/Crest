package io.dataease.utils;

import io.dataease.auth.bo.TokenUserBO;
import io.dataease.exception.DEException;
import io.dataease.result.ResultCode;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

public class CrestPermissionUtils {

    public static boolean currentUserIsAdmin() {
        Long uid = currentUserId();
        if (ObjectUtils.isEmpty(uid)) {
            return false;
        }
        try {
            Object userManage = CommonBeanFactory.getBean("crestUserManage");
            Method method = DeReflectUtil.findMethod(userManage.getClass(), "isAdmin");
            Object result = ReflectionUtils.invokeMethod(method, userManage, uid);
            if (result instanceof Boolean bool) {
                return bool;
            }
        } catch (Exception ignored) {
        }
        return uid == 1L;
    }

    public static void requireAdmin() {
        if (!currentUserIsAdmin()) {
            DEException.throwException(ResultCode.PERMISSION_NO_ACCESS.code(), "当前用户没有管理员权限");
        }
    }

    public static boolean canAccessCreator(String createBy) {
        if (currentUserIsAdmin()) {
            return true;
        }
        Long uid = currentUserId();
        return uid != null && StringUtils.equals(String.valueOf(uid), StringUtils.trimToEmpty(createBy));
    }

    public static void requireCreator(String createBy) {
        if (currentUserId() == null) {
            return;
        }
        if (!canAccessCreator(createBy)) {
            DEException.throwException(ResultCode.PERMISSION_NO_ACCESS.code(), "当前用户无权访问该资源");
        }
    }

    public static Long currentUserId() {
        TokenUserBO user = AuthUtils.getUser();
        return user == null ? null : user.getUserId();
    }

    public static String communityScopeSql() {
        Long uid = currentUserId();
        if (ObjectUtils.isEmpty(uid) || currentUserIsAdmin()) {
            return null;
        }
        String safeUid = StringUtils.replace(String.valueOf(uid), "'", "''");
        return """
                SELECT 1 FROM (
                  SELECT CAST(id AS CHAR) AS resource_id, create_by FROM core_datasource
                  UNION ALL SELECT CAST(id AS CHAR) AS resource_id, create_by FROM core_dataset_group
                  UNION ALL SELECT CAST(id AS CHAR) AS resource_id, create_by FROM core_chart_view
                  UNION ALL SELECT CAST(id AS CHAR) AS resource_id, create_by FROM data_visualization_info
                ) crest_resource_scope
                WHERE crest_resource_scope.resource_id = CAST(%s AS CHAR)
                  AND (crest_resource_scope.create_by IS NULL OR crest_resource_scope.create_by <> '""" + safeUid + "')";
    }
}
