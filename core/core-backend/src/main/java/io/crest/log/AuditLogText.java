package io.crest.log;

import io.crest.constant.LogOT;

import java.util.Locale;

final class AuditLogText {

    private AuditLogText() {
    }

    static String operationLabel(String operationType) {
        if (operationType == null) return "其他操作";
        return switch (operationType) {
            case "CREATE" -> "新建";
            case "MODIFY" -> "编辑";
            case "DELETE" -> "删除";
            case "READ" -> "查看";
            case "EXPORT" -> "导出";
            case "AUTHORIZE" -> "授权";
            case "UNAUTHORIZE" -> "取消授权";
            case "CREATELINK" -> "创建分享链接";
            case "DELETELINK" -> "删除分享链接";
            case "MODIFYLINK" -> "更新分享链接";
            case "UPLOADFILE" -> "上传";
            case "BIND" -> "绑定";
            case "UNBIND" -> "解绑";
            case "LOGIN" -> "登录";
            case "DOWNLOAD" -> "下载";
            case "TEMPLATE_EXPORT" -> "导出模板";
            case "APP_TEMPLATE_EXPORT" -> "导出应用模板";
            case "PDF_EXPORT" -> "导出 PDF";
            case "IMG_EXPORT" -> "导出图片";
            case "TASK_ENABLE", "SYNC_TASK_ENABLE" -> "启用任务";
            case "TASK_DISENABLE", "SYNC_TASK_DISENABLE" -> "停用任务";
            case "TASK_RUN_IMMEDIATELY", "SYNC_TASK_RUN_IMMEDIATELY" -> "立即执行任务";
            case "SYNC_TASK_RUN_TERMINATION" -> "终止同步任务";
            case "CLEAR" -> "清理";
            default -> operationType;
        };
    }

    static String resourceLabel(String resourceType) {
        if (resourceType == null) return "资源";
        return switch (resourceType) {
            case "PANEL" -> "仪表盘";
            case "SCREEN" -> "数据大屏";
            case "DATASET" -> "数据集";
            case "DATASOURCE" -> "数据源";
            case "STYLE_TEMPLATE" -> "样式模板";
            case "APP_TEMPLATE" -> "应用模板";
            case "USER" -> "用户";
            case "ROLE" -> "角色";
            case "ORG" -> "组织";
            case "VIEW" -> "图表";
            case "LINK" -> "分享链接";
            case "DRIVER" -> "数据源驱动";
            case "DRIVER_FILE" -> "驱动文件";
            case "MENU" -> "菜单权限";
            case "APIKEY" -> "API Key";
            case "DATA_FILLING" -> "数据填报";
            case "DATA" -> "数据";
            case "REPORT_TASK" -> "报告任务";
            case "SYNC_DATASOURCE" -> "同步数据源";
            case "SYNC_TASK" -> "同步任务";
            case "SYNC_TASK_LOG" -> "同步任务日志";
            default -> resourceType;
        };
    }

    static String description(LogOT operationType, String resourceType, String requestUrl) {
        String url = requestUrl == null ? "" : requestUrl.toLowerCase(Locale.ROOT);
        if (url.contains("/login/locallogin")) return "本地账号登录";
        if (url.contains("/sso/login")) return "发起单点登录";
        if (url.contains("/sso/callback")) return "处理单点登录回调";
        if (url.contains("/sso/token/")) return "签发单点登录票据";
        if (url.contains("/logout")) return "退出登录";
        if (url.contains("/auditlog/pager")) return "查询审计日志";
        if (url.contains("/auditlog/statistics")) return "查看审计统计";
        if (url.contains("/auth/busipermission")) return "查看业务资源权限";
        if (url.contains("/auth/menupermission")) return "查看菜单权限";
        if (url.contains("/auth/savebusiper")) return "配置业务资源权限";
        if (url.contains("/auth/savebusitargetper")) return "批量配置业务资源权限";
        if (url.contains("/auth/savemenuper")) return "配置菜单权限";
        if (url.contains("/auth/savemenutargetper")) return "批量配置菜单权限";
        if (url.contains("/role/bycurorg") || url.contains("/role/query")) return "查询角色列表";
        if (url.contains("/role/detail")) return "查看角色详情";
        if (url.contains("/role/mount")) return "为角色添加用户";
        if (url.contains("/role/unmount")) return "从角色移除用户";
        if (url.contains("/org/page/tree")) return "查询组织树";
        if (url.contains("/user/bycurorg") || url.contains("/user/pager")) return "查询用户列表";
        if (url.contains("/user/info")) return "查看当前用户信息";
        if (url.contains("/user/personinfo")) return "查看个人信息";
        if (url.contains("/user/resetpwd")) return "重置用户密码";
        if (url.contains("/user/modifypwd")) return "修改用户密码";
        if (url.contains("/user/enable")) return "变更用户状态";
        if (url.contains("/switchlanguage")) return "切换系统语言";
        if (url.contains("/exportcenter/exporttasks/records")) return "查看导出任务统计";
        if (url.contains("/exportcenter/exporttasks")) return "查询导出任务列表";
        if (url.contains("/exportcenter/exportlimit")) return "查看导出限制";
        if (url.contains("/exportcenter/generatedownloaduri")) return "生成导出文件下载地址";
        if (url.contains("/exportcenter/download")) return "下载导出文件";
        if (url.contains("/exportcenter/delete")) return "删除导出任务";
        if (url.contains("/datasettree/exportdataset")) return "导出数据集";
        if (url.contains("/sso/validate")) return "校验单点登录配置";
        if (url.contains("/sso/config")) return operationType == LogOT.READ ? "查看单点登录配置" : "保存单点登录配置";
        if (url.contains("/engine/validate")) return "校验引擎连接";
        if (url.contains("/engine/")) return operationType == LogOT.READ ? "查看引擎设置" : "维护引擎设置";
        if (url.contains("/sysparameter")) return operationType == LogOT.READ ? "查看系统参数" : "维护系统参数";
        if (url.contains("/querybyid")) return "查看" + resourceLabel(resourceType) + "详情";
        if (url.contains("/tree")) return "查询" + resourceLabel(resourceType) + "目录";
        if (url.contains("/overview")) return "查看数据血缘概览";
        if (url.contains("/create") || url.contains("/add")) return "新建" + resourceLabel(resourceType);
        if (url.contains("/edit") || url.contains("/update")) return "编辑" + resourceLabel(resourceType);
        if (url.contains("/delete") || url.contains("/batchdel") || url.contains("/remove")) return "删除" + resourceLabel(resourceType);
        if (operationType == null) return "操作" + resourceLabel(resourceType);
        return operationLabel(operationType.name()) + resourceLabel(resourceType);
    }
}
