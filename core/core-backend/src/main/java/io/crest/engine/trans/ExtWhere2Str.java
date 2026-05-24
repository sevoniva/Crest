package io.crest.engine.trans;

import io.crest.constant.SQLConstants;
import io.crest.engine.utils.Utils;
import io.crest.exception.DEException;
import io.crest.extensions.datasource.api.PluginManageApi;
import io.crest.extensions.datasource.constant.SqlPlaceholderConstants;
import io.crest.extensions.datasource.dto.CalParam;
import io.crest.extensions.datasource.dto.DatasetTableFieldDTO;
import io.crest.extensions.datasource.dto.DatasourceSchemaDTO;
import io.crest.extensions.datasource.model.SQLMeta;
import io.crest.extensions.datasource.model.SQLObj;
import io.crest.extensions.datasource.vo.DatasourceConfiguration;
import io.crest.extensions.view.dto.ChartExtFilterDTO;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Author Junjun
 */
public class ExtWhere2Str {
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^[-+]?\\d+(\\.\\d+)?([eE][-+]?\\d+)?$");

    public static void extWhere2sqlOjb(SQLMeta meta, List<ChartExtFilterDTO> fields, List<DatasetTableFieldDTO> originFields, boolean isCross, Map<Long, DatasourceSchemaDTO> dsMap, List<CalParam> fieldParam, List<CalParam> chartParam, PluginManageApi pluginManage) {
        SQLObj tableObj = meta.getTable();
        if (ObjectUtils.isEmpty(tableObj)) {
            return;
        }
        Map<String, String> paramMap = Utils.mergeParam(fieldParam, chartParam);
        List<SQLObj> list = new ArrayList<>();
        Map<String, String> fieldsDialect = new HashMap<>();

        String dsType = null;
        if (dsMap != null && dsMap.entrySet().iterator().hasNext()) {
            Map.Entry<Long, DatasourceSchemaDTO> next = dsMap.entrySet().iterator().next();
            dsType = next.getValue().getType();
        }

        if (ObjectUtils.isNotEmpty(fields)) {
            for (ChartExtFilterDTO request : fields) {
                List<String> value = request.getValue();

                List<String> whereNameList = new ArrayList<>();
                List<DatasetTableFieldDTO> fieldList = new ArrayList<>();
                if (request.getIsTree()) {
                    fieldList.addAll(request.getDatasetTableFieldList());
                } else {
                    fieldList.add(request.getDatasetTableField());
                }

                for (DatasetTableFieldDTO field : fieldList) {
                    if (ObjectUtils.isEmpty(value) || ObjectUtils.isEmpty(field)) {
                        continue;
                    }
                    String whereName = "";

                    String originName;
                    if (ObjectUtils.isNotEmpty(field.getExtField()) && field.getExtField() == 2) {
                        // 解析origin name中有关联的字段生成sql表达式
                        String calcFieldExp = Utils.calcFieldRegex(field, tableObj, originFields, isCross, dsMap, paramMap, pluginManage);
                        // 给计算字段处加一个占位符，后续SQL方言转换后再替换
                        originName = String.format(SqlPlaceholderConstants.CALC_FIELD_PLACEHOLDER, field.getId());
                        fieldsDialect.put(originName, calcFieldExp);
                        if (isCross) {
                            originName = calcFieldExp;
                        }
                    } else if (ObjectUtils.isNotEmpty(field.getExtField()) && field.getExtField() == 1) {
                        if (Strings.CI.equals(dsType, "es")) {
                            originName = String.format(SQLConstants.FIELD_NAME, tableObj.getTableAlias(), field.getOriginName());
                        } else {
                            originName = String.format(SQLConstants.FIELD_NAME, tableObj.getTableAlias(), field.getDataeaseName());
                        }
                    } else if (ObjectUtils.isNotEmpty(field.getExtField()) && field.getExtField() == 3) {
                        String groupFieldExp = Utils.transGroupFieldToSql(field, originFields, isCross, dsMap, pluginManage);
                        // 给计算字段处加一个占位符，后续SQL方言转换后再替换
                        originName = String.format(SqlPlaceholderConstants.CALC_FIELD_PLACEHOLDER, field.getId());
                        fieldsDialect.put(originName, groupFieldExp);
                        if (isCross) {
                            originName = groupFieldExp;
                        }
                    } else {
                        if (Strings.CI.equals(dsType, "es")) {
                            originName = String.format(SQLConstants.FIELD_NAME, tableObj.getTableAlias(), field.getOriginName());
                        } else {
                            originName = String.format(SQLConstants.FIELD_NAME, tableObj.getTableAlias(), field.getDataeaseName());
                        }
                    }

                    if (field.getDeType() == 1) {
                        if (field.getDeExtractType() == 0 || field.getDeExtractType() == 5) {
                            // 此处获取标准格式的日期
                            whereName = String.format(SQLConstants.DE_STR_TO_DATE, originName, StringUtils.isEmpty(field.getDateFormat()) ? SQLConstants.DEFAULT_DATE_FORMAT : field.getDateFormat());
                        }
                        if (field.getDeExtractType() == 2 || field.getDeExtractType() == 3 || field.getDeExtractType() == 4) {
                            String cast = String.format(SQLConstants.CAST, originName, SQLConstants.DEFAULT_INT_FORMAT);
                            // 此处获取标准格式的日期
                            whereName = String.format(SQLConstants.FROM_UNIXTIME, cast, SQLConstants.DEFAULT_DATE_FORMAT);
                            if (isCross) {
                                whereName = String.format(SQLConstants.UNIX_TIMESTAMP, whereName);
                            }
                        }
                        if (field.getDeExtractType() == 1) {
                            // 如果都是时间类型，把date和time类型进行字符串拼接
                            if (isCross) {
                                if (Strings.CI.equals(field.getType(), "date")) {
                                    originName = String.format(SQLConstants.DE_STR_TO_DATE, String.format(SQLConstants.CONCAT, originName, "' 00:00:00'"), SQLConstants.DEFAULT_DATE_FORMAT);
                                } else if (Strings.CI.equals(field.getType(), "time")) {
                                    originName = String.format(SQLConstants.DE_STR_TO_DATE, String.format(SQLConstants.CONCAT, "'1970-01-01 '", originName), SQLConstants.DEFAULT_DATE_FORMAT);
                                }
                            }
                            // 此处获取标准格式的日期，同时此处是仪表板过滤，仪表板中图表的日期均已经格式化，所以要强制加上日期转换
                            whereName = String.format(SQLConstants.DE_CAST_DATE_FORMAT, originName,
                                    SQLConstants.DEFAULT_DATE_FORMAT,
                                    SQLConstants.DEFAULT_DATE_FORMAT);
                        }
                    } else if (field.getDeType() == 2 || field.getDeType() == 3) {
                        if (field.getDeExtractType() == 0 || field.getDeExtractType() == 5) {
                            whereName = String.format(SQLConstants.CAST, originName, SQLConstants.DEFAULT_FLOAT_FORMAT);
                        }
                        if (field.getDeExtractType() == 1) {
                            whereName = String.format(SQLConstants.UNIX_TIMESTAMP, originName);
                        }
                        if (field.getDeExtractType() == 2 || field.getDeExtractType() == 4) {
                            whereName = String.format(SQLConstants.CAST, originName, SQLConstants.DEFAULT_INT_FORMAT);
                        }
                        if (field.getDeExtractType() == 3) {
                            whereName = String.format(SQLConstants.CAST, originName, SQLConstants.DEFAULT_FLOAT_FORMAT);
                        }
                    } else {
                        whereName = originName;
                    }
                    whereNameList.add(whereName);
                }

                String whereName = "";
                if (request.getIsTree()) {
                    if (Strings.CI.equals(dsType, DatasourceConfiguration.DatasourceType.sqlServer.getType()) && whereNameList.size() == 1) {
                        whereName = whereNameList.get(0);
                    } else {
                        whereName = "CONCAT(" + StringUtils.join(whereNameList, ",',',") + ")";
                    }
                } else {
                    whereName = whereNameList.get(0);
                }
                String whereTerm = Utils.transFilterTerm(request.getOperator());
                String whereValue = "";

                if (Strings.CI.contains(request.getOperator(), "-")) {
                    String[] split = request.getOperator().split("-");
                    String term1 = split[0];
                    String logic = split[1];
                    String term2 = split[2];
                    whereValue = Utils.transFilterTerm(term1) + getValue(term1, value.get(0)) + " " + logic + " " + whereName + Utils.transFilterTerm(term2) + getValue(term2, value.get(1));
                } else if (Strings.CI.contains(request.getOperator(), "in")) {
                    // 过滤空数据
                    if (value.contains(SQLConstants.EMPTY_SIGN)) {
                        String joined = value.stream().map(ExtWhere2Str::sanitizeSqlLiteral).collect(Collectors.joining("','"));
                        whereValue = "('" + joined + "', '')" + " or " + whereName + " is null ";
                    } else {
                        // tree的情况需额外处理
                        if (request.getIsTree()) {
                            List<DatasetTableFieldDTO> datasetTableFieldList = request.getDatasetTableFieldList();
                            boolean hasN = false;
                            for (DatasetTableFieldDTO dto : datasetTableFieldList) {
                                if (Strings.CI.contains(dto.getType(), "NVARCHAR")
                                        || Strings.CI.contains(dto.getType(), "NCHAR")) {
                                    hasN = true;
                                    break;
                                }
                            }
                            if (hasN && !isCross && Strings.CI.equals(dsType, DatasourceConfiguration.DatasourceType.sqlServer.getType())) {
                                whereValue = "(" + value.stream().map(ExtWhere2Str::toSqlServerNQuotedValue).collect(Collectors.joining(",")) + ")";
                            } else {
                                whereValue = "(" + value.stream().map(ExtWhere2Str::toQuotedValue).collect(Collectors.joining(",")) + ")";
                            }
                        } else {
                            if ((Strings.CI.contains(request.getDatasetTableField().getType(), "NVARCHAR")
                                    || Strings.CI.contains(request.getDatasetTableField().getType(), "NCHAR"))
                                    && !isCross
                                    && Strings.CI.equals(dsType, DatasourceConfiguration.DatasourceType.sqlServer.getType())) {
                                whereValue = "(" + value.stream().map(ExtWhere2Str::toSqlServerNQuotedValue).collect(Collectors.joining(",")) + ")";
                            } else {
                                if (request.getDatasetTableField().getDeType() == 2 || request.getDatasetTableField().getDeType() == 3) {
                                    whereValue = "(" + value.stream().map(ExtWhere2Str::sanitizeNumberLiteral).collect(Collectors.joining(",")) + ")";
                                } else {
                                    whereValue = "(" + value.stream().map(ExtWhere2Str::toQuotedValue).collect(Collectors.joining(",")) + ")";
                                }
                            }
                        }
                    }
                } else if (Strings.CI.contains(request.getOperator(), "like")) {
                    // tree的情况需额外处理
                    if (request.getIsTree()) {
                        List<DatasetTableFieldDTO> datasetTableFieldList = request.getDatasetTableFieldList();
                        boolean hasN = false;
                        for (DatasetTableFieldDTO dto : datasetTableFieldList) {
                            if (Strings.CI.contains(dto.getType(), "NVARCHAR")
                                    || Strings.CI.contains(dto.getType(), "NCHAR")) {
                                hasN = true;
                                break;
                            }
                        }
                        if (hasN && !isCross && Strings.CI.equals(dsType, DatasourceConfiguration.DatasourceType.sqlServer.getType())) {
                            whereValue = toSqlServerNLikeValue(value.get(0));
                        } else {
                            whereValue = toLikeValue(value.get(0));
                        }
                    } else {
                        if ((Strings.CI.contains(request.getDatasetTableField().getType(), "NVARCHAR")
                                || Strings.CI.contains(request.getDatasetTableField().getType(), "NCHAR"))
                                && !isCross
                                && Strings.CI.equals(dsType, DatasourceConfiguration.DatasourceType.sqlServer.getType())) {
                            whereValue = toSqlServerNLikeValue(value.get(0));
                        } else {
                            whereValue = toLikeValue(value.get(0));
                        }
                    }
                } else if (Strings.CI.contains(request.getOperator(), "between")) {
                    if (request.getDatasetTableField().getDeType() == 1) {
                        if (request.getDatasetTableField().getDeExtractType() == 2
                                || request.getDatasetTableField().getDeExtractType() == 3
                                || request.getDatasetTableField().getDeExtractType() == 4) {
                            if (isCross) {
                                whereValue = String.format(SQLConstants.WHERE_VALUE_BETWEEN, sanitizeNumberLiteral(value.get(0)), sanitizeNumberLiteral(value.get(1)));
                            } else {
                                whereValue = String.format(SQLConstants.WHERE_BETWEEN, Utils.transLong2Str(Long.parseLong(value.get(0))), Utils.transLong2Str(Long.parseLong(value.get(1))));
                            }
                        } else {
                            if (isCross) {
                                whereName = String.format(SQLConstants.UNIX_TIMESTAMP, whereName);
                                whereValue = String.format(SQLConstants.WHERE_BETWEEN, Long.parseLong(value.get(0)), Long.parseLong(value.get(1)));
                            } else {
                                if (Strings.CI.equals(request.getDatasetTableField().getType(), "date")) {
                                    whereValue = String.format(SQLConstants.WHERE_BETWEEN, Utils.transLong2StrShort(Long.parseLong(value.get(0))), Utils.transLong2StrShort(Long.parseLong(value.get(1))) + " 23:59:59");
                                } else {
                                    whereValue = String.format(SQLConstants.WHERE_BETWEEN, Utils.transLong2Str(Long.parseLong(value.get(0))), Utils.transLong2Str(Long.parseLong(value.get(1))));
                                }
                            }
                        }
                    } else if (request.getDatasetTableField().getDeType() == 2
                            || request.getDatasetTableField().getDeType() == 3
                            || request.getDatasetTableField().getDeType() == 4) {
                        whereValue = String.format(SQLConstants.WHERE_VALUE_BETWEEN, sanitizeNumberLiteral(value.get(0)), sanitizeNumberLiteral(value.get(1)));
                    } else {
                        whereValue = String.format(SQLConstants.WHERE_BETWEEN, sanitizeSqlLiteral(value.get(0)), sanitizeSqlLiteral(value.get(1)));
                    }
                } else {
                    // 过滤空数据
                    if (Strings.CS.equals(value.get(0), SQLConstants.EMPTY_SIGN)) {
                        whereValue = String.format(SQLConstants.WHERE_VALUE_VALUE, "") + " or " + whereName + " is null ";
                    } else {
                        // tree的情况需额外处理
                        if (request.getIsTree()) {
                            List<DatasetTableFieldDTO> datasetTableFieldList = request.getDatasetTableFieldList();
                            boolean hasN = false;
                            for (DatasetTableFieldDTO dto : datasetTableFieldList) {
                                if ((Strings.CI.contains(dto.getType(), "NVARCHAR")
                                        || Strings.CI.contains(dto.getType(), "NCHAR"))
                                        && Strings.CI.equals(dsType, DatasourceConfiguration.DatasourceType.sqlServer.getType())) {
                                    hasN = true;
                                    break;
                                }
                            }
                            if (hasN && !isCross) {
                                whereValue = String.format(SQLConstants.WHERE_VALUE_VALUE_CH, sanitizeSqlLiteral(value.get(0)));
                            } else {
                                whereValue = String.format(SQLConstants.WHERE_VALUE_VALUE, sanitizeSqlLiteral(value.get(0)));
                            }
                        } else {
                            if ((Strings.CI.contains(request.getDatasetTableField().getType(), "NVARCHAR")
                                    || Strings.CI.contains(request.getDatasetTableField().getType(), "NCHAR"))
                                    && !isCross
                                    && Strings.CI.equals(dsType, DatasourceConfiguration.DatasourceType.sqlServer.getType())) {
                                whereValue = String.format(SQLConstants.WHERE_VALUE_VALUE_CH, sanitizeSqlLiteral(value.get(0)));
                            } else {
                                if (request.getDatasetTableField().getDeType() == 2
                                        || request.getDatasetTableField().getDeType() == 3
                                        || request.getDatasetTableField().getDeType() == 4) {
                                    whereValue = String.format(SQLConstants.WHERE_NUMBER_VALUE, sanitizeNumberLiteral(value.get(0)));
                                } else {
                                    whereValue = String.format(SQLConstants.WHERE_VALUE_VALUE, sanitizeSqlLiteral(value.get(0)));
                                }
                            }
                        }
                    }
                }
                list.add(SQLObj.builder()
                        .whereField(whereName)
                        .whereTermAndValue(whereTerm + whereValue)
                        .build());
            }
            List<String> strList = new ArrayList<>();
            list.forEach(ele -> strList.add("(" + ele.getWhereField() + " " + ele.getWhereTermAndValue() + ")"));
            meta.setExtWheres(ObjectUtils.isNotEmpty(list) ? "(" + String.join(" AND ", strList) + ")" : null);
        }
        meta.setExtWheresDialect(fieldsDialect);
    }

    private static String getValue(String term, String value) {
        switch (term) {
            case "like":
                return toLikeValue(value);
            case "eq":
                return toQuotedValue(value);
        }
        return null;
    }

    private static String sanitizeSqlLiteral(String value) {
        String normalized = StringUtils.defaultString(value);
        Utils.validateSqlInjectionRisk(normalized);
        return Utils.transValue(normalized);
    }

    private static String toQuotedValue(String value) {
        return "'" + sanitizeSqlLiteral(value) + "'";
    }

    private static String toLikeValue(String value) {
        return "'%" + sanitizeSqlLiteral(value) + "%'";
    }

    private static String toSqlServerNQuotedValue(String value) {
        return "'" + SQLConstants.MSSQL_N_PREFIX + sanitizeSqlLiteral(value) + "'";
    }

    private static String toSqlServerNLikeValue(String value) {
        return "'" + SQLConstants.MSSQL_N_PREFIX + "%" + sanitizeSqlLiteral(value) + "%'";
    }

    private static String sanitizeNumberLiteral(String value) {
        String normalized = StringUtils.trimToEmpty(value);
        if (!NUMBER_PATTERN.matcher(normalized).matches()) {
            DEException.throwException("Illegal number filter value");
        }
        return normalized;
    }

}
