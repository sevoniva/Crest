package io.crest.datasource.provider;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcraft.jsch.*;
import io.crest.constant.SQLConstants;
import io.crest.dataset.utils.FieldUtils;
import io.crest.datasource.dao.auto.entity.CoreDatasource;
import io.crest.datasource.dao.auto.entity.CoreDriver;
import io.crest.datasource.dao.auto.mapper.CoreDatasourceMapper;
import io.crest.datasource.manage.EngineManage;
import io.crest.datasource.request.EngineRequest;
import io.crest.datasource.type.*;
import io.crest.exception.DEException;
import io.crest.extensions.datasource.dto.*;
import io.crest.extensions.datasource.provider.DriverShim;
import io.crest.extensions.datasource.provider.ExtendedJdbcClassLoader;
import io.crest.extensions.datasource.provider.Provider;
import io.crest.extensions.datasource.vo.DatasourceConfiguration;
import io.crest.i18n.Translator;
import io.crest.utils.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.config.CalciteConnectionProperty;
import org.apache.calcite.config.Lex;
import org.apache.calcite.config.NullCollation;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Component("calciteProvider")
@SuppressWarnings({"deprecation", "unchecked"})
public class CalciteProvider extends Provider {

    @Resource
    protected CoreDatasourceMapper coreDatasourceMapper;
    @Resource
    private EngineManage engineManage;
    protected ExtendedJdbcClassLoader extendedJdbcClassLoader;
    private Map<Long, ExtendedJdbcClassLoader> customJdbcClassLoaders = new HashMap<>();
    @Value("${crest.path.driver:/opt/crest/drivers}")
    private String FILE_PATH;
    @Value("${crest.path.custom-drivers:/opt/crest/custom-drivers/}")
    private String CUSTOM_PATH;
    private static String split = "DE";

    @Resource
    private CommonThreadPool commonThreadPool;

    private boolean isOracleLike(String type) {
        return Strings.CI.equalsAny(type,
                DatasourceConfiguration.DatasourceType.oracle.getType(),
                DatasourceConfiguration.DatasourceType.obOracle.getType());
    }

    private boolean isObOracle(String type) {
        return Strings.CI.equals(type, DatasourceConfiguration.DatasourceType.obOracle.getType());
    }

    boolean shouldUseReadOnlyConnection(
            DatasourceRequest datasourceRequest,
            DatasourceConfiguration datasourceConfiguration,
            String datasourceType
    ) {
        if (Boolean.TRUE.equals(datasourceRequest.getReadOnly())) {
            return true;
        }
        return isObOracle(datasourceType)
                && datasourceConfiguration instanceof ObOracle obOracle
                && !Boolean.FALSE.equals(obOracle.getReadOnly());
    }

    private void setConnectionReadOnly(Connection connection, boolean readOnly) {
        if (!readOnly || connection == null) {
            return;
        }
        try {
            connection.setReadOnly(true);
        } catch (SQLException e) {
            DEException.throwException("源库只读连接启用失败：" + e.getMessage());
        }
    }

    private void resetConnectionReadOnly(Connection connection, boolean readOnly) {
        if (!readOnly || connection == null) {
            return;
        }
        try {
            connection.setReadOnly(false);
        } catch (SQLException e) {
            LogUtil.warn("源库只读连接恢复失败：" + e.getMessage());
        }
    }

    private DatasourceConfiguration parseOracleLikeConfiguration(String configuration, String type) {
        if (isObOracle(type)) {
            return JsonUtil.parseObject(configuration, ObOracle.class);
        }
        return JsonUtil.parseObject(configuration, Oracle.class);
    }

    private DatasourceConfiguration parseDatasourceConfiguration(String configuration, String type) {
        if (isOracleLike(type)) {
            return parseOracleLikeConfiguration(configuration, type);
        }
        return JsonUtil.parseObject(configuration, DatasourceConfiguration.class);
    }

    @PostConstruct
    public void init() throws Exception {
        try {
            String jarPath = FILE_PATH;
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            extendedJdbcClassLoader = new ExtendedJdbcClassLoader(new URL[]{new File(jarPath).toURI().toURL()}, classLoader);
            File file = new File(jarPath);
            File[] array = file.listFiles();
            Optional.ofNullable(array).ifPresent(files -> {
                for (File tmp : array) {
                    if (tmp.getName().endsWith(".jar")) {
                        try {
                            extendedJdbcClassLoader.addFile(tmp);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (Exception e) {

        }
    }

    @Override
    public List<String> getSchema(DatasourceRequest datasourceRequest) {
        List<String> schemas = new ArrayList<>();
        String queryStr = getSchemaSql(datasourceRequest.getDatasource());
        try (ConnectionObj con = getConnection(datasourceRequest.getDatasource())) {
            DatasourceConfiguration datasourceConfiguration = parseDatasourceConfiguration(
                    datasourceRequest.getDatasource().getConfiguration(),
                    datasourceRequest.getDatasource().getType()
            );
            boolean readOnly = shouldUseReadOnlyConnection(
                    datasourceRequest,
                    datasourceConfiguration,
                    datasourceRequest.getDatasource().getType()
            );
            setConnectionReadOnly(con.getConnection(), readOnly);
            try (Statement statement = getStatement(con.getConnection(), 30);
                 ResultSet resultSet = statement.executeQuery(queryStr)) {
                while (resultSet.next()) {
                    schemas.add(resultSet.getString(1));
                }
            } finally {
                resetConnectionReadOnly(con.getConnection(), readOnly);
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            DEException.throwException(e.getMessage());
        }
        if (datasourceRequest.getDatasource().getType().equalsIgnoreCase(DatasourceConfiguration.DatasourceType.pg.name())) {
            Set<String> SYSTEM_SCHEMAS = new HashSet<>(Arrays.asList("information_schema", "pg_catalog", "pg_temp_1", "pg_toast", "pg_toast_temp_1"));
            return schemas.stream().filter(schema -> !SYSTEM_SCHEMAS.contains(schema)).collect(Collectors.toList());
        }
        return schemas;
    }

    @Override
    public String checkStatus(DatasourceRequest datasourceRequest) throws Exception {
        DatasourceConfiguration.DatasourceType datasourceType = DatasourceConfiguration.DatasourceType.valueOf(datasourceRequest.getDatasource().getType());
        switch (datasourceType) {
            case pg:
                DatasourceConfiguration configuration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), Pg.class);
                List<String> schemas = getSchema(datasourceRequest);
                if (CollectionUtils.isEmpty(schemas) || !schemas.contains(configuration.getSchema())) {
                    DEException.throwException("无效的 schema！");
                }
                break;
            default:
                break;
        }

        try (ConnectionObj con = getConnection(datasourceRequest.getDatasource())) {
            DatasourceConfiguration datasourceConfiguration = parseDatasourceConfiguration(
                    datasourceRequest.getDatasource().getConfiguration(),
                    datasourceRequest.getDatasource().getType()
            );
            boolean readOnly = shouldUseReadOnlyConnection(
                    datasourceRequest,
                    datasourceConfiguration,
                    datasourceRequest.getDatasource().getType()
            );
            setConnectionReadOnly(con.getConnection(), readOnly);
            try {
                datasourceRequest.setDsVersion(con.getConnection().getMetaData().getDatabaseMajorVersion());
                String querySql = getTablesSql(datasourceRequest).get(0);
                Statement statement = getStatement(con.getConnection(), 30);
                ResultSet resultSet = statement.executeQuery(querySql);
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
            } finally {
                resetConnectionReadOnly(con.getConnection(), readOnly);
            }
        } catch (Exception e) {
            throw e;
        }
        return "Success";
    }

    @Override
    public List<DatasetTableDTO> getTables(DatasourceRequest datasourceRequest) {
        List<DatasetTableDTO> tables = new ArrayList<>();
        try (Connection con = getConnectionFromPool(datasourceRequest.getDatasource().getId());
             Statement statement = getStatement(con, 30)) {
            DatasourceConfiguration datasourceConfiguration = parseDatasourceConfiguration(
                    datasourceRequest.getDatasource().getConfiguration(),
                    datasourceRequest.getDatasource().getType()
            );
            boolean readOnly = shouldUseReadOnlyConnection(
                    datasourceRequest,
                    datasourceConfiguration,
                    datasourceRequest.getDatasource().getType()
            );
            setConnectionReadOnly(con, readOnly);
            try {
                datasourceRequest.setDsVersion(con.getMetaData().getDatabaseMajorVersion());
                List<String> tablesSqls = getTablesSql(datasourceRequest);
                for (String tablesSql : tablesSqls) {
                    ResultSet resultSet = statement.executeQuery(tablesSql);
                    while (resultSet.next()) {
                        tables.add(getTableDesc(datasourceRequest, resultSet));
                    }
                }
            } finally {
                resetConnectionReadOnly(con, readOnly);
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            DEException.throwException(e.getMessage());
        }
        return tables;
    }

    @Override
    public Map<String, Object> fetchResultField(DatasourceRequest datasourceRequest) throws DEException {
        // 不跨数据源
        if (datasourceRequest.getIsCross() == null || !datasourceRequest.getIsCross()) {
            return jdbcFetchResultField(datasourceRequest);
        }

        List<TableField> datasetTableFields = new ArrayList<>();
        List<String[]> list = new LinkedList<>();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        Connection connection = take();
        try {
            CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
            statement = calciteConnection.prepareStatement(datasourceRequest.getQuery());
            resultSet = statement.executeQuery();
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                TableField tableField = new TableField();
                tableField.setOriginName(metaData.getColumnLabel(i));
                tableField.setType(metaData.getColumnTypeName(i));
                tableField.setPrecision(metaData.getPrecision(i));
                int deType = FieldUtils.transType2DeType(tableField.getType());
                tableField.setDeExtractType(deType);
                tableField.setDeType(deType);
                tableField.setScale(metaData.getScale(i));
                datasetTableFields.add(tableField);
            }
            list = getDataResult(resultSet);
        } catch (Exception | AssertionError e) {
            String msg;
            if (e.getCause() != null && e.getCause().getCause() != null) {
                msg = e.getMessage() + " [" + e.getCause().getCause().getMessage() + "]";
            } else {
                msg = e.getMessage();
            }
            DEException.throwException(Translator.get("i18n_fetch_error") + msg);
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
            } catch (Exception e) {
            }
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("fields", datasetTableFields);
        map.put("data", list);
        return map;
    }

    @Override
    public String transSqlDialect(String sql, Map<Long, DatasourceSchemaDTO> dsMap) throws DEException {
        DatasourceSchemaDTO value = dsMap.entrySet().iterator().next().getValue();
        try (Connection connection = getConnectionFromPool(value.getId());) {
            // 获取数据库version
            if (connection != null) {
                value.setDsVersion(connection.getMetaData().getDatabaseMajorVersion());
            }
            SqlParser parser = SqlParser.create(sql, SqlParser.Config.DEFAULT.withLex(Lex.JAVA));
            SqlNode sqlNode = parser.parseStmt();
            return sqlNode.toSqlString(getDialect(value)).toString();
        } catch (Exception e) {
            DEException.throwException(e.getMessage());
        }
        return null;
    }

    private List<TableField> fetchResultField(ResultSet rs) throws Exception {
        List<TableField> fieldList = new ArrayList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        for (int j = 0; j < columnCount; j++) {
            String columnName = metaData.getColumnName(j + 1);
            String label = StringUtils.isNotEmpty(metaData.getColumnLabel(j + 1)) ? metaData.getColumnLabel(j + 1) : columnName;
            TableField tableField = new TableField();
            tableField.setOriginName(columnName);
            tableField.setName(label);
            tableField.setType(metaData.getColumnTypeName(j + 1));
            tableField.setFieldType(tableField.getType());
            int deType = FieldUtils.transType2DeType(tableField.getType());
            tableField.setDeExtractType(deType);
            tableField.setDeType(deType);
            fieldList.add(tableField);
        }
        return fieldList;
    }

    private Map<String, Integer> getTableTypeMap(DatasourceRequest datasourceRequest, DatasourceConfiguration datasourceConfiguration, String tableName) throws DEException {
        Map<String, Integer> map = new HashMap<>();
        String schemaTable = (ObjectUtils.isNotEmpty(datasourceConfiguration.getSchema()) ? (quoteIdentifierPart(datasourceConfiguration.getSchema()) + ".") : "") + quoteIdentifierPart(tableName);
        // nosemgrep: java.lang.security.audit.formatted-sql-string.formatted-sql-string - SQL identifiers cannot be bound as parameters; each part is escaped by quoteIdentifierPart.
        String sql = "SELECT * FROM " + schemaTable + " LIMIT 0 OFFSET 0";
        sql = transSqlDialect(sql, datasourceRequest.getDsList());
        ResultSet resultSet = null;
        try (Connection con = getConnectionFromPool(datasourceRequest.getDatasource().getId());
             PreparedStatement statement = con.prepareStatement(sql)) {
            boolean readOnly = shouldUseReadOnlyConnection(
                    datasourceRequest,
                    datasourceConfiguration,
                    datasourceRequest.getDatasource().getType()
            );
            setConnectionReadOnly(con, readOnly);
            try {
                statement.setQueryTimeout(30);
                // nosemgrep: java.lang.security.audit.formatted-sql-string.formatted-sql-string - table identifiers were already quoted and escaped before preparing the statement.
                resultSet = statement.executeQuery();

                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();
                for (int j = 0; j < columnCount; j++) {
                    String name = StringUtils.lowerCase(metaData.getColumnName(j + 1));
                    Integer type = metaData.getColumnType(j + 1);
                    map.put(name, type);
                }
            } finally {
                resetConnectionReadOnly(con, readOnly);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return map;
    }

    private String quoteIdentifierPart(String identifier) {
        if (StringUtils.isBlank(identifier)) {
            DEException.throwException("Illegal empty identifier");
        }
        return "`" + identifier.replace("`", "``") + "`";
    }

    @Override
    public List<TableField> fetchTableField(DatasourceRequest datasourceRequest) throws DEException {
        if (datasourceRequest.getIsCross() != null && datasourceRequest.getIsCross()) {
            List<TableField> datasetTableFields = new ArrayList<>();
            PreparedStatement statement = null;
            ResultSet resultSet = null;
            Connection connection = take();
            try {
                CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
                statement = calciteConnection.prepareStatement(datasourceRequest.getQuery());
                resultSet = statement.executeQuery();
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    TableField tableField = new TableField();
                    tableField.setOriginName(metaData.getColumnLabel(i));
                    tableField.setType(metaData.getColumnTypeName(i));
                    tableField.setPrecision(metaData.getPrecision(i));
                    int deType = FieldUtils.transType2DeType(tableField.getType());
                    tableField.setDeExtractType(deType);
                    tableField.setDeType(deType);
                    tableField.setScale(metaData.getScale(i));
                    datasetTableFields.add(tableField);
                }
            } catch (Exception e) {
                throw DEException.getException(e.getMessage());
            }
            return datasetTableFields;
        }
        List<TableField> datasetTableFields = new ArrayList<>();
        DatasourceSchemaDTO datasourceSchemaDTO = datasourceRequest.getDsList().entrySet().iterator().next().getValue();
        datasourceRequest.setDatasource(datasourceSchemaDTO);

        DatasourceConfiguration datasourceConfiguration = parseDatasourceConfiguration(datasourceRequest.getDatasource().getConfiguration(), datasourceSchemaDTO.getType());

        String table = datasourceRequest.getTable();
        if (StringUtils.isEmpty(table)) {
            ResultSet resultSet = null;
            try (Connection con = getConnectionFromPool(datasourceRequest.getDatasource().getId());
                 Statement statement = getStatement(con, 30)) {
                if (isOracleLike(datasourceSchemaDTO.getType())) {
                    statement.executeUpdate("ALTER SESSION SET CURRENT_SCHEMA = " + datasourceConfiguration.getSchema());
                }
                boolean readOnly = shouldUseReadOnlyConnection(datasourceRequest, datasourceConfiguration, datasourceSchemaDTO.getType());
                setConnectionReadOnly(con, readOnly);
                try {
                    resultSet = statement.executeQuery(datasourceRequest.getQuery());
                    datasetTableFields.addAll(getField(resultSet, datasourceRequest));
                } finally {
                    resetConnectionReadOnly(con, readOnly);
                }
            } catch (Exception e) {
                DEException.throwException(e.getMessage());
            } finally {
                if (resultSet != null) {
                    try {
                        resultSet.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            if (!getTables(datasourceRequest).stream().map(DatasetTableDTO::getTableName).collect(Collectors.toList()).contains(table)) {
                DEException.throwException(Translator.get("i18n_invalid_table_name"));
            }
            ResultSet resultSet = null;
            try (Connection con = getConnectionFromPool(datasourceRequest.getDatasource().getId());
                 Statement statement = getStatement(con, 30)) {
                boolean readOnly = shouldUseReadOnlyConnection(datasourceRequest, datasourceConfiguration, datasourceSchemaDTO.getType());
                setConnectionReadOnly(con, readOnly);
                try {
                    datasourceRequest.setDsVersion(con.getMetaData().getDatabaseMajorVersion());
                    if (datasourceRequest.getDatasource().getType().equalsIgnoreCase("mongo")) {
                        resultSet = statement.executeQuery("select * from " + String.format(" `%s`", table) + " limit 0 offset 0 ");
                        return fetchResultField(resultSet);
                    }
                    if (isDorisCatalog(datasourceRequest)) {
                        resultSet = statement.executeQuery("desc " + String.format(" `%s`", table));
                    } else {
                        resultSet = statement.executeQuery(getTableFiledSql(datasourceRequest));
                    }

                    Map<String, Integer> tableTypeMap = getTableTypeMap(datasourceRequest, datasourceConfiguration, table);

                    while (resultSet.next()) {
                        TableField tableFieldDesc = getTableFieldDesc(datasourceRequest, resultSet, 3, tableTypeMap);
                        boolean repeat = false;
                        for (TableField ele : datasetTableFields) {
                            if (Strings.CI.equals(ele.getOriginName(), tableFieldDesc.getOriginName())) {
                                repeat = true;
                                break;
                            }
                        }
                        if (!repeat) {
                            datasetTableFields.add(tableFieldDesc);
                        }
                    }
                    if (isObOracle(datasourceRequest.getDatasource().getType())) {
                        DatasourceConfiguration configuration = parseOracleLikeConfiguration(datasourceRequest.getDatasource().getConfiguration(), datasourceRequest.getDatasource().getType());
                        applyOracleColumnComments(datasetTableFields, getOracleColumnComments(con, configuration.getSchema(), table));
                    }
                } finally {
                    resetConnectionReadOnly(con, readOnly);
                }
            } catch (Exception e) {
                DEException.throwException(e.getMessage());
            } finally {
                if (resultSet != null) {
                    try {
                        resultSet.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return datasetTableFields;
    }

    private boolean isDorisCatalog(DatasourceRequest datasourceRequest) {
        if (!datasourceRequest.getDatasource().getType().equalsIgnoreCase("doris")) {
            return false;
        }
        DatasourceConfiguration configuration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), Mysql.class);
        String database = "";
        if (StringUtils.isEmpty(configuration.getUrlType()) || configuration.getUrlType().equalsIgnoreCase("hostName")) {
            database = configuration.getDataBase();
        } else {
            Pattern WITH_SQL_FRAGMENT = Pattern.compile("jdbc:mysql://(.*):(\\d+)/(.*)");
            Matcher matcher = WITH_SQL_FRAGMENT.matcher(configuration.getJdbcUrl());
            matcher.find();
            String[] databasePrams = matcher.group(3).split("\\?");
            database = databasePrams[0];
        }
        return database.contains(".");
    }

    @Override
    public ConnectionObj getConnection(DatasourceDTO coreDatasource) throws Exception {
        ConnectionObj connectionObj = new ConnectionObj();
        DatasourceConfiguration configuration = null;
        DatasourceConfiguration.DatasourceType datasourceType = DatasourceConfiguration.DatasourceType.valueOf(coreDatasource.getType());
        switch (datasourceType) {
            case mysql:
            case mongo:
            case StarRocks:
            case doris:
            case TiDB:
            case mariadb:
                configuration = JsonUtil.parseObject(coreDatasource.getConfiguration(), Mysql.class);
                break;
            case impala:
                configuration = JsonUtil.parseObject(coreDatasource.getConfiguration(), Impala.class);
                break;
            case sqlServer:
                configuration = JsonUtil.parseObject(coreDatasource.getConfiguration(), Sqlserver.class);
                break;
            case oracle:
            case obOracle:
                configuration = parseOracleLikeConfiguration(coreDatasource.getConfiguration(), coreDatasource.getType());
                break;
            case db2:
                configuration = JsonUtil.parseObject(coreDatasource.getConfiguration(), Db2.class);
                break;
            case pg:
                configuration = JsonUtil.parseObject(coreDatasource.getConfiguration(), Pg.class);
                break;
            case redshift:
                configuration = JsonUtil.parseObject(coreDatasource.getConfiguration(), Redshift.class);
                break;
            case h2:
                configuration = JsonUtil.parseObject(coreDatasource.getConfiguration(), H2.class);
                break;
            case ck:
                configuration = JsonUtil.parseObject(coreDatasource.getConfiguration(), CK.class);
                break;
            default:
                configuration = JsonUtil.parseObject(coreDatasource.getConfiguration(), Mysql.class);
        }
        startSshSession(configuration, connectionObj, null);
        Properties props = new Properties();
        if (StringUtils.isNotBlank(configuration.getUsername())) {
            props.setProperty("user", configuration.getUsername());
        }
        if (StringUtils.isNotBlank(configuration.getPassword())) {
            props.setProperty("password", configuration.getPassword());
        }
        String driverClassName = configuration.getDriver();
        ExtendedJdbcClassLoader jdbcClassLoader = extendedJdbcClassLoader;
        Connection conn = null;
        try {
            Driver driverClass = (Driver) jdbcClassLoader.loadClass(driverClassName).newInstance();
            conn = driverClass.connect(configuration.getJdbc(), props);

        } catch (Exception e) {
            DEException.throwException(e.getMessage());
        }
        connectionObj.setConnection(conn);
        return connectionObj;
    }

    private DatasetTableDTO getTableDesc(DatasourceRequest datasourceRequest, ResultSet resultSet) throws SQLException {
        DatasetTableDTO tableDesc = new DatasetTableDTO();
        tableDesc.setDatasourceId(datasourceRequest.getDatasource().getId());
        tableDesc.setType("db");
        tableDesc.setTableName(resultSet.getString(1));
        if (resultSet.getMetaData().getColumnCount() > 1) {
            tableDesc.setName(resultSet.getString(2));
        } else {
            tableDesc.setName(resultSet.getString(1));
        }
        return tableDesc;
    }

    private List<String> getDriver() {
        List<String> drivers = new ArrayList<>();
        Map<String, DatasourceConfiguration> beansOfType = CommonBeanFactory.getApplicationContext().getBeansOfType((DatasourceConfiguration.class));
        beansOfType.keySet().forEach(key -> drivers.add(beansOfType.get(key).getDriver()));
        return drivers;
    }

    public Map<String, Object> jdbcFetchResultField(DatasourceRequest datasourceRequest) throws DEException {
        DatasourceSchemaDTO value = datasourceRequest.getDsList().entrySet().iterator().next().getValue();
        datasourceRequest.setDatasource(value);

        DatasourceConfiguration datasourceConfiguration = parseDatasourceConfiguration(datasourceRequest.getDatasource().getConfiguration(), value.getType());

        Map<String, Object> map = new LinkedHashMap<>();
        List<TableField> fieldList = new ArrayList<>();
        List<String[]> dataList = new LinkedList<>();

        // schema
        ResultSet resultSet = null;
        String oracleCharset = normalizeOracleCharset(datasourceConfiguration.getCharset());
        String oracleTargetCharset = normalizeOracleCharset(datasourceConfiguration.getTargetCharset());

        try (Connection con = getConnectionFromPool(datasourceRequest.getDatasource().getId())) {

            Statement statement = getStatement(value, con, datasourceRequest, datasourceConfiguration, null);
            boolean readOnly = shouldUseReadOnlyConnection(datasourceRequest, datasourceConfiguration, value.getType());
            setConnectionReadOnly(con, readOnly);
            try {
                if (CollectionUtils.isNotEmpty(datasourceRequest.getTableFieldWithValues())) {
                    LogUtil.info("execWithPreparedStatement sql: " + datasourceRequest.getQuery());
                    for (int i = 0; i < datasourceRequest.getTableFieldWithValues().size(); i++) {
                        try {
                            Object valueObject = datasourceRequest.getTableFieldWithValues().get(i).getValue();

                            if (valueObject instanceof String && isOracleLike(value.getType())) {
                                if (StringUtils.isNotEmpty(oracleCharset) && StringUtils.isNotEmpty(oracleTargetCharset)) {
                                    //转换为数据库的字符集
                                    valueObject = convertOracleText((String) valueObject, oracleTargetCharset, oracleCharset);
                                }
                                if (datasourceRequest.getTableFieldWithValues().get(i).getType().equals(Types.CLOB)) {
                                    Reader reader = new StringReader((String) valueObject);
                                    ((PreparedStatement) statement).setCharacterStream(i + 1, reader, ((String) valueObject).length());
                                } else {
                                    ((PreparedStatement) statement).setObject(i + 1, valueObject, datasourceRequest.getTableFieldWithValues().get(i).getType());
                                }
                            } else {
                                ((PreparedStatement) statement).setObject(i + 1, valueObject, datasourceRequest.getTableFieldWithValues().get(i).getType());
                            }
                            LogUtil.info("execWithPreparedStatement param[" + (i + 1) + "](" + datasourceRequest.getTableFieldWithValues().get(i).getColumnTypeName() + "): " + datasourceRequest.getTableFieldWithValues().get(i).getValue());
                        } catch (SQLException e) {
                            throw new SQLException(e.getMessage() + ". VALUE: " + datasourceRequest.getTableFieldWithValues().get(i).getValue().toString() + " , TARGET TYPE: " + datasourceRequest.getTableFieldWithValues().get(i).getColumnTypeName());
                        }
                    }
                    resultSet = ((PreparedStatement) statement).executeQuery();
                } else {
                    resultSet = statement.executeQuery(datasourceRequest.getQuery());
                }
                fieldList = getField(resultSet, datasourceRequest);
                dataList = getData(resultSet, datasourceRequest);
            } finally {
                resetConnectionReadOnly(con, readOnly);
            }
        } catch (SQLException e) {
            DEException.throwException("SQL ERROR: " + e.getMessage());
        } catch (Exception e) {
            DEException.throwException("Datasource connection exception: " + e.getMessage());
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        map.put("fields", fieldList);
        map.put("data", dataList);
        return map;
    }

    @Override
    public void exec(DatasourceRequest datasourceRequest) throws DEException {
        DatasourceSchemaDTO value = datasourceRequest.getDsList().entrySet().iterator().next().getValue();
        datasourceRequest.setDatasource(value);
        DatasourceConfiguration datasourceConfiguration = parseDatasourceConfiguration(datasourceRequest.getDatasource().getConfiguration(), value.getType());
        // schema
        ResultSet resultSet = null;
        String oracleCharset = normalizeOracleCharset(datasourceConfiguration.getCharset());
        String oracleTargetCharset = normalizeOracleCharset(datasourceConfiguration.getTargetCharset());

        try (Connection con = getConnectionFromPool(datasourceRequest.getDatasource().getId())) {

            Statement statement = getStatement(value, con, datasourceRequest, datasourceConfiguration, null);

            if (CollectionUtils.isNotEmpty(datasourceRequest.getTableFieldWithValues())) {
                LogUtil.info("execWithPreparedStatement sql: " + datasourceRequest.getQuery());
                for (int i = 0; i < datasourceRequest.getTableFieldWithValues().size(); i++) {
                    try {
                        Object valueObject = datasourceRequest.getTableFieldWithValues().get(i).getValue();
                        if (valueObject instanceof String && isOracleLike(value.getType())) {
                            if (StringUtils.isNotEmpty(oracleCharset) && StringUtils.isNotEmpty(oracleTargetCharset)) {
                                //转换为数据库的字符集
                                valueObject = convertOracleText((String) valueObject, oracleTargetCharset, oracleCharset);
                            }
                            if (datasourceRequest.getTableFieldWithValues().get(i).getType().equals(Types.CLOB)) {
                                Reader reader = new StringReader((String) valueObject);
                                ((PreparedStatement) statement).setCharacterStream(i + 1, reader, ((String) valueObject).length());
                            } else {
                                ((PreparedStatement) statement).setObject(i + 1, valueObject, datasourceRequest.getTableFieldWithValues().get(i).getType());
                            }
                        } else {
                            ((PreparedStatement) statement).setObject(i + 1, valueObject, datasourceRequest.getTableFieldWithValues().get(i).getType());
                        }
                        LogUtil.info("execWithPreparedStatement param[" + (i + 1) + "](" + datasourceRequest.getTableFieldWithValues().get(i).getColumnTypeName() + "): " + datasourceRequest.getTableFieldWithValues().get(i).getValue());
                    } catch (SQLException e) {
                        throw new SQLException(e.getMessage() + ". VALUE: " + datasourceRequest.getTableFieldWithValues().get(i).getValue().toString() + " , TARGET TYPE: " + datasourceRequest.getTableFieldWithValues().get(i).getColumnTypeName());
                    }
                }
                ((PreparedStatement) statement).execute();
            } else {
                statement.execute(datasourceRequest.getQuery());
            }

        } catch (SQLException e) {
            DEException.throwException("SQL ERROR: " + e.getMessage());
        } catch (Exception e) {
            DEException.throwException("Datasource connection exception: " + e.getMessage());
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 针对Oracle特殊处理
     */
    private Statement getStatement(DatasourceSchemaDTO value, Connection con, DatasourceRequest datasourceRequest, DatasourceConfiguration datasourceConfiguration, String autoIncrementPkName) throws Exception {
        Statement statement;
        if (isOracleLike(value.getType())) {
            statement = getStatement(con, datasourceConfiguration.getQueryTimeout());
            if (StringUtils.isNotBlank(datasourceConfiguration.getSchema())) {
                statement.executeUpdate("ALTER SESSION SET CURRENT_SCHEMA = " + datasourceConfiguration.getSchema());
            }
            statement.executeUpdate("ALTER SESSION SET NLS_TIMESTAMP_FORMAT = 'YYYY-MM-DD HH24:MI:SS'");
            //调整字符集
            String oracleCharset = normalizeOracleCharset(datasourceConfiguration.getCharset());
            String oracleTargetCharset = normalizeOracleCharset(datasourceConfiguration.getTargetCharset());
            if (StringUtils.isNotEmpty(oracleCharset) && StringUtils.isNotEmpty(oracleTargetCharset)) {
                datasourceRequest.setQuery(convertOracleText(datasourceRequest.getQuery(), oracleTargetCharset, oracleCharset));
            }
        }
        statement = getPreparedStatement(con, datasourceConfiguration.getQueryTimeout(), datasourceRequest.getQuery(), datasourceRequest.getTableFieldWithValues(), autoIncrementPkName, datasourceConfiguration);
        return statement;
    }

    @Override
    public ExecuteResult executeUpdate(DatasourceRequest datasourceRequest, String autoIncrementPkName) throws DEException {
        DatasourceSchemaDTO value = datasourceRequest.getDsList().entrySet().iterator().next().getValue();
        datasourceRequest.setDatasource(value);
        DatasourceConfiguration datasourceConfiguration = parseDatasourceConfiguration(datasourceRequest.getDatasource().getConfiguration(), value.getType());
        // schema
        ResultSet resultSet = null;
        String oracleCharset = normalizeOracleCharset(datasourceConfiguration.getCharset());
        String oracleTargetCharset = normalizeOracleCharset(datasourceConfiguration.getTargetCharset());
        try (Connection con = getConnectionFromPool(datasourceRequest.getDatasource().getId())) {

            Statement statement = getStatement(value, con, datasourceRequest, datasourceConfiguration, autoIncrementPkName);

            int count = 0;
            if (CollectionUtils.isNotEmpty(datasourceRequest.getTableFieldWithValues())) {
                LogUtil.info("execWithPreparedStatement sql: " + datasourceRequest.getQuery());
                for (int i = 0; i < datasourceRequest.getTableFieldWithValues().size(); i++) {
                    try {
                        Object valueObject = datasourceRequest.getTableFieldWithValues().get(i).getValue();

                        if (valueObject instanceof String && isOracleLike(value.getType())) {
                            if (StringUtils.isNotEmpty(oracleCharset) && StringUtils.isNotEmpty(oracleTargetCharset)) {
                                //转换为数据库的字符集
                                valueObject = convertOracleText((String) valueObject, oracleTargetCharset, oracleCharset);
                            }
                            if (datasourceRequest.getTableFieldWithValues().get(i).getType().equals(Types.CLOB)) {
                                Reader reader = new StringReader((String) valueObject);
                                ((PreparedStatement) statement).setCharacterStream(i + 1, reader, ((String) valueObject).length());
                            } else {
                                ((PreparedStatement) statement).setObject(i + 1, valueObject, datasourceRequest.getTableFieldWithValues().get(i).getType());
                            }
                        } else {
                            ((PreparedStatement) statement).setObject(i + 1, valueObject, datasourceRequest.getTableFieldWithValues().get(i).getType());
                        }
                        LogUtil.info("execWithPreparedStatement param[" + (i + 1) + "](" + datasourceRequest.getTableFieldWithValues().get(i).getColumnTypeName() + "): " + datasourceRequest.getTableFieldWithValues().get(i).getValue());
                    } catch (SQLException e) {
                        throw new SQLException(e.getMessage() + ". VALUE: " + datasourceRequest.getTableFieldWithValues().get(i).getValue().toString() + " , TARGET TYPE: " + datasourceRequest.getTableFieldWithValues().get(i).getColumnTypeName());
                    }
                }
                count = ((PreparedStatement) statement).executeUpdate();
            } else {
                count = statement.executeUpdate(datasourceRequest.getQuery());
            }

            ExecuteResult result = new ExecuteResult();
            result.setCount(count);

            if (StringUtils.isNotBlank(autoIncrementPkName)) {
                List<String> generatedKeys = new ArrayList<>();
                ResultSet keys = statement.getGeneratedKeys();
                while (keys.next()) {
                    generatedKeys.add(keys.getObject(1).toString());
                }
                result.setGeneratedKeys(generatedKeys);
            }

            return result;
        } catch (SQLException e) {
            DEException.throwException("SQL ERROR: " + e.getMessage());
        } catch (Exception e) {
            DEException.throwException("Datasource connection exception: " + e.getMessage());
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return new ExecuteResult();
    }

    private List<TableField> getField(ResultSet rs, DatasourceRequest datasourceRequest) throws Exception {
        List<TableField> fieldList = new ArrayList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        for (int j = 0; j < columnCount; j++) {
            String f = metaData.getColumnName(j + 1);
            if (Strings.CI.contains(f, "ROWNUM")) {
                continue;
            }
            String l = StringUtils.isNotEmpty(metaData.getColumnLabel(j + 1)) ? metaData.getColumnLabel(j + 1) : f;
            String t = metaData.getColumnTypeName(j + 1).toUpperCase();
            TableField field = new TableField();
            field.setOriginName(l);
            field.setName(l);
            field.setFieldType(t);
            field.setType(t);
            fieldList.add(field);
        }
        return fieldList;
    }

    private List<String[]> getData(ResultSet rs, DatasourceRequest datasourceRequest) throws Exception {
        String targetCharset = null;
        String originCharset = null;
        if (datasourceRequest != null && isOracleLike(datasourceRequest.getDatasource().getType())) {
            DatasourceConfiguration jdbcConfiguration = parseDatasourceConfiguration(datasourceRequest.getDatasource().getConfiguration(), datasourceRequest.getDatasource().getType());

            if (StringUtils.isNotEmpty(jdbcConfiguration.getCharset())) {
                originCharset = normalizeOracleCharset(jdbcConfiguration.getCharset());
            }
            if (StringUtils.isNotEmpty(jdbcConfiguration.getTargetCharset())) {
                targetCharset = normalizeOracleCharset(jdbcConfiguration.getTargetCharset());
            }
        }
        List<String[]> list = new LinkedList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        while (rs.next()) {
            String[] row = new String[columnCount];
            for (int j = 0; j < columnCount; j++) {
                int columnType = metaData.getColumnType(j + 1);
                switch (columnType) {
                    case Types.DATE:
                        if (rs.getDate(j + 1) != null) {
                            row[j] = rs.getDate(j + 1).toString();
                        }
                        break;
                    case Types.TIMESTAMP:
                        if (rs.getTimestamp(j + 1) != null) {
                            row[j] = rs.getTimestamp(j + 1).toString();
                        }
                        break;
                    case Types.BOOLEAN:
                        row[j] = rs.getBoolean(j + 1) ? "1" : "0";
                        break;
                    case Types.NUMERIC:
                        BigDecimal bigDecimal = rs.getBigDecimal(j + 1);
                        row[j] = bigDecimal == null ? null : bigDecimal.toString();
                        break;
                    default:
                        if (metaData.getColumnTypeName(j + 1).toLowerCase().equalsIgnoreCase("blob")) {
                            row[j] = rs.getBlob(j + 1) == null ? "" : rs.getBlob(j + 1).toString();
                        }
                        if (targetCharset != null && StringUtils.isNotEmpty(rs.getString(j + 1)) && columnType == Types.CLOB) {
                            if (originCharset == null) {
                                row[j] = new String(rs.getString(j + 1).getBytes(), targetCharset);
                            } else {
                                row[j] = new String(rs.getString(j + 1).getBytes(originCharset), targetCharset);
                            }
                        } else if (targetCharset != null && StringUtils.isNotEmpty(rs.getString(j + 1)) && (columnType != Types.NVARCHAR && columnType != Types.NCHAR)) {
                            row[j] = new String(rs.getBytes(j + 1), targetCharset);
                        } else {
                            row[j] = rs.getString(j + 1);
                        }

                        break;
                }
            }
            list.add(row);
        }
        return list;
    }

    private String normalizeOracleCharset(String charset) {
        if (StringUtils.isBlank(charset) || Strings.CI.equals(charset, "Default")) {
            return null;
        }
        String normalized = Strings.CI.equals(charset, "US7ASCII") ? "US-ASCII" : charset;
        try {
            return Charset.forName(normalized).name();
        } catch (UnsupportedCharsetException | IllegalCharsetNameException e) {
            DEException.throwException("Unsupported charset: " + charset);
        }
        return null;
    }

    private String convertOracleText(String value, String fromCharset, String toCharset) throws UnsupportedEncodingException {
        if (StringUtils.isEmpty(value) || StringUtils.isBlank(fromCharset) || StringUtils.isBlank(toCharset)) {
            return value;
        }
        return new String(value.getBytes(fromCharset), toCharset);
    }

    @Override
    public void hidePW(DatasourceDTO datasourceDTO) {
        DatasourceConfiguration configuration = null;
        DatasourceConfiguration.DatasourceType datasourceType = DatasourceConfiguration.DatasourceType.valueOf(datasourceDTO.getType());
        switch (datasourceType) {
            case mysql:
            case mongo:
            case mariadb:
            case TiDB:
            case StarRocks:
            case doris:
                configuration = JsonUtil.parseObject(datasourceDTO.getConfiguration(), Mysql.class);
                if (StringUtils.isNotEmpty(configuration.getUrlType()) && configuration.getUrlType().equalsIgnoreCase("jdbcUrl")) {
                    if (configuration.getJdbcUrl().contains("password=")) {
                        String[] params = configuration.getJdbcUrl().split("\\?")[1].split("&");
                        String pd = "";
                        for (int i = 0; i < params.length; i++) {
                            if (params[i].contains("password=")) {
                                pd = params[i];
                            }
                        }
                        configuration.setJdbcUrl(configuration.getJdbcUrl().replace(pd, "password=******"));
                        datasourceDTO.setConfiguration(JsonUtil.toJSONString(configuration).toString());
                    }
                }
                break;
            case obOracle:
                configuration = JsonUtil.parseObject(datasourceDTO.getConfiguration(), ObOracle.class);
                if (StringUtils.isNotEmpty(configuration.getUrlType()) && configuration.getUrlType().equalsIgnoreCase("jdbcUrl")) {
                    if (configuration.getJdbcUrl().contains("password=")) {
                        String[] params = configuration.getJdbcUrl().split("\\?")[1].split("&");
                        String pd = "";
                        for (int i = 0; i < params.length; i++) {
                            if (params[i].contains("password=")) {
                                pd = params[i];
                            }
                        }
                        configuration.setJdbcUrl(configuration.getJdbcUrl().replace(pd, "password=******"));
                        datasourceDTO.setConfiguration(JsonUtil.toJSONString(configuration).toString());
                    }
                }
                break;
            case pg:
                configuration = JsonUtil.parseObject(datasourceDTO.getConfiguration(), Pg.class);
                if (StringUtils.isNotEmpty(configuration.getUrlType()) && configuration.getUrlType().equalsIgnoreCase("jdbcUrl")) {
                    if (configuration.getJdbcUrl().contains("password=")) {
                        String[] params = configuration.getJdbcUrl().split("\\?")[1].split("&");
                        String pd = "";
                        for (int i = 0; i < params.length; i++) {
                            if (params[i].contains("password=")) {
                                pd = params[i];
                            }
                        }
                        configuration.setJdbcUrl(configuration.getJdbcUrl().replace(pd, "password=******"));
                        datasourceDTO.setConfiguration(JsonUtil.toJSONString(configuration).toString());
                    }
                }
                break;
            case redshift:
                configuration = JsonUtil.parseObject(datasourceDTO.getConfiguration(), Redshift.class);
                if (StringUtils.isNotEmpty(configuration.getUrlType()) && configuration.getUrlType().equalsIgnoreCase("jdbcUrl")) {
                    if (configuration.getJdbcUrl().contains("password=")) {
                        String[] params = configuration.getJdbcUrl().split("\\?")[1].split("&");
                        String pd = "";
                        for (int i = 0; i < params.length; i++) {
                            if (params[i].contains("password=")) {
                                pd = params[i];
                            }
                        }
                        configuration.setJdbcUrl(configuration.getJdbcUrl().replace(pd, "password=******"));
                        datasourceDTO.setConfiguration(JsonUtil.toJSONString(configuration).toString());
                    }
                }
                break;
            case ck:
                configuration = JsonUtil.parseObject(datasourceDTO.getConfiguration(), CK.class);
                if (StringUtils.isNotEmpty(configuration.getUrlType()) && configuration.getUrlType().equalsIgnoreCase("jdbcUrl")) {
                    if (configuration.getJdbcUrl().contains("password=")) {
                        String[] params = configuration.getJdbcUrl().split("\\?")[1].split("&");
                        String pd = "";
                        for (int i = 0; i < params.length; i++) {
                            if (params[i].contains("password=")) {
                                pd = params[i];
                            }
                        }
                        configuration.setJdbcUrl(configuration.getJdbcUrl().replace(pd, "password=******"));
                        datasourceDTO.setConfiguration(JsonUtil.toJSONString(configuration).toString());
                    }
                }
                break;
            case impala:
                configuration = JsonUtil.parseObject(datasourceDTO.getConfiguration(), Impala.class);
                if (StringUtils.isNotEmpty(configuration.getUrlType()) && configuration.getUrlType().equalsIgnoreCase("jdbcUrl")) {
                    if (configuration.getJdbcUrl().contains("password=")) {
                        String[] params = configuration.getJdbcUrl().split(";");
                        String pd = "";
                        for (int i = 0; i < params.length; i++) {
                            if (params[i].contains("password=")) {
                                pd = params[i];
                            }
                        }
                        configuration.setJdbcUrl(configuration.getJdbcUrl().replace(pd, "password=******"));
                        datasourceDTO.setConfiguration(JsonUtil.toJSONString(configuration).toString());
                    }
                }
                break;
            default:
                break;
        }
    }

    static void applyOracleColumnComments(List<TableField> fields, Map<String, String> columnComments) {
        if (CollectionUtils.isEmpty(fields) || columnComments == null || columnComments.isEmpty()) {
            return;
        }
        for (TableField field : fields) {
            String comments = columnComments.get(normalizeOracleName(field.getOriginName()));
            if (StringUtils.isNotBlank(comments)) {
                field.setName(comments.trim());
            }
        }
    }

    private Map<String, String> getOracleColumnComments(Connection connection, String schema, String table) throws SQLException {
        Map<String, String> comments = new HashMap<>();
        if (StringUtils.isBlank(schema) || StringUtils.isBlank(table)) {
            return comments;
        }
        String sql = "select column_name, comments from all_col_comments where owner = ? and table_name = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, normalizeOracleName(schema));
            statement.setString(2, normalizeOracleName(table));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    comments.put(normalizeOracleName(resultSet.getString("column_name")), resultSet.getString("comments"));
                }
            }
        }
        return comments;
    }

    private static String normalizeOracleName(String name) {
        return StringUtils.trimToEmpty(name).toUpperCase(Locale.ROOT);
    }

    private TableField getTableFieldDesc(DatasourceRequest datasourceRequest, ResultSet resultSet, int commentIndex, Map<String, Integer> tableTypeMap) throws SQLException {
        TableField tableField = new TableField();
        tableField.setOriginName(resultSet.getString(1));
        tableField.setType(resultSet.getString(2).toUpperCase());
        tableField.setFieldType(tableField.getType());
        int deType = FieldUtils.transType2DeType(tableField.getType());
        tableField.setDeExtractType(deType);
        tableField.setDeType(deType);
        tableField.setName(resultSet.getString(commentIndex));
        try {
            tableField.setPrimary(resultSet.getInt(4) > 0);
        } catch (Exception e) {
        }
        try {
            if (StringUtils.endsWithIgnoreCase(datasourceRequest.getDatasource().getType(), "oracle")) {
                if (Strings.CS.contains(resultSet.getString(5), "nextval") || Strings.CI.equals(resultSet.getString(5), "GENERATED ALWAYS AS IDENTITY")) {
                    tableField.setAutoIncrement(true);
                }
            } else {
                tableField.setAutoIncrement(resultSet.getInt(5) > 0);
            }
        } catch (Exception e) {
        }
        try {
            tableField.setTypeNumber(tableTypeMap.get(StringUtils.lowerCase(tableField.getOriginName())));
        } catch (Exception e) {
        }
        return tableField;
    }

    public Connection initConnection(Map<Long, DatasourceSchemaDTO> dsMap) throws SQLException {
        Connection connection = take();
        CalciteConnection calciteConnection = null;
        calciteConnection = connection.unwrap(CalciteConnection.class);
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDsList(dsMap);
        buildSchema(datasourceRequest, calciteConnection);
        return connection;
    }

    private void registerDriver() {
        for (String driverClass : getDriver()) {
            try {
                Driver driver = (Driver) extendedJdbcClassLoader.loadClass(driverClass).newInstance();
                DriverManager.registerDriver(new DriverShim(driver));
            } catch (Exception e) {
                LogUtil.debug("Skip unavailable datasource driver: " + driverClass);
            }
        }
    }

    private Connection getCalciteConnection() {
        registerDriver();
        Properties info = new Properties();
        info.setProperty(CalciteConnectionProperty.LEX.camelName(), "JAVA");
        info.setProperty(CalciteConnectionProperty.FUN.camelName(), "all");
        info.setProperty(CalciteConnectionProperty.CASE_SENSITIVE.camelName(), "false");
        info.setProperty(CalciteConnectionProperty.PARSER_FACTORY.camelName(), "org.apache.calcite.sql.parser.impl.SqlParserImpl#FACTORY");
        info.setProperty(CalciteConnectionProperty.DEFAULT_NULL_COLLATION.camelName(), NullCollation.LAST.name());
        info.setProperty("remarks", "true");
        Connection connection = null;
        try {
            Class.forName("org.apache.calcite.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:calcite:", info);
        } catch (Exception e) {
            DEException.throwException(e.getMessage());
        }
        return connection;
    }

    // 构建root schema
    private SchemaPlus buildSchema(DatasourceRequest datasourceRequest, CalciteConnection calciteConnection) {
        SchemaPlus rootSchema = calciteConnection.getRootSchema();
        Map<Long, DatasourceSchemaDTO> dsList = datasourceRequest.getDsList();
        for (Map.Entry<Long, DatasourceSchemaDTO> next : dsList.entrySet()) {
            DatasourceSchemaDTO ds = next.getValue();
            commonThreadPool.addTask(() -> {
                try {
                    BasicDataSource dataSource = new BasicDataSource();
                    dataSource.setMaxWaitMillis(5 * 1000);
                    dataSource.setTestWhileIdle(true);
                    dataSource.setTestOnBorrow(true);
                    dataSource.setTestOnReturn(true);
                    dataSource.setTimeBetweenEvictionRunsMillis(60 * 1000);
                    dataSource.setValidationQuery("select 1");
                    dataSource.setValidationQueryTimeout(5);
                    Schema schema = null;
                    DatasourceConfiguration configuration = null;
                    DatasourceConfiguration.DatasourceType datasourceType = DatasourceConfiguration.DatasourceType.valueOf(ds.getType());
                    try {
                        if (rootSchema.getSubSchema(ds.getSchemaAlias()) != null) {
                            JdbcSchema jdbcSchema = rootSchema.getSubSchema(ds.getSchemaAlias()).unwrap(JdbcSchema.class);
                            BasicDataSource basicDataSource = (BasicDataSource) jdbcSchema.getDataSource();
                            basicDataSource.close();
                            removeSubSchema(rootSchema, ds.getSchemaAlias());
                        }
                        switch (datasourceType) {
                            case mysql:
                            case mongo:
                            case mariadb:
                            case TiDB:
                            case StarRocks:
                            case doris:
                                configuration = JsonUtil.parseObject(ds.getConfiguration(), Mysql.class);
                                if (StringUtils.isNotBlank(configuration.getUsername())) {
                                    dataSource.setUsername(configuration.getUsername());
                                }
                                if (StringUtils.isNotBlank(configuration.getPassword())) {
                                    dataSource.setPassword(configuration.getPassword());
                                }
                                dataSource.setDefaultQueryTimeout(Integer.valueOf(configuration.getQueryTimeout()));
                                dataSource.setInitialSize(configuration.getInitialPoolSize());
                                dataSource.setMaxTotal(configuration.getMaxPoolSize());
                                dataSource.setMinIdle(configuration.getMinPoolSize());
                                startSshSession(configuration, null, ds.getId());
                                dataSource.setUrl(configuration.getJdbc());
                                schema = JdbcSchema.create(rootSchema, ds.getSchemaAlias(), dataSource, null, configuration.getDataBase());
                                rootSchema.add(ds.getSchemaAlias(), schema);
                                break;
                            case impala:
                                configuration = JsonUtil.parseObject(ds.getConfiguration(), Impala.class);
                                if (StringUtils.isNotBlank(configuration.getUsername())) {
                                    dataSource.setUsername(configuration.getUsername());
                                }
                                if (StringUtils.isNotBlank(configuration.getPassword())) {
                                    dataSource.setPassword(configuration.getPassword());
                                }
                                dataSource.setInitialSize(configuration.getInitialPoolSize());
                                dataSource.setMaxTotal(configuration.getMaxPoolSize());
                                dataSource.setMinIdle(configuration.getMinPoolSize());
                                dataSource.setDefaultQueryTimeout(Integer.valueOf(configuration.getQueryTimeout()));
                                startSshSession(configuration, null, ds.getId());
                                dataSource.setUrl(configuration.getJdbc());
                                schema = JdbcSchema.create(rootSchema, ds.getSchemaAlias(), dataSource, null, configuration.getDataBase());
                                rootSchema.add(ds.getSchemaAlias(), schema);
                                break;
                            case sqlServer:
                                configuration = JsonUtil.parseObject(ds.getConfiguration(), Sqlserver.class);
                                if (StringUtils.isNotBlank(configuration.getUsername())) {
                                    dataSource.setUsername(configuration.getUsername());
                                }
                                if (StringUtils.isNotBlank(configuration.getPassword())) {
                                    dataSource.setPassword(configuration.getPassword());
                                }
                                dataSource.setInitialSize(configuration.getInitialPoolSize());
                                dataSource.setMaxTotal(configuration.getMaxPoolSize());
                                dataSource.setMinIdle(configuration.getMinPoolSize());
                                dataSource.setDefaultQueryTimeout(Integer.valueOf(configuration.getQueryTimeout()));
                                startSshSession(configuration, null, ds.getId());
                                dataSource.setUrl(configuration.getJdbc());
                                schema = JdbcSchema.create(rootSchema, ds.getSchemaAlias(), dataSource, null, configuration.getSchema());
                                rootSchema.add(ds.getSchemaAlias(), schema);
                                break;
                            case oracle:
                            case obOracle:
                                dataSource.setValidationQuery("SELECT 1 FROM DUAL");
                                configuration = parseOracleLikeConfiguration(ds.getConfiguration(), ds.getType());
                                if (StringUtils.isNotBlank(configuration.getUsername())) {
                                    dataSource.setUsername(configuration.getUsername());
                                }
                                if (StringUtils.isNotBlank(configuration.getPassword())) {
                                    dataSource.setPassword(configuration.getPassword());
                                }
                                dataSource.setInitialSize(configuration.getInitialPoolSize());
                                dataSource.setMaxTotal(configuration.getMaxPoolSize());
                                dataSource.setMinIdle(configuration.getMinPoolSize());
                                dataSource.setDefaultQueryTimeout(Integer.valueOf(configuration.getQueryTimeout()));
                                startSshSession(configuration, null, ds.getId());
                                dataSource.setUrl(configuration.getJdbc());
                                schema = JdbcSchema.create(rootSchema, ds.getSchemaAlias(), dataSource, null, configuration.getSchema());
                                rootSchema.add(ds.getSchemaAlias(), schema);
                                break;
                            case db2:
                                configuration = JsonUtil.parseObject(ds.getConfiguration(), Db2.class);
                                dataSource.setValidationQuery("select 1 from syscat.tables  WHERE TABSCHEMA ='DE_SCHEMA' AND \"TYPE\" = 'T'".replace("DE_SCHEMA", configuration.getSchema()));
                                if (StringUtils.isNotBlank(configuration.getUsername())) {
                                    dataSource.setUsername(configuration.getUsername());
                                }
                                if (StringUtils.isNotBlank(configuration.getPassword())) {
                                    dataSource.setPassword(configuration.getPassword());
                                }
                                dataSource.setInitialSize(configuration.getInitialPoolSize());
                                dataSource.setMaxTotal(configuration.getMaxPoolSize());
                                dataSource.setMinIdle(configuration.getMinPoolSize());
                                dataSource.setDefaultQueryTimeout(Integer.valueOf(configuration.getQueryTimeout()));
                                startSshSession(configuration, null, ds.getId());
                                dataSource.setUrl(configuration.getJdbc());
                                schema = JdbcSchema.create(rootSchema, ds.getSchemaAlias(), dataSource, null, configuration.getSchema());
                                rootSchema.add(ds.getSchemaAlias(), schema);
                                break;
                            case ck:
                                configuration = JsonUtil.parseObject(ds.getConfiguration(), CK.class);
                                if (StringUtils.isNotBlank(configuration.getUsername())) {
                                    dataSource.setUsername(configuration.getUsername());
                                }
                                if (StringUtils.isNotBlank(configuration.getPassword())) {
                                    dataSource.setPassword(configuration.getPassword());
                                }
                                dataSource.setInitialSize(configuration.getInitialPoolSize());
                                dataSource.setMaxTotal(configuration.getMaxPoolSize());
                                dataSource.setMinIdle(configuration.getMinPoolSize());
                                dataSource.setDefaultQueryTimeout(Integer.valueOf(configuration.getQueryTimeout()));
                                startSshSession(configuration, null, ds.getId());
                                dataSource.setUrl(configuration.getJdbc());
                                schema = JdbcSchema.create(rootSchema, ds.getSchemaAlias(), dataSource, null, configuration.getDataBase());
                                rootSchema.add(ds.getSchemaAlias(), schema);
                                break;
                            case pg:
                                configuration = JsonUtil.parseObject(ds.getConfiguration(), Pg.class);
                                if (StringUtils.isNotBlank(configuration.getUsername())) {
                                    dataSource.setUsername(configuration.getUsername());
                                }
                                if (StringUtils.isNotBlank(configuration.getPassword())) {
                                    dataSource.setPassword(configuration.getPassword());
                                }
                                dataSource.setInitialSize(configuration.getInitialPoolSize());
                                dataSource.setMaxTotal(configuration.getMaxPoolSize());
                                dataSource.setMinIdle(configuration.getMinPoolSize());
                                dataSource.setDefaultQueryTimeout(Integer.valueOf(configuration.getQueryTimeout()));
                                startSshSession(configuration, null, ds.getId());
                                dataSource.setUrl(configuration.getJdbc());
                                schema = JdbcSchema.create(rootSchema, ds.getSchemaAlias(), dataSource, null, configuration.getSchema());
                                rootSchema.add(ds.getSchemaAlias(), schema);
                                break;
                            case redshift:
                                configuration = JsonUtil.parseObject(ds.getConfiguration(), Redshift.class);
                                if (StringUtils.isNotBlank(configuration.getUsername())) {
                                    dataSource.setUsername(configuration.getUsername());
                                }
                                if (StringUtils.isNotBlank(configuration.getPassword())) {
                                    dataSource.setPassword(configuration.getPassword());
                                }
                                dataSource.setInitialSize(configuration.getInitialPoolSize());
                                dataSource.setMaxTotal(configuration.getMaxPoolSize());
                                dataSource.setMinIdle(configuration.getMinPoolSize());
                                dataSource.setDefaultQueryTimeout(Integer.valueOf(configuration.getQueryTimeout()));
                                startSshSession(configuration, null, ds.getId());
                                dataSource.setUrl(configuration.getJdbc());
                                schema = JdbcSchema.create(rootSchema, ds.getSchemaAlias(), dataSource, null, configuration.getSchema());
                                rootSchema.add(ds.getSchemaAlias(), schema);
                                break;
                            case h2:
                                configuration = JsonUtil.parseObject(ds.getConfiguration(), H2.class);
                                if (StringUtils.isNotBlank(configuration.getUsername())) {
                                    dataSource.setUsername(configuration.getUsername());
                                }
                                if (StringUtils.isNotBlank(configuration.getPassword())) {
                                    dataSource.setPassword(configuration.getPassword());
                                }
                                dataSource.setInitialSize(configuration.getInitialPoolSize());
                                dataSource.setMaxTotal(configuration.getMaxPoolSize());
                                dataSource.setMinIdle(configuration.getMinPoolSize());
                                dataSource.setDefaultQueryTimeout(Integer.valueOf(configuration.getQueryTimeout()));
                                startSshSession(configuration, null, ds.getId());
                                dataSource.setUrl(configuration.getJdbc());
                                schema = JdbcSchema.create(rootSchema, ds.getSchemaAlias(), dataSource, null, configuration.getDataBase());
                                rootSchema.add(ds.getSchemaAlias(), schema);
                                break;
                            default:
                                configuration = JsonUtil.parseObject(ds.getConfiguration(), Mysql.class);
                                if (StringUtils.isNotBlank(configuration.getUsername())) {
                                    dataSource.setUsername(configuration.getUsername());
                                }
                                if (StringUtils.isNotBlank(configuration.getPassword())) {
                                    dataSource.setPassword(configuration.getPassword());
                                }
                                dataSource.setInitialSize(configuration.getInitialPoolSize());
                                dataSource.setMaxTotal(configuration.getMaxPoolSize());
                                dataSource.setMinIdle(configuration.getMinPoolSize());
                                dataSource.setDefaultQueryTimeout(Integer.valueOf(configuration.getQueryTimeout()));
                                startSshSession(configuration, null, ds.getId());
                                dataSource.setUrl(configuration.getJdbc());
                                schema = JdbcSchema.create(rootSchema, ds.getSchemaAlias(), dataSource, null, configuration.getDataBase());
                                rootSchema.add(ds.getSchemaAlias(), schema);
                        }
                    } catch (Exception e) {
                        LogUtil.info("Skip unavailable datasource pool: " + ds.getName());
                    }
                } catch (Exception e) {
                }
            });
        }
        return rootSchema;
    }

    private List<String[]> getDataResult(ResultSet rs) {
        List<String[]> list = new LinkedList<>();
        try {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (rs.next()) {
                String[] row = new String[columnCount];
                for (int j = 0; j < columnCount; j++) {
                    int columnType = metaData.getColumnType(j + 1);
                    switch (columnType) {
                        case Types.DATE:
                            if (rs.getDate(j + 1) != null) {
                                row[j] = rs.getDate(j + 1).toString();
                            }
                            break;
                        case Types.BOOLEAN:
                            row[j] = rs.getBoolean(j + 1) ? "true" : "false";
                            break;
                        default:
                            if (metaData.getColumnTypeName(j + 1).toLowerCase().equalsIgnoreCase("blob")) {
                                row[j] = rs.getBlob(j + 1) == null ? "" : rs.getBlob(j + 1).toString();
                            } else {
                                row[j] = rs.getString(j + 1);
                            }
                            break;
                    }
                }
                list.add(row);
            }
        } catch (Exception e) {
            DEException.throwException(e.getMessage());
        }
        return list;
    }

    private String getTableFiledSql(DatasourceRequest datasourceRequest) {
        String sql = "";
        DatasourceConfiguration configuration = null;
        String database = "";
        DatasourceConfiguration.DatasourceType datasourceType = DatasourceConfiguration.DatasourceType.valueOf(datasourceRequest.getDatasource().getType());
        switch (datasourceType) {
            case StarRocks:
            case doris:
                configuration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), Mysql.class);
                if (StringUtils.isEmpty(configuration.getUrlType()) || configuration.getUrlType().equalsIgnoreCase("hostName")) {
                    database = configuration.getDataBase();
                } else {
                    Pattern WITH_SQL_FRAGMENT = Pattern.compile("jdbc:mysql://(.*):(\\d+)/(.*)");
                    Matcher matcher = WITH_SQL_FRAGMENT.matcher(configuration.getJdbcUrl());
                    matcher.find();
                    String[] databasePrams = matcher.group(3).split("\\?");
                    database = databasePrams[0];
                }
                if (database.contains(".")) {
                    sql = "select * from " + datasourceRequest.getTable() + " limit 0 offset 0 ";
                } else {
                    sql = String.format("SELECT COLUMN_NAME,DATA_TYPE,COLUMN_COMMENT,IF(COLUMN_KEY='PRI',1,0),IF(EXTRA LIKE '%%auto_increment%%',1,0) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = '%s' AND TABLE_NAME = '%s'", database, datasourceRequest.getTable());
                }
                break;
            case mysql:
            case mongo:
            case mariadb:
            case TiDB:
                configuration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), Mysql.class);
                if (StringUtils.isEmpty(configuration.getUrlType()) || configuration.getUrlType().equalsIgnoreCase("hostName")) {
                    database = configuration.getDataBase();
                } else {
                    Pattern WITH_SQL_FRAGMENT = Pattern.compile("jdbc:mysql://(.*):(\\d+)/(.*)");
                    Matcher matcher = WITH_SQL_FRAGMENT.matcher(configuration.getJdbcUrl());
                    matcher.find();
                    String[] databasePrams = matcher.group(3).split("\\?");
                    database = databasePrams[0];
                }
                sql = String.format("SELECT COLUMN_NAME,DATA_TYPE,COLUMN_COMMENT,IF(COLUMN_KEY='PRI',1,0),IF(EXTRA LIKE '%%auto_increment%%',1,0) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = '%s' AND TABLE_NAME = '%s'", database, datasourceRequest.getTable());
                break;
            case oracle:
            case obOracle:
                configuration = parseOracleLikeConfiguration(datasourceRequest.getDatasource().getConfiguration(), datasourceRequest.getDatasource().getType());
                if (StringUtils.isEmpty(configuration.getSchema())) {
                    DEException.throwException(Translator.get("i18n_schema_is_empty"));
                }
                sql = String.format("""
                        SELECT tc.COLUMN_NAME AS ColumnName,
                               tc.DATA_TYPE,
                               cc.COMMENTS,
                               CASE
                                   WHEN ac.COLUMN_NAME IS NOT NULL THEN 1
                                   ELSE 0
                                   END,
                               tc.DATA_DEFAULT
                        FROM ALL_TAB_COLUMNS tc
                                 LEFT JOIN (SELECT cols.OWNER,
                                                   cols.TABLE_NAME,
                                                   cols.COLUMN_NAME
                                            FROM ALL_CONSTRAINTS cons
                                                     JOIN
                                                 ALL_CONS_COLUMNS cols
                                                 ON cons.OWNER = cols.OWNER
                                                     AND cons.CONSTRAINT_NAME = cols.CONSTRAINT_NAME
                                            WHERE cons.TABLE_NAME = '%s'
                                              AND cons.CONSTRAINT_TYPE = 'P') ac
                                           ON tc.OWNER = ac.OWNER
                                               AND tc.TABLE_NAME = ac.TABLE_NAME
                                               AND tc.COLUMN_NAME = ac.COLUMN_NAME
                                 LEFT JOIN ALL_COL_COMMENTS cc
                                           ON tc.owner = cc.owner AND tc.table_name = cc.table_name AND tc.column_name = cc.column_name
                        WHERE tc.TABLE_NAME = '%s'
                          AND tc.OWNER = '%s'
                        ORDER BY tc.TABLE_NAME, tc.COLUMN_ID
                        """, datasourceRequest.getTable(), datasourceRequest.getTable(), configuration.getSchema());
                break;
            case db2:
                configuration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), Db2.class);
                if (StringUtils.isEmpty(configuration.getSchema())) {
                    DEException.throwException(Translator.get("i18n_schema_is_empty"));
                }
                sql = String.format("SELECT COLNAME, TYPENAME, REMARKS, 0, 0 FROM SYSCAT.COLUMNS WHERE TABSCHEMA = '%s' AND TABNAME = '%s' ", configuration.getSchema(), datasourceRequest.getTable());
                break;
            case sqlServer:
                configuration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), Sqlserver.class);
                if (StringUtils.isEmpty(configuration.getSchema())) {
                    DEException.throwException(Translator.get("i18n_schema_is_empty"));
                }

                sql = String.format("""
                        SELECT
                            c.name,
                            t.name,
                            CAST(ep.value AS NVARCHAR(4000)),
                            CASE
                                WHEN pk.column_id IS NOT NULL THEN 1
                                ELSE 0
                                END,
                            COLUMNPROPERTY(c.object_id, c.name, 'IsIdentity')
                        FROM sys.columns AS c
                                 INNER JOIN sys.objects AS o ON c.object_id = o.object_id
                                 INNER JOIN sys.schemas AS s ON o.schema_id = s.schema_id
                                 LEFT JOIN sys.types AS t ON c.user_type_id = t.user_type_id
                                 LEFT JOIN sys.extended_properties AS ep
                                           ON c.object_id = ep.major_id
                                               AND c.column_id = ep.minor_id
                                               AND ep.name = 'MS_Description'
                                 LEFT JOIN (
                            SELECT ic.object_id, ic.column_id
                            FROM sys.indexes i
                                     INNER JOIN sys.index_columns ic
                                                ON i.object_id = ic.object_id
                                                    AND i.index_id = ic.index_id
                            WHERE i.is_primary_key = 1
                        ) pk ON c.object_id = pk.object_id AND c.column_id = pk.column_id
                        WHERE o.name = '%s'
                          AND s.name = '%s'
                        ORDER BY c.column_id
                        """, datasourceRequest.getTable(), configuration.getSchema());
                break;
            case pg:
                configuration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), Pg.class);
                if (StringUtils.isEmpty(configuration.getSchema())) {
                    DEException.throwException(Translator.get("i18n_schema_is_empty"));
                }
                sql = String.format("""
                        SELECT a.attname     AS ColumnName,
                               t.typname,
                               b.description AS ColumnDescription,
                               CASE
                                   WHEN d.indisprimary THEN 1
                                   ELSE 0
                                   END,
                               CASE
                                   WHEN pg_get_expr(ad.adbin, ad.adrelid) LIKE 'nextval%%' THEN 1
                        """ + (datasourceRequest.getDsVersion() > 9 ? """
                                   WHEN a.attidentity = 'd' THEN 1
                                   WHEN a.attidentity = 'a' THEN 1
                        """ : "") + """
                                   ELSE 0
                                   END
                        FROM pg_class c
                                 JOIN pg_attribute a ON a.attrelid = c.oid
                                 LEFT JOIN pg_attrdef ad ON a.attrelid = ad.adrelid AND a.attnum = ad.adnum
                                 LEFT JOIN pg_description b ON a.attrelid = b.objoid AND a.attnum = b.objsubid
                                 JOIN pg_type t ON a.atttypid = t.oid
                                 LEFT JOIN pg_index d ON d.indrelid = a.attrelid AND d.indisprimary AND a.attnum = ANY (d.indkey)
                        where c.relnamespace = (SELECT oid FROM pg_namespace WHERE nspname = '%s')
                          AND c.relname = '%s'
                          AND a.attnum > 0
                          AND NOT a.attisdropped
                        ORDER BY a.attnum;
                        """, configuration.getSchema(), datasourceRequest.getTable());
                break;
            case redshift:
                configuration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), CK.class);
                sql = String.format("SELECT\n" + "    a.attname AS ColumnName,\n" + "    t.typname,\n" + "    b.description AS ColumnDescription,\n" + "    0, 0\n" + "FROM\n" + "    pg_class c\n" + "    JOIN pg_attribute a ON a.attrelid = c.oid\n" + "    LEFT JOIN pg_description b ON a.attrelid = b.objoid AND a.attnum = b.objsubid\n" + "    JOIN pg_type t ON a.atttypid = t.oid\n" + "WHERE\n" + "    c.relname = '%s'\n" + "    AND a.attnum > 0\n" + "    AND NOT a.attisdropped\n" + "ORDER BY\n" + "    a.attnum\n" + "   ", datasourceRequest.getTable());
                break;
            case ck:
                configuration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), CK.class);

                if (StringUtils.isEmpty(configuration.getUrlType()) || configuration.getUrlType().equalsIgnoreCase("hostName")) {
                    database = configuration.getDataBase();
                } else {
                    Pattern WITH_SQL_FRAGMENT = Pattern.compile("jdbc:clickhouse://(.*):(\\d+)/(.*)");
                    Matcher matcher = WITH_SQL_FRAGMENT.matcher(configuration.getJdbcUrl());
                    matcher.find();
                    String[] databasePrams = matcher.group(3).split("\\?");
                    database = databasePrams[0];
                }
                sql = String.format(" SELECT\n" + "    name,\n" + "    type,\n" + "    comment,\n" + "    0, 0\n" + "FROM\n" + "    system.columns\n" + "WHERE\n" + "    database = '%s'  \n" + "    AND table = '%s' ", database, datasourceRequest.getTable());
                break;
            case impala:
                sql = String.format("DESCRIBE `%s`", datasourceRequest.getTable());
                break;
            case h2:
                sql = String.format("SELECT COLUMN_NAME, DATA_TYPE, REMARKS, 0, 0 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '%s'", datasourceRequest.getTable());
                break;
            default:
                break;
        }

        return sql;
    }

    private List<String> getTablesSql(DatasourceRequest datasourceRequest) throws DEException {
        List<String> tableSqls = new ArrayList<>();
        DatasourceConfiguration.DatasourceType datasourceType = DatasourceConfiguration.DatasourceType.valueOf(datasourceRequest.getDatasource().getType());
        DatasourceConfiguration configuration = null;
        String database = "";
        switch (datasourceType) {
            case StarRocks:
            case doris:
                configuration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), Mysql.class);
                if (StringUtils.isEmpty(configuration.getUrlType()) || configuration.getUrlType().equalsIgnoreCase("hostName")) {
                    database = configuration.getDataBase();
                } else {
                    Pattern WITH_SQL_FRAGMENT = Pattern.compile("jdbc:mysql://(.*):(\\d+)/(.*)");
                    Matcher matcher = WITH_SQL_FRAGMENT.matcher(configuration.getJdbcUrl());
                    matcher.find();
                    String[] databasePrams = matcher.group(3).split("\\?");
                    database = databasePrams[0];
                }
                if (database.contains(".")) {
                    tableSqls.add("show tables");
                } else {
                    tableSqls.add(String.format("SELECT TABLE_NAME,TABLE_COMMENT FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '%s' ;", database));
                }
                break;
            case mongo:
                tableSqls.add("show tables");
                break;
            case mysql:
            case mariadb:
            case TiDB:
                configuration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), Mysql.class);
                if (StringUtils.isEmpty(configuration.getUrlType()) || configuration.getUrlType().equalsIgnoreCase("hostName")) {
                    database = configuration.getDataBase();
                } else {
                    Pattern WITH_SQL_FRAGMENT = Pattern.compile("jdbc:mysql://(.*):(\\d+)/(.*)");
                    Matcher matcher = WITH_SQL_FRAGMENT.matcher(configuration.getJdbcUrl());
                    matcher.find();
                    String[] databasePrams = matcher.group(3).split("\\?");
                    database = databasePrams[0];
                }
                tableSqls.add(String.format("SELECT TABLE_NAME,TABLE_COMMENT FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '%s' ;", database));
                break;
            case oracle:
            case obOracle:
                configuration = parseOracleLikeConfiguration(datasourceRequest.getDatasource().getConfiguration(), datasourceRequest.getDatasource().getType());
                if (StringUtils.isEmpty(configuration.getSchema())) {
                    DEException.throwException(Translator.get("i18n_schema_is_empty"));
                }
                tableSqls.add("select table_name, comments, owner  from all_tab_comments where owner='" + configuration.getSchema() + "' AND table_type = 'TABLE'");
                tableSqls.add("select table_name, comments, owner  from all_tab_comments where owner='" + configuration.getSchema() + "' AND table_type = 'VIEW'");
                if (!isObOracle(datasourceRequest.getDatasource().getType())) {
                    tableSqls.add("SELECT \n" + "    m.mview_name,\n" + "    c.comments\n" + "FROM \n" + "    ALL_MVIEWS m\n" + "LEFT JOIN \n" + "    ALL_TAB_COMMENTS c \n" + "ON \n" + "    m.owner = c.owner \n" + "    AND m.mview_name = c.table_name\n" + "    AND c.table_type = 'MATERIALIZED VIEW'\n" + "WHERE m.OWNER ='DE_SCHEMA'".replace("DE_SCHEMA", configuration.getSchema()));
                }
                break;
            case db2:
                configuration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), Db2.class);
                if (StringUtils.isEmpty(configuration.getSchema())) {
                    DEException.throwException(Translator.get("i18n_schema_is_empty"));
                }
                tableSqls.add("select TABNAME, REMARKS from syscat.tables  WHERE TABSCHEMA ='DE_SCHEMA' AND \"TYPE\" = 'T'".replace("DE_SCHEMA", configuration.getSchema()));
                break;
            case sqlServer:
                configuration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), Sqlserver.class);
                if (StringUtils.isEmpty(configuration.getSchema())) {
                    DEException.throwException(Translator.get("i18n_schema_is_empty"));
                }
                tableSqls.add("SELECT   \n" + "    t.name AS TableName,  \n" + "    ep.value AS TableDescription  \n" + "FROM   \n" + "    sys.tables t  \n" + "LEFT OUTER JOIN   sys.schemas sc ON sc.schema_id =t.schema_id \n" + "LEFT OUTER JOIN   \n" + "    sys.extended_properties ep ON t.object_id = ep.major_id   \n" + "                               AND ep.minor_id = 0   \n" + "                               AND ep.class = 1  \n" + "                               AND ep.name = 'MS_Description'\n" + "where sc.name ='DS_SCHEMA'".replace("DS_SCHEMA", configuration.getSchema()));
                tableSqls.add("SELECT   \n" + "    t.name AS TableName,  \n" + "    ep.value AS TableDescription  \n" + "FROM   \n" + "    sys.views t  \n" + "LEFT OUTER JOIN   sys.schemas sc ON sc.schema_id =t.schema_id \n" + "LEFT OUTER JOIN   \n" + "    sys.extended_properties ep ON t.object_id = ep.major_id   \n" + "                               AND ep.minor_id = 0   \n" + "                               AND ep.class = 1  \n" + "                               AND ep.name = 'MS_Description'\n" + "where sc.name ='DS_SCHEMA'".replace("DS_SCHEMA", configuration.getSchema()));
                break;
            case pg:
                configuration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), Pg.class);
                if (StringUtils.isEmpty(configuration.getSchema())) {
                    DEException.throwException(Translator.get("i18n_schema_is_empty"));
                }
                tableSqls.add("SELECT  \n" + "    relname AS TableName,  \n" + "    obj_description(relfilenode::regclass, 'pg_class') AS TableDescription  \n" + "FROM  \n" + "    pg_class  \n" + "WHERE  \n" + "   relkind in  ('r','p', 'f')  \n" + "    AND relnamespace = (SELECT oid FROM pg_namespace WHERE nspname = 'SCHEMA') ".replace("SCHEMA", configuration.getSchema()));
                tableSqls.add("SELECT \n" + "    c.relname AS view_name,\n" + "    COALESCE(d.description, 'No description provided') AS view_description\n" + "FROM \n" + "    pg_class c\n" + "JOIN \n" + "    pg_namespace n ON c.relnamespace = n.oid\n" + "LEFT JOIN \n" + "    pg_description d ON c.oid = d.objoid\n" + "WHERE \n" + "    c.relkind = 'v'  \n" + "    AND n.nspname = 'SCHEMA'".replace("SCHEMA", configuration.getSchema()));
                tableSqls.add("SELECT \n" + "    c.relname AS materialized_view_name,\n" + "    COALESCE(d.description, '') AS view_description\n" + "FROM \n" + "    pg_class c\n" + "JOIN \n" + "    pg_namespace n ON c.relnamespace = n.oid\n" + "LEFT JOIN \n" + "    pg_description d ON c.oid = d.objoid\n" + "WHERE \n" + "    c.relkind = 'm' and n.nspname ='SCHEMA';  ".replace("SCHEMA", configuration.getSchema()));
                break;
            case redshift:
                configuration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), CK.class);
                tableSqls.add("SELECT  \n" + "    relname AS TableName,  \n" + "    obj_description(relfilenode::regclass, 'pg_class') AS TableDescription  \n" + "FROM  \n" + "    pg_class  \n" + "WHERE  \n" + "   relkind in  ('r','p', 'f')  \n" + "    AND relnamespace = (SELECT oid FROM pg_namespace WHERE nspname = 'SCHEMA') ".replace("SCHEMA", configuration.getSchema()));
                break;
            case ck:
                configuration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), CK.class);
                if (StringUtils.isEmpty(configuration.getUrlType()) || configuration.getUrlType().equalsIgnoreCase("hostName")) {
                    database = configuration.getDataBase();
                } else {
                    Pattern WITH_SQL_FRAGMENT = Pattern.compile("jdbc:clickhouse://(.*):(\\d+)/(.*)");
                    Matcher matcher = WITH_SQL_FRAGMENT.matcher(configuration.getJdbcUrl());
                    matcher.find();
                    String[] databasePrams = matcher.group(3).split("\\?");
                    database = databasePrams[0];
                }
                if (datasourceRequest.getDsVersion() < 22) {
                    tableSqls.add("SELECT name, name FROM system.tables where database='DATABASE';".replace("DATABASE", database));
                } else {
                    tableSqls.add("SELECT name, comment FROM system.tables where database='DATABASE';".replace("DATABASE", database));
                }


                break;
            default:
                tableSqls.add("show tables");
        }
        return tableSqls;

    }

    private String getSchemaSql(DatasourceDTO datasource) throws DEException {
        DatasourceConfiguration.DatasourceType datasourceType = DatasourceConfiguration.DatasourceType.valueOf(datasource.getType());
        switch (datasourceType) {
            case oracle:
            case obOracle:
                return "select * from all_users";
            case sqlServer:
                return "select name from sys.schemas;";
            case db2:
                DatasourceConfiguration configuration = JsonUtil.parseObject(datasource.getConfiguration(), Db2.class);
                return "select SCHEMANAME from syscat.SCHEMATA   WHERE \"DEFINER\" ='USER'".replace("USER", configuration.getUsername().toUpperCase());
            case pg:
                return "SELECT nspname FROM pg_namespace;";
            case redshift:
                return "SELECT nspname FROM pg_namespace;";
            default:
                return "show tables;";
        }
    }

    public Statement getStatement(Connection connection, int queryTimeout) {
        if (connection == null) {
            DEException.throwException("Failed to get connection!");
        }
        Statement stat = null;
        try {
            stat = connection.createStatement();
            stat.setQueryTimeout(queryTimeout);
        } catch (Exception e) {
            DEException.throwException(e.getMessage());
        }
        return stat;
    }

    public Statement getPreparedStatement(Connection connection, int queryTimeout, String sql, List<TableFieldWithValue> values) throws Exception {
        return getPreparedStatement(connection, queryTimeout, sql, values, null, null);
    }

    public Statement getPreparedStatement(Connection connection, int queryTimeout, String sql, List<TableFieldWithValue> values, String autoIncrementPkName, DatasourceConfiguration datasourceConfiguration) throws Exception {
        if (connection == null) {
            throw new Exception("Failed to get connection!");
        }
        if (CollectionUtils.isNotEmpty(values)) {
            PreparedStatement stat = null;
            String pkName = autoIncrementPkName;
            try {
                if (StringUtils.isNotBlank(autoIncrementPkName)) {
                    String[] generatedColumns = {pkName};
                    stat = connection.prepareStatement(sql, generatedColumns);
                } else {
                    stat = connection.prepareStatement(sql);
                }
                stat.setQueryTimeout(queryTimeout);
            } catch (Exception e) {
                DEException.throwException(e.getMessage());
            }
            return stat;
        } else {
            return getStatement(connection, queryTimeout);
        }
    }

    protected boolean isDefaultClassLoader(String customDriver) {
        return StringUtils.isEmpty(customDriver) || customDriver.equalsIgnoreCase("default");
    }

    protected ExtendedJdbcClassLoader getCustomJdbcClassLoader(CoreDriver coreDriver) {
        if (coreDriver == null) {
            DEException.throwException("Can not found custom Driver");
        }
        ExtendedJdbcClassLoader customJdbcClassLoader = customJdbcClassLoaders.get(coreDriver.getId());
        if (customJdbcClassLoader == null) {
            return addCustomJdbcClassLoader(coreDriver);
        } else {
            if (StringUtils.isNotEmpty(customJdbcClassLoader.getDriver()) && customJdbcClassLoader.getDriver().equalsIgnoreCase(coreDriver.getDriverClass())) {
                return customJdbcClassLoader;
            } else {
                customJdbcClassLoaders.remove(coreDriver.getId());
                return addCustomJdbcClassLoader(coreDriver);
            }
        }
    }

    private synchronized ExtendedJdbcClassLoader addCustomJdbcClassLoader(CoreDriver coreDriver) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        while (classLoader.getParent() != null) {
            classLoader = classLoader.getParent();
            if (classLoader.toString().contains("ExtClassLoader")) {
                break;
            }
        }
        try {
            ExtendedJdbcClassLoader customJdbcClassLoader = new ExtendedJdbcClassLoader(new URL[]{new File(CUSTOM_PATH + coreDriver.getId()).toURI().toURL()}, classLoader);
            customJdbcClassLoader.setDriver(coreDriver.getDriverClass());
            File file = new File(CUSTOM_PATH + coreDriver.getId());
            File[] array = file.listFiles();
            Optional.ofNullable(array).ifPresent(files -> {
                for (File tmp : array) {
                    if (tmp.getName().endsWith(".jar")) {
                        try {
                            customJdbcClassLoader.addFile(tmp);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            customJdbcClassLoaders.put(coreDriver.getId(), customJdbcClassLoader);
            return customJdbcClassLoader;
        } catch (Exception e) {
            DEException.throwException(e.getMessage());
        }
        return null;
    }

    private Connection connection = null;

    public void initConnectionPool() {
        LogUtil.info("Begin to init datasource pool...");
        QueryWrapper<CoreDatasource> datasourceQueryWrapper = new QueryWrapper();
        List<CoreDatasource> coreDatasources = coreDatasourceMapper.selectList(datasourceQueryWrapper).stream()
                .filter(coreDatasource -> !Arrays.asList("folder", "API", "Excel", "ExcelRemote").contains(coreDatasource.getType()))
                .filter(coreDatasource -> Strings.CI.equals(coreDatasource.getStatus(), "Success"))
                .collect(Collectors.toList());
        CoreDatasource engine = engineManage.deEngine();
        if (engine != null) {
            coreDatasources.add(engine);
        }

        for (CoreDatasource coreDatasource : coreDatasources) {
            Map<Long, DatasourceSchemaDTO> dsMap = new HashMap<>();
            DatasourceSchemaDTO datasourceSchemaDTO = new DatasourceSchemaDTO();
            BeanUtils.copyBean(datasourceSchemaDTO, coreDatasource);
            datasourceSchemaDTO.setSchemaAlias(String.format(SQLConstants.SCHEMA, datasourceSchemaDTO.getId()));
            dsMap.put(datasourceSchemaDTO.getId(), datasourceSchemaDTO);
            commonThreadPool.addTask(() -> {
                try {
                    connection = initConnection(dsMap);
                } catch (Exception ignore) {
                }
            });
        }
        LogUtil.info("dsMap size..." + coreDatasources.size());

    }

    public void update(DatasourceDTO datasourceDTO) throws DEException {
        DatasourceSchemaDTO datasourceSchemaDTO = new DatasourceSchemaDTO();
        BeanUtils.copyBean(datasourceSchemaDTO, datasourceDTO);
        datasourceSchemaDTO.setSchemaAlias(String.format(SQLConstants.SCHEMA, datasourceSchemaDTO.getId()));
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDsList(Map.of(datasourceSchemaDTO.getId(), datasourceSchemaDTO));
        try {
            Connection conn = (connection != null) ? connection : getCalciteConnection();
            CalciteConnection calciteConnection = conn.unwrap(CalciteConnection.class);
            SchemaPlus rootSchema = buildSchema(datasourceRequest, calciteConnection);
        } catch (Exception e) {
            DEException.throwException(e.getMessage());
        }
    }

    public void updateDsPoolAfterCheckStatus(DatasourceDTO datasourceDTO) throws DEException {
        DatasourceSchemaDTO datasourceSchemaDTO = new DatasourceSchemaDTO();
        BeanUtils.copyBean(datasourceSchemaDTO, datasourceDTO);
        datasourceSchemaDTO.setSchemaAlias(String.format(SQLConstants.SCHEMA, datasourceSchemaDTO.getId()));
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDsList(Map.of(datasourceSchemaDTO.getId(), datasourceSchemaDTO));
        try {
            CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
            SchemaPlus rootSchema = calciteConnection.getRootSchema();
            if (rootSchema.getSubSchema(datasourceSchemaDTO.getSchemaAlias()) == null) {
                buildSchema(datasourceRequest, calciteConnection);
            }
            DatasourceConfiguration configuration = parseDatasourceConfiguration(datasourceDTO.getConfiguration(), datasourceDTO.getType());
            if (configuration.isUseSSH()) {
                Session session = Provider.getSessions().get(datasourceDTO.getId());
                session.disconnect();
                Provider.getSessions().remove(datasourceDTO.getId());
                startSshSession(configuration, null, datasourceDTO.getId());
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            DEException.throwException(e.getMessage());
        }
    }

    public void delete(CoreDatasource datasource) throws DEException {
        DatasourceSchemaDTO datasourceSchemaDTO = new DatasourceSchemaDTO();
        BeanUtils.copyBean(datasourceSchemaDTO, datasource);
        datasourceSchemaDTO.setSchemaAlias(String.format(SQLConstants.SCHEMA, datasourceSchemaDTO.getId()));
        try {
            Connection conn = (connection != null) ? connection : getCalciteConnection();
            CalciteConnection calciteConnection = conn.unwrap(CalciteConnection.class);
            SchemaPlus rootSchema = calciteConnection.getRootSchema();
            if (rootSchema.getSubSchema(datasourceSchemaDTO.getSchemaAlias()) != null) {
                JdbcSchema jdbcSchema = rootSchema.getSubSchema(datasourceSchemaDTO.getSchemaAlias()).unwrap(JdbcSchema.class);
                BasicDataSource basicDataSource = (BasicDataSource) jdbcSchema.getDataSource();
                basicDataSource.close();
                removeSubSchema(rootSchema, datasourceSchemaDTO.getSchemaAlias());
            }
        } catch (Exception e) {
            DEException.throwException(e.getMessage());
        }
        Provider.getLPorts().remove(datasource.getId());
        if (Provider.getSessions().get(datasource.getId()) != null) {
            Provider.getSessions().get(datasource.getId()).disconnect();
        }
        Provider.getSessions().remove(datasource.getId());
    }

    public Connection take() {
        if (connection == null) { // 第一次检查，无需锁
            synchronized (Connection.class) { // 同步块
                if (connection == null) { // 第二次检查，需要锁
                    connection = getCalciteConnection();
                }
            }
        }
        return connection;
    }

    private void removeSubSchema(SchemaPlus rootSchema, String schemaAlias) {
        CalciteSchema.from(rootSchema).removeSubSchema(schemaAlias);
    }

    private Connection getConnectionFromPool(Long dsId) {
        try {
            Connection connection = take();
            CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
            SchemaPlus rootSchema = calciteConnection.getRootSchema();
            if (rootSchema.getSubSchema(String.format(SQLConstants.SCHEMA, dsId)) == null) {
                DEException.throwException(Translator.get("i18n_check_datasource_connection"));
            }
            JdbcSchema jdbcSchema = rootSchema.getSubSchema(String.format(SQLConstants.SCHEMA, dsId)).unwrap(JdbcSchema.class);
            BasicDataSource basicDataSource = (BasicDataSource) jdbcSchema.getDataSource();
            basicDataSource.setMaxWaitMillis(5 * 1000);
            return basicDataSource.getConnection();
        } catch (DEException e) {
            throw e;
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            DEException.throwException(Translator.get("i18n_invalid_connection") + e.getMessage());
        }
        return null;
    }

    public Map<String, Object> fetchResultField(EngineRequest engineRequest) throws Exception {
        DatasourceConfiguration configuration = JsonUtil.parseObject(engineRequest.getEngine().getConfiguration(), DatasourceConfiguration.class);
        int queryTimeout = configuration.getQueryTimeout();
        DatasourceDTO datasource = new DatasourceDTO();
        BeanUtils.copyBean(datasource, engineRequest.getEngine());
        try (Connection connection = getConnectionFromPool(datasource.getId());
             PreparedStatement preparedStatement = connection.prepareStatement(engineRequest.getQuery())) {
            preparedStatement.setQueryTimeout(queryTimeout);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("fields", fetchResultField(resultSet));
                map.put("data", getDataResult(resultSet));
                return map;
            }
        }
    }

    public void exec(EngineRequest engineRequest) throws Exception {
        DatasourceConfiguration configuration = JsonUtil.parseObject(engineRequest.getEngine().getConfiguration(), DatasourceConfiguration.class);
        int queryTimeout = configuration.getQueryTimeout();
        DatasourceDTO datasource = new DatasourceDTO();
        BeanUtils.copyBean(datasource, engineRequest.getEngine());
        try (Connection connection = getConnectionFromPool(datasource.getId()); Statement stat = getStatement(connection, queryTimeout)) {
            PreparedStatement preparedStatement = connection.prepareStatement(engineRequest.getQuery());
            preparedStatement.setQueryTimeout(queryTimeout);
            Boolean result = preparedStatement.execute();
        } catch (Exception e) {
            throw e;
        }
    }

    public void execWithEngineTransaction(EngineRequest engineRequest, EngineConnectionExecutor executor) throws Exception {
        DatasourceConfiguration configuration = JsonUtil.parseObject(engineRequest.getEngine().getConfiguration(), DatasourceConfiguration.class);
        int queryTimeout = configuration.getQueryTimeout();
        DatasourceDTO datasource = new DatasourceDTO();
        BeanUtils.copyBean(datasource, engineRequest.getEngine());
        try (Connection connection = getConnectionFromPool(datasource.getId())) {
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                executor.execute(connection, queryTimeout);
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(autoCommit);
            }
        }
    }

    @FunctionalInterface
    public interface EngineConnectionExecutor {
        void execute(Connection connection, int queryTimeout) throws Exception;
    }
}
