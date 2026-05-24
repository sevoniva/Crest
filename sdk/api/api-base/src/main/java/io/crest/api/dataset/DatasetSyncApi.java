package io.crest.api.dataset;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.crest.api.dataset.dto.DatasetSyncLogDTO;
import io.crest.api.dataset.dto.DatasetSyncTaskDTO;
import io.crest.auth.DeApiPath;
import io.crest.auth.DePermit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import static io.crest.constant.AuthResourceEnum.DATASET;

@Tag(name = "数据集管理:同步")
@ApiSupport(order = 978)
@DeApiPath(value = "/datasetSync", rt = DATASET)
public interface DatasetSyncApi {

    @Operation(summary = "保存数据集同步任务")
    @DePermit({"#p0.datasetGroupId+':manage'"})
    @PostMapping("save")
    DatasetSyncTaskDTO save(@RequestBody DatasetSyncTaskDTO task) throws Exception;

    @Operation(summary = "查询数据集同步任务")
    @DePermit({"#p0+':read'"})
    @GetMapping("task/{datasetGroupId}")
    DatasetSyncTaskDTO task(@PathVariable("datasetGroupId") Long datasetGroupId);

    @Operation(summary = "立即执行数据集同步")
    @DePermit({"#p0+':manage'"})
    @PostMapping("execute/{datasetGroupId}")
    DatasetSyncTaskDTO execute(@PathVariable("datasetGroupId") Long datasetGroupId) throws Exception;

    @Operation(summary = "停止数据集同步")
    @DePermit({"#p0+':manage'"})
    @PostMapping("stop/{datasetGroupId}")
    void stop(@PathVariable("datasetGroupId") Long datasetGroupId);

    @Operation(summary = "查询数据集同步日志")
    @DePermit({"#p0+':read'"})
    @GetMapping("logs/{datasetGroupId}")
    List<DatasetSyncLogDTO> logs(@PathVariable("datasetGroupId") Long datasetGroupId);
}
