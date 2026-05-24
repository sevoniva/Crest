package io.crest.datasource.type;

import io.crest.exception.DEException;
import io.crest.extensions.datasource.vo.DatasourceConfiguration;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Data
@EqualsAndHashCode(callSuper = false)
@Component("obOracle")
@SuppressWarnings("deprecation")
public class ObOracle extends DatasourceConfiguration {
    private String driver = "com.oceanbase.jdbc.Driver";
    private String extraParams = "";
    private Boolean readOnly = true;
    private static final List<String> ILLEGAL_PARAMETERS = Arrays.asList(
            "java.naming.factory.initial", "java.naming.provider.url", "rmi",
            "ldap://", "ldaps://", "java.naming.factory.object", "java.naming.factory.state",
            "autoDeserialize", "connectionProperties", "initSQL", "dns", "file", "ftp"
    );

    public String getJdbc() {
        if (StringUtils.isNoneEmpty(getUrlType()) && !Strings.CI.equals(getUrlType(), "hostName")) {
            if (!Strings.CI.startsWith(getJdbcUrl(), "jdbc:oceanbase://")) {
                DEException.throwException("Illegal jdbcUrl: " + getJdbcUrl());
            }
            validateIllegalParameters(getJdbcUrl());
            return getJdbcUrl();
        }

        String database = StringUtils.defaultString(getDataBase()).trim();
        String jdbcUrl = "jdbc:oceanbase://HOSTNAME:PORT/DATABASE"
                .replace("HOSTNAME", getLHost().trim())
                .replace("PORT", getLPort().toString().trim())
                .replace("DATABASE", database);
        if (StringUtils.isNotBlank(getExtraParams())) {
            jdbcUrl = jdbcUrl + "?" + getExtraParams().trim();
        }
        validateIllegalParameters(jdbcUrl);
        return jdbcUrl;
    }

    @Override
    public String getSchema() {
        String schema = super.getSchema();
        if (StringUtils.isNotBlank(schema)) {
            return schema.trim();
        }
        String username = StringUtils.defaultString(getUsername()).trim();
        if (StringUtils.isBlank(username)) {
            return schema;
        }
        int atIndex = username.indexOf("@");
        int clusterIndex = username.indexOf("#");
        int endIndex = username.length();
        if (atIndex >= 0) {
            endIndex = Math.min(endIndex, atIndex);
        }
        if (clusterIndex >= 0) {
            endIndex = Math.min(endIndex, clusterIndex);
        }
        return username.substring(0, endIndex).toUpperCase(Locale.ROOT);
    }

    private void validateIllegalParameters(String value) {
        String decodedValue = URLDecoder.decode(StringUtils.defaultString(value), StandardCharsets.UTF_8);
        for (String illegalParameter : ILLEGAL_PARAMETERS) {
            if (Strings.CI.contains(decodedValue, illegalParameter)) {
                DEException.throwException("Illegal parameter: " + illegalParameter);
            }
        }
    }
}
