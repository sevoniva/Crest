package io.crest.api.menu;

import io.crest.api.menu.vo.MenuVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Tag(name = "菜单")
public interface MenuApi {

    @Operation(summary = "请求菜单")
    @GetMapping("/query")
    List<MenuVO> query();
}
