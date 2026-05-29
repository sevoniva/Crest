package io.crest.chart.manage;

import io.crest.api.chart.request.ThresholdCheckRequest;
import io.crest.api.chart.vo.ThresholdCheckVO;
import io.crest.constant.DeTypeConstants;
import io.crest.exception.DEException;
import io.crest.extensions.datasource.dto.DatasetTableFieldDTO;
import io.crest.extensions.view.dto.ChartViewDTO;
import io.crest.extensions.view.dto.ChartViewFieldDTO;
import io.crest.extensions.view.filter.FilterTreeItem;
import io.crest.extensions.view.filter.FilterTreeObj;
import io.crest.i18n.Translator;
import io.crest.utils.DateUtils;
import io.crest.utils.JsonUtil;
import io.crest.utils.LogUtil;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component("chartViewThresholdManage")
@SuppressWarnings("unchecked")
public class ChartViewThresholdManage {


    @Resource
    private ChartViewManege chartViewManege;


    public String convertThresholdRules(Long chartId, String thresholdRules, String resourceTable) {
        ChartViewDTO details = chartViewManege.getDetails(chartId, resourceTable);
        return convertThresholdRules(details, thresholdRules);
    }

    private String convertThresholdRules(ChartViewDTO chart, String thresholdRules) {
        List<DatasetTableFieldDTO> fieldList = chartFields(chart);
        FilterTreeObj filterTreeObj = JsonUtil.parseObject(thresholdRules, FilterTreeObj.class);
        Map<String, DatasetTableFieldDTO> fieldMap = fieldList.stream()
                .collect(Collectors.toMap(
                        item -> item.getId().toString(),
                        item -> item,
                        (existing, replacement) -> existing
                ));
        return convertTree(filterTreeObj, fieldMap);
    }

    private List<DatasetTableFieldDTO> chartFields(ChartViewDTO details) {
        List<DatasetTableFieldDTO> result = new ArrayList<>();
        List<ChartViewFieldDTO> xAxis = details.getXAxis();
        if (CollectionUtils.isNotEmpty(xAxis)) {
            result.addAll(xAxis);
        }
        List<ChartViewFieldDTO> xAxisExt = details.getXAxisExt();
        if (CollectionUtils.isNotEmpty(xAxisExt)) {
            result.addAll(xAxisExt);
        }
        List<ChartViewFieldDTO> yAxis = details.getYAxis();
        if (CollectionUtils.isNotEmpty(yAxis)) {
            result.addAll(yAxis);
        }
        List<ChartViewFieldDTO> yAxisExt = details.getYAxisExt();
        if (CollectionUtils.isNotEmpty(yAxisExt)) {
            result.addAll(yAxisExt);
        }
        List<ChartViewFieldDTO> extStack = details.getExtStack();
        if (CollectionUtils.isNotEmpty(extStack)) {
            result.addAll(extStack);
        }
        List<ChartViewFieldDTO> extBubble = details.getExtBubble();
        if (CollectionUtils.isNotEmpty(extBubble)) {
            result.addAll(extBubble);
        }
        List<ChartViewFieldDTO> extLabel = details.getExtLabel();
        if (CollectionUtils.isNotEmpty(extLabel)) {
            result.addAll(extLabel);
        }
        List<ChartViewFieldDTO> extTooltip = details.getExtTooltip();
        if (CollectionUtils.isNotEmpty(extTooltip)) {
            result.addAll(extTooltip);
        }
        List<ChartViewFieldDTO> extColor = details.getExtColor();
        if (CollectionUtils.isNotEmpty(extColor)) {
            result.addAll(extColor);
        }
        List<ChartViewFieldDTO> flowMapStartName = details.getFlowMapStartName();
        if (CollectionUtils.isNotEmpty(flowMapStartName)) {
            result.addAll(flowMapStartName);
        }
        List<ChartViewFieldDTO> flowMapEndName = details.getFlowMapEndName();
        if (CollectionUtils.isNotEmpty(flowMapEndName)) {
            result.addAll(flowMapEndName);
        }
        return result;
    }

    private String convertTree(FilterTreeObj filterTreeObj, Map<String, DatasetTableFieldDTO> fieldMap) {
        String logic = filterTreeObj.getLogic();
        String logicText = translateLogic(logic);
        List<FilterTreeItem> items = filterTreeObj.getItems();

        StringBuilder result = new StringBuilder();
        for (FilterTreeItem item : items) {
            String type = item.getType();
            if (Strings.CS.equals("tree", type) && ObjectUtils.isNotEmpty(item.getSubTree())) {
                String childResult = convertTree(item.getSubTree(), fieldMap);
                result.append(childResult);
            } else {
                String itemResult = convertItem(item, fieldMap);
                result.append(itemResult);
            }
            result.append(logicText);
        }
        int lastIndex = -1;
        if ((!result.isEmpty()) && (lastIndex = result.lastIndexOf(logicText)) > 0) {
            return result.substring(0, lastIndex);
        }

        return null;
    }

    private String convertItem(FilterTreeItem item, Map<String, DatasetTableFieldDTO> fieldMap) {
        String filterType = item.getFilterType();
        Long fieldId = item.getFieldId();
        DatasetTableFieldDTO map = fieldMap.get(fieldId.toString());
        String fieldName = map.getName();
        if (Strings.CS.equals(filterType, "enum")) {
            List<String> enumValue = item.getEnumValue();
            String enumValueText = String.join(",", enumValue);
            return fieldName + " " + Translator.get("i18n_threshold_logic_in") + " " + "( " + enumValueText + " )";
        } else {
            Integer deType = map.getDeType();
            String valueType = item.getValueType();
            return fieldName + " " + translateTerm(item.getTerm()) + " " + formatFieldValue(item.getValue(), valueType, deType);
        }
    }

    private String formatFieldValue(String value, String valueType, Integer deType) {
        if (StringUtils.isBlank(valueType)) {
            valueType = "fixed";
        }
        if (Strings.CS.equals("fixed", valueType)) {
            return value;
        }
        if (Strings.CS.equals("max", value)) {
            return Translator.get("i18n_threshold_max");
        } else if (Strings.CS.equals("min", value)) {
            return Translator.get("i18n_threshold_min");
        } else if (Strings.CS.equals("average", value)) {
            return Translator.get("i18n_threshold_average");
        } else if (deType == 1) {
            return formatDynamicTimeLabel(value);
        } else {
            return value;
        }
    }

    private String formatDynamicTimeLabel(String value) {
        if (StringUtils.isBlank(value)) {
            return value;
        }
        try {
            Map map = JsonUtil.parseObject(value, Map.class);
            String format = map.get("format").toString();
            int timeFlag = Integer.parseInt(map.get("timeFlag").toString());

            if (timeFlag == 9) {
                int count = Integer.parseInt(map.get("count").toString());
                int unit = Integer.parseInt(map.get("unit").toString());
                int suffix = Integer.parseInt(map.get("suffix").toString());
                String time = map.get("time").toString();
                if (unit > 3) {
                    time = getCustomTimeValue(format, unit, suffix, count, false);
                }
                List<String> unitLabels = null;
                if (Strings.CI.equals("YYYY", format)) {
                    unitLabels = List.of(Translator.get("i18n_time_year"));
                } else if (Strings.CI.equals("YYYY-MM", format)) {
                    unitLabels = List.of(Translator.get("i18n_time_year"), Translator.get("i18n_time_month"));
                } else if (Strings.CI.equals("YYYY-MM-DD", format)) {
                    unitLabels = List.of(Translator.get("i18n_time_year"), Translator.get("i18n_time_month"), Translator.get("i18n_time_date"));
                } else if (Strings.CI.equals("HH:mm:ss", format)) {
                    DEException.throwException("纯时间格式不支持动态格式");
                } else {
                    unitLabels = List.of(Translator.get("i18n_time_year"), Translator.get("i18n_time_month"), Translator.get("i18n_time_date"), Translator.get("i18n_time_hour"));
                }
                String unitText = unitLabels.get(unit - 1);
                String suffixText = Translator.get("i18n_time_ago");
                if (suffix == 2) {
                    suffixText = Translator.get("i18n_time_later");
                }
                String timeText = "";
                if (Strings.CI.contains(format, "HH")) {
                    timeText = " (" + time + ")";
                }
                return count + " " + unitText + suffixText + timeText;
            } else {
                List<String> shortLabels = null;
                if (Strings.CI.equals("YYYY", format)) {
                    shortLabels = List.of(Translator.get("i18n_time_year_current"), Translator.get("i18n_time_year_last"), Translator.get("i18n_time_year_next"));
                } else if (Strings.CI.equals("YYYY-MM", format)) {
                    shortLabels = List.of(Translator.get("i18n_time_month_current"), Translator.get("i18n_time_month_last"), Translator.get("i18n_time_month_next"),
                            Translator.get("i18n_time_month_start"), Translator.get("i18n_time_month_end"));
                } else if (Strings.CI.equals("YYYY-MM-DD", format)) {
                    shortLabels = List.of(Translator.get("i18n_time_date_current"), Translator.get("i18n_time_date_last"), Translator.get("i18n_time_date_next"),
                            Translator.get("i18n_time_date_start"), Translator.get("i18n_time_date_end"));
                } else if (Strings.CI.equals("HH:mm:ss", format)) {
                    shortLabels = List.of("当前", "1小时前", "1小时后");
                } else {
                    shortLabels = List.of(Translator.get("i18n_time_date_current"), Translator.get("i18n_time_date_last"), Translator.get("i18n_time_date_next"),
                            Translator.get("i18n_time_date_start"), Translator.get("i18n_time_date_end"));
                }
                return shortLabels.get(timeFlag - 1);
            }

        } catch (Exception e) {
            LogUtil.error("动态时间配置错误，请重新配置！");
            return value;
        }
    }

    private String translateTerm(String term) {
        if (Strings.CS.equals(term, "not in")) {
            return Translator.get("i18n_threshold_logic_not_in");
        } else if (Strings.CS.equals(term, "not like")) {
            return Translator.get("i18n_threshold_logic_not_like");
        } else {
            return Translator.get("i18n_threshold_logic_" + term);
        }
    }

    private String translateLogic(String logic) {
        if (Strings.CS.equals(logic, "and")) return String.format(" %s ", Translator.get("i18n_threshold_logic_and"));
        return String.format(" %s ", Translator.get("i18n_threshold_logic_or"));
    }

    private String convertStyle(String htmlString) {
        String regex = "<span\\s+id=\"(changeText-0|changeText-1)\"\\s+style=\"([^\"]*)\">";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(htmlString);
        if (matcher.find()) {
            String styleAttribute = matcher.group();
            String newStyle = styleAttribute.replaceAll("background: #[0-9A-Fa-f]{6}33", "background: #FFFFFF")
                    .replace("color: #2b5fd9", "color: #000000");
            return matcher.replaceAll(Matcher.quoteReplacement(newStyle));
        }
        return htmlString;
    }

    public ThresholdCheckVO checkThreshold(ThresholdCheckRequest request) throws Exception {
        String thresholdTemplate = request.getThresholdTemplate();
        String thresholdRules = request.getThresholdRules();
        Long chartId = request.getChartId();
        try {
            ChartViewDTO chart = chartViewManege.getChart(chartId, request.getResourceTable(), true);
            Map<String, Object> data = null;
            if (ObjectUtils.isEmpty(chart) || MapUtils.isEmpty(data = chart.getData())) {
                return new ThresholdCheckVO(false, null, "查询图表异常！", null);
            }
            thresholdTemplate = thresholdTemplate.replace("[检测时间]", DateUtils.time2String(System.currentTimeMillis()));
            String s = convertThresholdRules(chart, thresholdRules);
            thresholdTemplate = convertStyle(thresholdTemplate.replace("[触发告警]", s));
            List<Map<String, Object>> tableRow = (List<Map<String, Object>>) data.get("tableRow");
            List<DatasetTableFieldDTO> fields = (List<DatasetTableFieldDTO>) data.get("fields");
            if (CollectionUtils.isEmpty(fields)) {
                return new ThresholdCheckVO(false, null, String.format("当前图表类型[%s]暂不支持阈值告警！", chart.getType()), null);
            }
            Map<Long, DatasetTableFieldDTO> fieldMap = fields.stream().collect(Collectors.toMap(DatasetTableFieldDTO::getId, item -> item));
            FilterTreeObj filterTreeObj = JsonUtil.parseObject(thresholdRules, FilterTreeObj.class);
            List<Map<String, Object>> rows = filterRows(tableRow, filterTreeObj, fieldMap);
            if (CollectionUtils.isEmpty(rows)) {
                return new ThresholdCheckVO(false, null, null, null);
            }
            String regex = "<span[^>]*id=\"changeText-(-?\\d+)(?!0$)(?!1$)\"[^>]*>.*?</span>";
            Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
            Matcher matcher = pattern.matcher(thresholdTemplate);
            StringBuilder sb = new StringBuilder();

            boolean withThresholdData = false;
            int thresholdRecordCount = request.getThresholdLimit();
            while (matcher.find()) {
                long id = Long.parseLong(matcher.group(1));
                if (id == 2L) {
                    withThresholdData = true;
                }
                // 根据id从map中获取替换文本
                DatasetTableFieldDTO fieldDTO = fieldMap.get(id);
                if (ObjectUtils.isEmpty(fieldDTO)) continue;
                String dataeaseName = fieldDTO.getDataeaseName();
                String fieldDTOName = fieldDTO.getName();

                if (rows.size() > thresholdRecordCount) {
                    rows = rows.subList(0, thresholdRecordCount);
                }
                if (request.isShowFieldValue()) {
                    String replacement = null;
                    if (fieldDTO.getDeType().equals(DeTypeConstants.DE_FLOAT) || fieldDTO.getDeType().equals(DeTypeConstants.DE_INT)) {
                        List<String> valueList = rows.stream().map(row -> ObjectUtils.isEmpty(row.get(dataeaseName)) ? null : stripTrailingZeros2String(row.get(dataeaseName))).collect(Collectors.toList());
                        replacement = fieldDTOName + ": " + JsonUtil.toJSONString(valueList);
                    } else {
                        List<String> valueList = rows.stream().map(row -> ObjectUtils.isEmpty(row.get(dataeaseName)) ? null : row.get(dataeaseName).toString()).collect(Collectors.toList());
                        replacement = fieldDTOName + ": " + JsonUtil.toJSONString(valueList);
                    }
                    matcher.appendReplacement(sb, replacement);
                } else {
                    matcher.appendReplacement(sb, fieldDTOName);
                }
            }

            matcher.appendTail(sb);
            // 输出替换后的HTML内容
            String result = sb.toString();

            if (withThresholdData) {
                Set<Long> thresholdFieldIdSet = new HashSet<>();
                getThresholdFieldIdList(filterTreeObj, thresholdFieldIdSet);
                List<List<String>> thresholdTableList = rows.stream().map(row -> thresholdFieldIdSet.stream().map(fieldId -> {
                    DatasetTableFieldDTO fieldDTO = fieldMap.get(fieldId);
                    if (ObjectUtils.isEmpty(fieldDTO)) return "";
                    String dataeaseName = fieldDTO.getDataeaseName();
                    Integer deType = fieldDTO.getDeType();
                    String value = null;

                    if (deType.equals(DeTypeConstants.DE_FLOAT) || deType.equals(DeTypeConstants.DE_INT)) {
                        value = ObjectUtils.isEmpty(row.get(dataeaseName)) ? null : stripTrailingZeros2String(row.get(dataeaseName));
                    } else {
                        value = ObjectUtils.isEmpty(row.get(dataeaseName)) ? null : row.get(dataeaseName).toString();
                    }
                    return value;
                }).collect(Collectors.toList())).collect(Collectors.toList());
                List<String> tableHeadList = thresholdFieldIdSet.stream().map(i -> fieldMap.get(i).getName()).collect(Collectors.toList());
                tableHeadList.addFirst("NO");
                thresholdTableList.addFirst(tableHeadList);
                StringBuilder tableHtml = new StringBuilder("<table style=\"min-width:35%;border-collapse:collapse;font-family:'Segoe UI',Arial,sans-serif;font-size:14px;border:1px solid;border-radius:8px;overflow:hidden;border-spacing:0\">");

                for (int i = 0; i < thresholdTableList.size(); i++) {
                    List<String> row = thresholdTableList.get(i);
                    if (i == 0) {
                        StringBuilder theadHtmlBuild = new StringBuilder("<thead><tr style=\"border-bottom:2px double;border-color: inherit;\">");
                        row.forEach(item -> {
                            theadHtmlBuild.append("<th style=\"border: 1px dashed;border-color: inherit;padding:12px;text-align:left;font-weight:bold;letter-spacing:1px;text-transform:uppercase;\">").append(item).append("</th>");
                        });
                        theadHtmlBuild.append("</tr></thead>");
                        tableHtml.append(theadHtmlBuild);
                        continue;
                    }
                    row.addFirst(String.valueOf(i));
                    if (i == 1) {
                        tableHtml.append("<tbody>");
                    }

                    StringBuilder trHtmlBuild = new StringBuilder("<tr style=\"border-bottom:1px dashed;border-color: inherit;\">");
                    row.forEach(item -> {
                        trHtmlBuild.append("<td style=\"border: 1px dashed;border-color: inherit;padding:12px\">").append(item).append("</td>");
                    });
                    trHtmlBuild.append("</tr>");
                    tableHtml.append(trHtmlBuild);

                    if (i == thresholdTableList.size() - 1) {
                        tableHtml.append("</tbody></table>");
                    }

                }

                String thresholdDataRex = "<span id=\"changeText-(\\d+)\"[^>]*?style=\"([^\"]*?)\"[^>]*?>\\s*<span[^>]*?data-mce-content=\"\\[告警数据\\]\"[^>]*?>\\[告警数据\\]</span>\\s*</span>";

                Pattern thresholdDataPattern = Pattern.compile(thresholdDataRex, Pattern.DOTALL);
                Matcher thresholdDataMatcher = thresholdDataPattern.matcher(result);
                if (thresholdDataMatcher.find()) {
                    String originStyle = thresholdDataMatcher.group(2);
                    String tableStyleHtml = tableHtml.toString().replace("min-width:35%;", originStyle + "min-width:35%;");
                    result = thresholdDataMatcher.replaceAll(tableStyleHtml);
                }
            }
            return new ThresholdCheckVO(true, result, null, null);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), new Throwable(e));
            return new ThresholdCheckVO(false, null, e.getMessage(), null);
        }
    }

    private String stripTrailingZeros2String(Object value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        if (!(value instanceof BigDecimal)) return value.toString();
        return ((BigDecimal) value).stripTrailingZeros().toPlainString();
    }

    private void getThresholdFieldIdList(FilterTreeObj conditionTree, Set<Long> fieldIdSet) {
        List<FilterTreeItem> items = conditionTree.getItems();
        items.forEach(item -> {
            if (!Strings.CS.equals("item", item.getType())) {
                getThresholdFieldIdList(item.getSubTree(), fieldIdSet);
            } else {
                Long fieldId = item.getFieldId();
                fieldIdSet.add(fieldId);
            }
        });
    }

    private void chartDynamicMap(List<Map<String, Object>> rows, FilterTreeObj conditionTree, Map<Long, DatasetTableFieldDTO> fieldMap) {
        List<FilterTreeItem> items = conditionTree.getItems();
        items.forEach(item -> {
            if (!Strings.CS.equals("item", item.getType())) {
                chartDynamicMap(rows, item.getSubTree(), fieldMap);
            } else {
                Long fieldId = item.getFieldId();
                DatasetTableFieldDTO fieldDTO = fieldMap.get(fieldId);
                if ((Objects.equals(fieldDTO.getDeType(), DeTypeConstants.DE_INT) || Objects.equals(fieldDTO.getDeType(), DeTypeConstants.DE_FLOAT)) && Strings.CS.equals("dynamic", item.getValueType())) {
                    item.setField(fieldDTO);
                    item.setValue(formatValue(rows, item));
                } else if (Objects.equals(fieldDTO.getDeType(), DeTypeConstants.DE_TIME) && Strings.CS.equals("dynamic", item.getValueType())) {
                    item.setField(fieldDTO);
                    item.setValue(dynamicFormatValue(item));
                }
            }
        });
    }

    private String dynamicFormatValue(FilterTreeItem item) {
        String value = item.getValue();

        if (StringUtils.isBlank(value)) {
            return value;
        }
        try {
            Map map = JsonUtil.parseObject(value, Map.class);
            String format = map.get("format").toString();
            int timeFlag = Integer.parseInt(map.get("timeFlag").toString());
            if (timeFlag == 9) {
                int count = Integer.parseInt(map.get("count").toString());
                int unit = Integer.parseInt(map.get("unit").toString());
                int suffix = Integer.parseInt(map.get("suffix").toString());
                String time = map.get("time").toString();
                String timeValue = getCustomTimeValue(format, unit, suffix, count, false);
                if (unit < 4 && Strings.CI.contains(format, "yyyy-MM-dd HH") && StringUtils.isNotBlank(time)) {
                    return timeValue + " " + time;
                }
                return timeValue;
            } else {
                LocalDateTime now = LocalDateTime.now();
                String fullFormat = "yyyy-MM-dd HH:mm:ss";
                int length = format.length();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(fullFormat.substring(0, length));
                int count = timeFlag == 1 ? 0 : 1;
                int suffix = timeFlag - 1;
                if (Strings.CI.equals("YYYY", format)) {
                    return getCustomTimeValue(format, 1, suffix, count, true);
                } else if (Strings.CI.equals("YYYY-MM", format)) {
                    if (timeFlag == 4) {
                        return now.withMonth(1).withDayOfMonth(1).format(formatter);
                    } else if (timeFlag == 5) {
                        return now.withMonth(12).withDayOfMonth(31).format(formatter);
                    } else {
                        return getCustomTimeValue(format, 2, suffix, count, true);
                    }
                } else {
                    if (timeFlag == 4) {
                        return now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).format(formatter);
                    } else if (timeFlag == 5) {
                        return now.plusMonths(1).withDayOfMonth(1).minusDays(1).withHour(0).withMinute(0).withSecond(0).format(formatter);
                    } else {
                        return getCustomTimeValue(format, 3, suffix, count, true);
                    }
                }
            }

        } catch (Exception e) {
            LogUtil.error("动态时间配置错误，请重新配置！" + e.getMessage());
            return value;
        }
    }

    private String getCustomTimeValue(String format, int unit, int suffix, int count, boolean hasTime) {
        LocalDateTime now = LocalDateTime.now();
        String fullFormat = "yyyy-MM-dd HH:mm:ss";
        int len = format.length();
        if (hasTime) {
            now = now.withHour(0).withMinute(0).withSecond(0);
        } else {
            len = unit > 3 ? len : Math.min(len, 10);
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(fullFormat.substring(0, len));
        if (count == 0) {
            return now.format(formatter);
        }
        if (unit == 1) {
            if (suffix == 1) {
                return now.minusYears(count).format(formatter);
            }
            return now.plusYears(count).format(formatter);
        } else if (unit == 2) {
            if (suffix == 1) {
                return now.minusMonths(count).format(formatter);
            }
            return now.plusMonths(count).format(formatter);
        } else if (unit == 3) {
            if (suffix == 1) {
                return now.minusDays(count).format(formatter);
            }
            return now.plusDays(count).format(formatter);
        } else {
            if (suffix == 1) {
                return now.minusHours(count).format(formatter);
            }
            return now.plusHours(count).format(formatter);
        }
    }


    private String formatValue(List<Map<String, Object>> rows, FilterTreeItem item) {
        DatasetTableFieldDTO field = item.getField();
        String dataeaseName = field.getDataeaseName();
        String value = item.getValue();
        Float tempFVal = Strings.CS.equalsAny(value, "min", "max") ? null : 0f;
        int validLen = 0;

        for (Map<String, Object> row : rows) {
            Object o = row.get(dataeaseName);
            if (ObjectUtils.isEmpty(o)) continue;
            float fvalue = Float.parseFloat(o.toString());
            if (Strings.CS.equals("min", value)) {
                if (ObjectUtils.isEmpty(tempFVal)) {
                    tempFVal = fvalue;
                } else {
                    tempFVal = Math.min(tempFVal, fvalue);
                }
            } else if (Strings.CS.equals("max", value)) {
                if (ObjectUtils.isEmpty(tempFVal)) {
                    tempFVal = fvalue;
                } else {
                    tempFVal = Math.max(tempFVal, fvalue);
                }
            } else if (Strings.CS.equals("average", value)) {
                tempFVal += fvalue;
                validLen++;
            }
        }
        if (Strings.CS.equals("average", value)) {
            return validLen == 0 ? "0f" : String.valueOf((tempFVal / validLen));
        }
        return String.valueOf(tempFVal);
    }

    public List<Map<String, Object>> filterRows(List<Map<String, Object>> rows, FilterTreeObj conditionTree, Map<Long, DatasetTableFieldDTO> fieldMap) {
        chartDynamicMap(rows, conditionTree, fieldMap);
        List<Map<String, Object>> filteredRows = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            if (matchesConditionTree(row, conditionTree, fieldMap)) {
                filteredRows.add(row);
            }
        }
        return filteredRows;
    }

    private boolean matchesConditionTree(Map<String, Object> row, FilterTreeObj conditionTree, Map<Long, DatasetTableFieldDTO> fieldMap) {
        if (conditionTree == null || conditionTree.getItems().isEmpty()) {
            return true; // 如果没有条件树或条件列表为空，返回所有行
        }
        List<FilterTreeItem> items = conditionTree.getItems();
        if (conditionTree.getLogic().equals("or")) {
            return matchesAnyItem(row, items, fieldMap);
        }
        return matchesAllItems(row, items, fieldMap);
    }

    private boolean matchesAllItems(Map<String, Object> row, List<FilterTreeItem> items, Map<Long, DatasetTableFieldDTO> fieldMap) {
        for (FilterTreeItem item : items) {
            if (!matchesConditionItem(row, item, fieldMap)) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesAnyItem(Map<String, Object> row, List<FilterTreeItem> items, Map<Long, DatasetTableFieldDTO> fieldMap) {
        for (FilterTreeItem item : items) {
            if (matchesConditionItem(row, item, fieldMap)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesConditionItem(Map<String, Object> row, FilterTreeItem item, Map<Long, DatasetTableFieldDTO> fieldMap) {
        if ("item".equals(item.getType())) {
            DatasetTableFieldDTO fieldDTO = fieldMap.get(item.getFieldId());
            return rowMatch(row, item, fieldDTO);
        } else if ("tree".equals(item.getType()) && item.getSubTree() != null) {
            return matchesConditionTree(row, item.getSubTree(), fieldMap);
        }
        return false; // 如果类型不匹配或没有子树，不匹配
    }

    private boolean rowMatch(Map<String, Object> row, FilterTreeItem item, DatasetTableFieldDTO fieldDTO) {
        String dataeaseName = fieldDTO.getDataeaseName();
        String filterType = item.getFilterType();
        Integer deType = fieldDTO.getDeType();
        Object valueObj = row.get(dataeaseName);
        if (Strings.CS.equals(filterType, "enum")) {
            List<String> enumValue = item.getEnumValue();
            return ObjectUtils.isNotEmpty(valueObj) && enumValue.contains(valueObj);
        } else {
            String term = item.getTerm();
            if (Objects.equals(deType, DeTypeConstants.DE_STRING)) {
                if (valueObj == null) {
                    return Strings.CS.equals(term, "null");
                }
                if (Strings.CS.equals(term, "eq")) {
                    return Strings.CS.equals(item.getValue(), valueObj.toString());
                } else if (Strings.CS.equals(term, "not_eq")) {
                    return !Strings.CS.equals(item.getValue(), valueObj.toString());
                } else if (Strings.CS.equals(term, "in")) {
                    return Arrays.stream(item.getValue().split(",")).toList().contains(valueObj.toString());
                } else if (Strings.CS.equals(term, "not_in")) {
                    return !Arrays.stream(item.getValue().split(",")).toList().contains(valueObj.toString());
                } else if (Strings.CS.equals(term, "like")) {
                    return Strings.CS.contains(item.getValue(), valueObj.toString());
                } else if (Strings.CS.equals(term, "not_like")) {
                    return !Strings.CS.contains(item.getValue(), valueObj.toString());
                } else if (Strings.CS.equals(term, "null")) {
                    return false;
                } else if (Strings.CS.equals(term, "not_null")) {
                    return true;
                } else if (Strings.CS.equals(term, "empty")) {
                    return StringUtils.isBlank(valueObj.toString());
                } else if (Strings.CS.equals(term, "not_empty")) {
                    return !StringUtils.isBlank(valueObj.toString());
                } else {
                    return Strings.CS.equals(item.getValue(), valueObj.toString());
                }
            } else if (Objects.equals(deType, DeTypeConstants.DE_INT) || Objects.equals(deType, DeTypeConstants.DE_FLOAT)) {
                if (valueObj == null) return false;
                if (ObjectUtils.isEmpty(item.getValue())) {
                    return false;
                }
                float targetVal = Float.parseFloat(item.getValue());
                float originVal = Float.parseFloat(valueObj.toString());
                if (Strings.CS.equals(term, "eq")) {
                    return Strings.CS.equals(String.valueOf(originVal), String.valueOf(targetVal));
                } else if (Strings.CS.equals(term, "not_eq")) {
                    return !Strings.CS.equals(String.valueOf(originVal), String.valueOf(targetVal));
                } else if (Strings.CS.equals(term, "gt")) {
                    return targetVal < originVal;
                } else if (Strings.CS.equals(term, "ge")) {
                    return targetVal <= originVal;
                } else if (Strings.CS.equals(term, "lt")) {
                    return targetVal > originVal;
                } else if (Strings.CS.equals(term, "le")) {
                    return targetVal >= originVal;
                } else {
                    return Strings.CS.equals(item.getValue(), valueObj.toString());
                }
            } else if (Objects.equals(deType, DeTypeConstants.DE_TIME)) {
                // 补充时间逻辑
                return timeMatch(item, valueObj);
            } else {
                return true;
            }
        }
    }

    private boolean timeMatch(FilterTreeItem item, Object valueObj) {
        if (ObjectUtils.isEmpty(valueObj)) return false;
        String valueText = valueObj.toString();
        String target = item.getValue();
        target = target.replaceAll("[^0-9]", "");
        valueText = valueText.replaceAll("[^0-9]", "");
        long targetLong = Long.parseLong(target);
        long valueLong = Long.parseLong(valueText);
        String term = item.getTerm();
        if (Strings.CS.equals(term, "eq")) {
            return valueLong == targetLong;
        } else if (Strings.CS.equals(term, "not_eq")) {
            return valueLong != targetLong;
        } else if (Strings.CS.equals(term, "gt")) {
            return valueLong > targetLong;
        } else if (Strings.CS.equals(term, "ge")) {
            return valueLong >= targetLong;
        } else if (Strings.CS.equals(term, "lt")) {
            return valueLong < targetLong;
        } else if (Strings.CS.equals(term, "le")) {
            return valueLong <= targetLong;
        } else {
            return valueLong == targetLong;
        }
    }

}
