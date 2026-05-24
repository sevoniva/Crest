package io.crest.commons.utils;

import io.crest.constant.SortConstants;
import io.crest.visualization.dto.VisualizationNodeBO;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author : WangJiaHao
 * @date : 2024/3/18 10:53
 */
public class CoreTreeUtils {

    public static List<VisualizationNodeBO> customSortBO(List<VisualizationNodeBO> list, String sortType) {
        Collator collator = Collator.getInstance(Locale.CHINA);
        if (Strings.CI.equals(SortConstants.NAME_DESC, sortType)) {
            Set<VisualizationNodeBO> poSet = new TreeSet<>(Comparator.comparing(VisualizationNodeBO::getName, collator));
            poSet.addAll(list);
            return poSet.stream().collect(Collectors.toList());
        } else if (Strings.CI.equals(SortConstants.NAME_ASC, sortType)) {
            Set<VisualizationNodeBO> poSet = new TreeSet<>(Comparator.comparing(VisualizationNodeBO::getName, collator).reversed());
            poSet.addAll(list);
            return poSet.stream().collect(Collectors.toList());
        } else if (Strings.CI.equals(SortConstants.TIME_ASC, sortType)) {
            Collections.reverse(list);
            return list;
        } else {
            // 默认时间倒序
            return list;
        }
    }
}
