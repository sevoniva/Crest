package io.crest.api.share;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.crest.api.share.request.*;
import io.crest.api.visualization.request.VisualizationWorkbranchQueryRequest;
import io.crest.api.share.vo.ShareGridVO;
import io.crest.api.share.vo.ShareProxyVO;
import io.crest.api.share.vo.ShareVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@Tag(name = "可视化管理:分享")
public interface ShareApi {

    @Operation(summary = "查询资源分享状态")
    @Parameter(name = "resourceId", description = "资源ID", required = true, in = ParameterIn.PATH)
    @GetMapping("/status/{resourceId}")
    boolean status(@PathVariable("resourceId") Long resourceId);

    @Operation(summary = "切换资源分享状态")
    @Parameter(name = "resourceId", description = "资源ID", required = true, in = ParameterIn.PATH)
    @PostMapping("/switcher/{resourceId}")
    void switcher(@PathVariable("resourceId") Long resourceId);

    @Operation(summary = "设置分享有效期")
    @PostMapping("/editExp")
    void editExp(@RequestBody ShareExpRequest request);

    @Operation(summary = "编辑分享密码")
    @PostMapping("/editPwd")
    void editPwd(@RequestBody SharePwdRequest request);

    @Operation(summary = "查询分享详情")
    @GetMapping("/detail/{resourceId}")
    @Parameter(name = "resourceId", description = "资源ID", required = true, in = ParameterIn.PATH)
    ShareVO detail(@PathVariable("resourceId") Long resourceId);

    @Operation(summary = "查询分享列表")
    @PostMapping("/query")
    List<ShareGridVO> query(@RequestBody VisualizationWorkbranchQueryRequest request);

    @Operation(summary = "分页查询分享列表")
    @PostMapping("/pager/{goPage}/{pageSize}")
    IPage<ShareGridVO> pager(@PathVariable("goPage") int goPage, @PathVariable("pageSize") int pageSize, @RequestBody VisualizationWorkbranchQueryRequest request);

    @Operation(summary = "查询分享代理信息")
    @PostMapping("/proxyInfo")
    ShareProxyVO proxyInfo(@RequestBody ShareProxyRequest request);

    @Operation(summary = "验证分享")
    @PostMapping("/validate")
    boolean validatePwd(@RequestBody SharePwdValidator validator);

    @Operation(summary = "", hidden = true)
    @GetMapping("/queryRelationByUserId/{uid}")
    Map<String, String> queryRelationByUserId(@PathVariable("uid") Long uid);

    @Operation(summary = "编辑分享uuid")
    @PostMapping("/editUuid")
    String editUuid(@RequestBody ShareUuidEditor editor);
}
