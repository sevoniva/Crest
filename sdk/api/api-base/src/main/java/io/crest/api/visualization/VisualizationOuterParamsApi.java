package io.crest.api.visualization;

import io.crest.api.dataset.vo.CoreDatasetGroupVO;
import io.crest.api.visualization.dto.VisualizationOuterParamsDTO;
import io.crest.api.visualization.response.VisualizationOuterParamsBaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "可视化管理:外部参数")
public interface VisualizationOuterParamsApi {


    @GetMapping("/queryWithVisualizationId/{dvId}")
    @Operation(summary = "查询")
    VisualizationOuterParamsDTO queryWithVisualizationId(@PathVariable("dvId") String dvId);

    @PostMapping("/updateOuterParamsSet")
    @Operation(summary = "更新")
    void updateOuterParamsSet(@RequestBody VisualizationOuterParamsDTO OuterParamsDTO);

    @GetMapping("/getOuterParamsInfo/{dvId}")
    @Operation(summary = "查询基础信息")
    VisualizationOuterParamsBaseResponse getOuterParamsInfo(@PathVariable("dvId") String dvId);

    @GetMapping("/queryDsWithVisualizationId/{dvId}")
    @Operation(summary = "查询涉及数据集基础信息")
    List<CoreDatasetGroupVO> queryDsWithVisualizationId(@PathVariable("dvId") String dvId);
}
