package io.crest.menu.bo;

import io.crest.menu.dao.auto.entity.CoreMenu;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class MenuTreeNode extends CoreMenu {

    private List<MenuTreeNode> children = new ArrayList<>();
}
