package io.dataease.relation.server;

import io.dataease.relation.dto.RelationGraphDTO;
import io.dataease.relation.dto.RelationResourceDTO;
import io.dataease.relation.dto.RelationResourceRequest;
import io.dataease.relation.manage.RelationManage;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/relation")
public class RelationServer {

    @Resource
    private RelationManage relationManage;

    @PostMapping("/overview")
    public RelationGraphDTO overview() {
        return relationManage.overview();
    }

    @PostMapping("/datasource/{id}")
    public RelationGraphDTO datasource(@PathVariable Long id) {
        return relationManage.datasource(id);
    }

    @PostMapping("/dataset/{id}")
    public RelationGraphDTO dataset(@PathVariable Long id) {
        return relationManage.dataset(id);
    }

    @PostMapping("/dv/{id}")
    public RelationGraphDTO dv(@PathVariable Long id) {
        return relationManage.dv(id);
    }

    @PostMapping("/resources/{type}")
    public List<RelationResourceDTO> resources(@PathVariable String type, @RequestBody(required = false) RelationResourceRequest request) {
        return relationManage.resources(type, request);
    }
}
