package io.crest.menu.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.crest.api.menu.vo.MenuMeta;
import io.crest.api.menu.vo.MenuVO;
import io.crest.i18n.Translator;
import io.crest.menu.bo.MenuTreeNode;
import io.crest.menu.dao.auto.entity.CoreMenu;
import io.crest.menu.dao.auto.mapper.CoreMenuMapper;
import io.crest.utils.BeanUtils;
import io.crest.utils.CrestPermissionUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MenuManage {

    private static final String I18N_PREFIX = "i18n_menu.";

    private final static int ROOTID = 0;

    private static final Set<Long> INTERNAL_LITE_MENU_IDS = Set.of(
            1L, 2L, 3L, 4L, 5L, 6L, 11L, 12L, 15L, 16L, 64L, 66L, 67L, 68L, 69L, 71L
    );
    private static final Set<Long> ADMIN_MENU_IDS = Set.of(15L, 16L, 64L, 67L, 68L, 69L, 71L);

    @Resource
    private CoreMenuMapper coreMenuMapper;

    @Value("${crest.internal-lite.enabled:false}")
    private boolean internalLiteEnabled;
    public List<MenuVO> query(List<CoreMenu> coreMenus) {
        List<CoreMenu> menus = internalLiteEnabled
                ? coreMenus.stream().filter(menu -> INTERNAL_LITE_MENU_IDS.contains(menu.getId())).toList()
                : coreMenus;
        if (!CrestPermissionUtils.currentUserIsAdmin()) {
            menus = menus.stream().filter(menu -> !ADMIN_MENU_IDS.contains(menu.getId())).toList();
        }
        List<MenuTreeNode> menuTreeNodes = new ArrayList<>(menus.stream().map(menu -> BeanUtils.copyBean(new MenuTreeNode(), menu)).toList());
        menuTreeNodes.sort(Comparator.comparing(MenuTreeNode::getMenuSort));
        List<MenuTreeNode> treeNodes = buildPOTree(menuTreeNodes);
        return convertTree(treeNodes);
    }

    public List<CoreMenu> coreMenus() {
        QueryWrapper<CoreMenu> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("menu_sort");
        return coreMenuMapper.selectList(wrapper);
    }


    private List<MenuTreeNode> buildPOTree(List<MenuTreeNode> coreMenus) {
        List<MenuTreeNode> result = new ArrayList<>();
        Map<Long, List<MenuTreeNode>> childMap = coreMenus.stream().collect(Collectors.groupingBy(CoreMenu::getPid));
        coreMenus.forEach(po -> {
            po.setChildren(childMap.get(po.getId()));
            if (po.getPid() == ROOTID) {
                result.add(po);
            }
        });
        return result;
    }

    private List<MenuVO> convertTree(List<MenuTreeNode> roots) {
        List<MenuVO> result = new ArrayList<>();
        for (MenuTreeNode menuTreeNode : roots) {
            MenuVO vo = convert(menuTreeNode);
            List<MenuTreeNode> children = null;
            if (CollectionUtils.isNotEmpty(children = menuTreeNode.getChildren())) {
                vo.setChildren(convertTree(children));
            }
            if (CollectionUtils.isNotEmpty(vo.getChildren()) || menuTreeNode.getType() != 1) {
                result.add(vo);
            }
        }
        return result;
    }

    private MenuVO convert(CoreMenu coreMenu) {

        if (ROOTID != coreMenu.getPid() && Strings.CS.startsWith(coreMenu.getPath(), "/")) {
            coreMenu.setPath(coreMenu.getPath().substring(1));
        }
        MenuVO menuVO = new MenuVO();
        BeanUtils.copyBean(menuVO, coreMenu, "children");
        MenuMeta meta = new MenuMeta();
        meta.setTitle(Translator.get(I18N_PREFIX + coreMenu.getName()));
        meta.setIcon(coreMenu.getIcon());
        menuVO.setMeta(meta);

        menuVO.setPlugin(!internalLiteEnabled && isCommercialMenu(coreMenu));
        return menuVO;
    }

    private boolean isCommercialMenu(CoreMenu coreMenu) {
        if (coreMenu.getId().equals(21L)) return false;
        return coreMenu.getId().equals(7L)
                || coreMenu.getPid().equals(7L)
                || coreMenu.getId().equals(14L)
                || coreMenu.getId().equals(17L)
                || coreMenu.getId().equals(18L)
                || coreMenu.getPid().equals(21L)
                || coreMenu.getId().equals(25L)
                || coreMenu.getId().equals(26L)
                || coreMenu.getId().equals(27L)
                || coreMenu.getId().equals(28L)
                || coreMenu.getId().equals(35L)
                || coreMenu.getId().equals(40L)
                || coreMenu.getId().equals(50L)
                || coreMenu.getId().equals(60L)
                || coreMenu.getId().equals(61L)
                || coreMenu.getId().equals(65L)
                || coreMenu.getId().equals(80L)
                || coreMenu.getId().equals(90L)
                || coreMenu.getPid().equals(70L);
    }
}
