package io.crest.relation.server;

import io.crest.constant.LogOT;
import io.crest.constant.LogST;
import io.crest.log.DeLog;
import io.crest.relation.dto.RelationGraphDTO;
import io.crest.relation.dto.RelationResourceDTO;
import io.crest.relation.dto.RelationResourceRequest;
import io.crest.relation.manage.RelationManage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/relation")
@Tag(name = "数据血缘")
public class RelationServer {

    @Resource
    private RelationManage relationManage;

    @DeLog(ot = LogOT.READ, st = LogST.DATASOURCE)
    @PostMapping("/overview")
    @Operation(summary = "查询全局血缘")
    public RelationGraphDTO overview() {
        return relationManage.overview();
    }

    @DeLog(ot = LogOT.READ, st = LogST.DATASOURCE, id = "#p0")
    @PostMapping("/datasource/{id}")
    @Operation(summary = "查询数据源血缘")
    public RelationGraphDTO datasource(@PathVariable Long id) {
        return relationManage.datasource(id);
    }

    @DeLog(ot = LogOT.READ, st = LogST.DATASET, id = "#p0")
    @PostMapping("/dataset/{id}")
    @Operation(summary = "查询数据集血缘")
    public RelationGraphDTO dataset(@PathVariable Long id) {
        return relationManage.dataset(id);
    }

    @DeLog(ot = LogOT.READ, st = LogST.PANEL, id = "#p0")
    @PostMapping("/dv/{id}")
    @Operation(summary = "查询仪表板或大屏血缘")
    public RelationGraphDTO dv(@PathVariable Long id) {
        return relationManage.dv(id);
    }

    @DeLog(ot = LogOT.READ, st = LogST.DATASOURCE)
    @PostMapping("/resources/{type}")
    @Operation(summary = "查询血缘资源")
    public List<RelationResourceDTO> resources(@PathVariable String type, @RequestBody(required = false) RelationResourceRequest request) {
        return relationManage.resources(type, request);
    }
}
