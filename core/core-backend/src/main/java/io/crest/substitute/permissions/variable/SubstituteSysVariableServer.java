package io.crest.substitute.permissions.variable;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.crest.api.permissions.variable.api.SysVariablesApi;
import io.crest.api.permissions.variable.dto.SysVariableDto;
import io.crest.api.permissions.variable.dto.SysVariableValueDto;
import io.crest.exception.DEException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController("sysVariableServer")
@RequestMapping("/sysVariable")
public class SubstituteSysVariableServer implements SysVariablesApi {

    @Override
    @PostMapping("/create")
    public SysVariableDto create(@RequestBody SysVariableDto sysVariableDto) {
        throwUnsupported();
        return sysVariableDto;
    }

    @Override
    @PostMapping("/edit")
    public SysVariableDto edit(@RequestBody SysVariableDto sysVariableDto) {
        throwUnsupported();
        return sysVariableDto;
    }

    @Override
    @GetMapping("/delete/{id}")
    public void delete(@PathVariable("id") Long id) {
        throwUnsupported();
    }

    @Override
    @GetMapping("/detail/{id}")
    public SysVariableDto detail(@PathVariable("id") Long id) {
        throwUnsupported();
        return null;
    }

    @Override
    @PostMapping("/query")
    public List<SysVariableDto> query(@RequestBody(required = false) SysVariableDto sysVariableDto) {
        return Collections.emptyList();
    }

    @Override
    @PostMapping("/value/create")
    public SysVariableValueDto createValue(@RequestBody SysVariableValueDto sysVariableDto) {
        throwUnsupported();
        return sysVariableDto;
    }

    @Override
    @PostMapping("/value/edit")
    public SysVariableValueDto editValue(@RequestBody SysVariableValueDto sysVariableDto) {
        throwUnsupported();
        return sysVariableDto;
    }

    @Override
    @GetMapping("/value/delete/{id}")
    public void deleteValue(@PathVariable("id") String id) {
        throwUnsupported();
    }

    @Override
    @GetMapping("/value/selected/{id}")
    public List<SysVariableValueDto> selectVariableValue(@PathVariable("id") Long id) {
        return Collections.emptyList();
    }

    @Override
    @PostMapping("/value/selected/{goPage}/{pageSize}")
    public IPage<SysVariableValueDto> selectPage(@PathVariable("goPage") int goPage, @PathVariable("pageSize") int pageSize, @RequestBody(required = false) SysVariableValueDto sysVariableValueDto) {
        return new Page<SysVariableValueDto>(goPage, pageSize).setRecords(Collections.emptyList());
    }

    @Override
    @PostMapping("/value/batchDel")
    public void batchDel(@RequestBody List<Long> ids) {
        throwUnsupported();
    }

    @Override
    public Map<Long, Map<String, String>> queryBatchSysVariable(List<Long> uids) {
        return Collections.emptyMap();
    }

    private void throwUnsupported() {
        DEException.throwException("当前版本不支持自定义系统变量管理");
    }
}
