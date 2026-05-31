package io.crest.substitute.permissions.user;


import com.baomidou.mybatisplus.core.metadata.IPage;
import io.crest.api.permissions.user.dto.LangSwitchRequest;
import io.crest.api.permissions.user.dto.ModifyPwdRequest;
import io.crest.api.permissions.user.dto.UserCreator;
import io.crest.api.permissions.user.dto.UserEditor;
import io.crest.api.permissions.user.dto.UserGridRequest;
import io.crest.api.permissions.user.dto.EnableSwitchRequest;
import io.crest.api.permissions.user.vo.CurIpVO;
import io.crest.api.permissions.user.vo.CurUserVO;
import io.crest.api.permissions.user.vo.UserFormVO;
import io.crest.api.permissions.user.vo.UserGridVO;
import io.crest.api.permissions.user.vo.UserImportVO;
import io.crest.api.permissions.user.vo.UserItem;
import io.crest.auth.bo.TokenUserBO;
import io.crest.constant.LogOT;
import io.crest.constant.LogST;
import io.crest.exception.DEException;
import io.crest.i18n.Lang;
import io.crest.log.DeLog;
import io.crest.result.ResultCode;
import io.crest.substitute.permissions.user.model.CrestUser;
import io.crest.substitute.permissions.user.model.UserImportRow;
import io.crest.utils.AuthUtils;
import io.crest.utils.CacheUtils;
import io.crest.utils.CommonExcelUtils;
import io.crest.utils.CrestPermissionUtils;
import io.crest.utils.IPUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static io.crest.constant.CacheConstant.UserCacheConstant.USER_COMMUNITY_LANGUAGE;

@Component
@ConditionalOnMissingBean(name = "userServer")
@RestController
@RequestMapping("/user")
public class SubstituteUserServer {

    @Resource
    private CrestUserManage crestUserManage;

    @DeLog(ot = LogOT.READ, st = LogST.USER)
    @GetMapping("/info")
    public CurUserVO info() {
        Long uid = currentUserId();
        CrestUser user = crestUserManage.queryById(uid);
        if (user == null) {
            DEException.throwException(ResultCode.USER_NOT_EXIST.code(), ResultCode.USER_NOT_EXIST.message());
        }
        CurUserVO result = crestUserManage.toCurrent(user);
        Object langObj = CacheUtils.get(USER_COMMUNITY_LANGUAGE, "de");
        if (ObjectUtils.isNotEmpty(langObj) && StringUtils.isNotBlank(langObj.toString())) {
            result.setLanguage(langObj.toString());
        }
        return result;
    }

    @DeLog(ot = LogOT.READ, st = LogST.USER)
    @GetMapping("/personInfo")
    public UserFormVO personInfo() {
        Long uid = currentUserId();
        UserFormVO userFormVO = crestUserManage.toForm(crestUserManage.queryById(uid));
        userFormVO.setIp(IPUtils.get());
        return userFormVO;
    }

    @GetMapping("/ipInfo")
    public CurIpVO ipInfo() {
        CurIpVO curIpVO = new CurIpVO();
        curIpVO.setAccount("admin");
        curIpVO.setName("管理员");
        curIpVO.setIp(IPUtils.get());
        return curIpVO;
    }

    @DeLog(ot = LogOT.READ, st = LogST.USER)
    @PostMapping("/pager/{goPage}/{pageSize}")
    public IPage<UserGridVO> pager(@PathVariable("goPage") int goPage, @PathVariable("pageSize") int pageSize, @RequestBody UserGridRequest request) {
        CrestPermissionUtils.requireAdmin();
        return crestUserManage.pager(goPage, pageSize, request);
    }

    @DeLog(ot = LogOT.READ, st = LogST.USER, id = "#p0")
    @GetMapping("/queryById/{id}")
    public UserFormVO queryById(@PathVariable("id") Long id) {
        CrestPermissionUtils.requireAdmin();
        return crestUserManage.toForm(crestUserManage.queryById(id));
    }

    @DeLog(ot = LogOT.CREATE, st = LogST.USER)
    @PostMapping("/create")
    public Long create(@RequestBody UserCreator creator) {
        CrestPermissionUtils.requireAdmin();
        return crestUserManage.create(creator);
    }

    @DeLog(ot = LogOT.MODIFY, st = LogST.USER, id = "#p0.id")
    @PostMapping("/edit")
    public void edit(@RequestBody UserEditor editor) {
        CrestPermissionUtils.requireAdmin();
        crestUserManage.edit(editor);
    }

    @DeLog(ot = LogOT.DELETE, st = LogST.USER, id = "#p0")
    @PostMapping("/delete/{id}")
    public void delete(@PathVariable("id") Long id) {
        CrestPermissionUtils.requireAdmin();
        crestUserManage.delete(id);
    }

    @DeLog(ot = LogOT.MODIFY, st = LogST.USER, id = "#p0.id")
    @PostMapping("/enable")
    public void enable(@RequestBody EnableSwitchRequest request) {
        CrestPermissionUtils.requireAdmin();
        crestUserManage.enable(request);
    }

    @GetMapping("/defaultPwd")
    public String defaultPwd() {
        CrestPermissionUtils.requireAdmin();
        return "";
    }

    @DeLog(ot = LogOT.MODIFY, st = LogST.USER, id = "#p0")
    @PostMapping("/resetPwd/{id}")
    public void resetPwd(@PathVariable("id") Long id) {
        CrestPermissionUtils.requireAdmin();
        crestUserManage.resetPwd(id);
    }

    @DeLog(ot = LogOT.MODIFY, st = LogST.USER)
    @PostMapping("/modifyPwd")
    public void modifyPwd(@RequestBody ModifyPwdRequest request) {
        Long uid = ObjectUtils.isEmpty(request.getUid()) ? AuthUtils.getUser().getUserId() : request.getUid();
        crestUserManage.modifyPwd(uid, request.getPwd(), request.getNewPwd());
    }

    @DeLog(ot = LogOT.READ, st = LogST.USER)
    @GetMapping("/queryByAccount/{account}")
    public CurUserVO queryByAccount(@PathVariable("account") String account) {
        return crestUserManage.toCurrent(crestUserManage.queryByAccount(account));
    }

    @PostMapping("/byCurOrg")
    public List<UserItem> byCurOrg(@RequestBody(required = false) UserGridRequest request) {
        return crestUserManage.usersByCurrentOrg(request == null ? null : request.getKeyword());
    }

    @PostMapping("/batchImport")
    public UserImportVO batchImport(@RequestParam("file") MultipartFile file) throws Exception {
        CrestPermissionUtils.requireAdmin();
        return crestUserManage.importUsers(file);
    }

    @PostMapping("/batchDel")
    public void batchDel(@RequestBody List<Long> ids) {
        CrestPermissionUtils.requireAdmin();
        if (ids != null) {
            ids.forEach(crestUserManage::delete);
        }
    }

    @PostMapping("/excelTemplate")
    public void excelTemplate(HttpServletResponse response) throws Exception {
        CrestPermissionUtils.requireAdmin();
        UserImportRow row = new UserImportRow();
        row.setAccount("user01");
        row.setName("张三");
        row.setEmail("user01@example.com");
        row.setPhone("13800000000");
        CommonExcelUtils.writeExcel(response, List.of(row), UserImportRow.class, List.of(), "user-import-template", "用户导入模板");
    }

    @GetMapping("/errorRecord/{key}")
    public void errorRecord(@PathVariable("key") String key, HttpServletResponse response) throws Exception {
        response.setContentType("text/plain;charset=UTF-8");
        response.getOutputStream().write("导入失败记录请根据返回数量检查源文件。".getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping("/clearErrorRecord/{key}")
    public void clearErrorRecord(@PathVariable("key") String key) {
    }

    @DeLog(ot = LogOT.MODIFY, st = LogST.USER)
    @PostMapping("/switchLanguage")
    public void switchLanguage(@RequestBody LangSwitchRequest request) {
        String lang = request.getLang();
        if (Strings.CI.equals(Lang.zh_CN.getDesc(), lang)) {
            lang = Lang.zh_CN.getDesc();
        } else if (Strings.CI.equalsAny(lang, "en", "tw")) {
            lang = lang.toLowerCase();
        } else {
            DEException.throwException("无效language");
        }
        CacheUtils.put(USER_COMMUNITY_LANGUAGE, "de", lang);
    }

    private Long currentUserId() {
        TokenUserBO user = AuthUtils.getUser();
        if (user == null || user.getUserId() == null) {
            DEException.throwException(ResultCode.USER_NOT_LOGGED_IN.code(), ResultCode.USER_NOT_LOGGED_IN.message());
        }
        return user.getUserId();
    }
}
