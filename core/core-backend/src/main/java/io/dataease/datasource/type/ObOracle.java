package io.dataease.datasource.type;

import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Data
@Component("obOracle")
public class ObOracle extends DatasourceConfiguration {
    private String driver = "com.oceanbase.jdbc.Driver";
    private String extraParams = "";
    private static final List<String> ILLEGAL_PARAMETERS = Arrays.asList(
            "java.naming.factory.initial", "java.naming.provider.url", "rmi",
            "ldap://", "ldaps://", "java.naming.factory.object", "java.naming.factory.state",
            "autoDeserialize", "connectionProperties", "initSQL", "dns", "file", "ftp"
    );

    public String getJdbc() {
        if (StringUtils.isNoneEmpty(getUrlType()) && !StringUtils.equalsIgnoreCase(getUrlType(), "hostName")) {
            if (!StringUtils.startsWithIgnoreCase(getJdbcUrl(), "jdbc:oceanbase://")) {
                DEException.throwException("Illegal jdbcUrl: " + getJdbcUrl());
            }
            validateIllegalParameters(getJdbcUrl());
            return getJdbcUrl();
        }

        String jdbcUrl = "jdbc:oceanbase://HOSTNAME:PORT/DATABASE"
                .replace("HOSTNAME", getLHost().trim())
                .replace("PORT", getLPort().toString().trim())
                .replace("DATABASE", getDataBase().trim());
        if (StringUtils.isNotBlank(getExtraParams())) {
            jdbcUrl = jdbcUrl + "?" + getExtraParams().trim();
        }
        validateIllegalParameters(jdbcUrl);
        return jdbcUrl;
    }

    private void validateIllegalParameters(String value) {
        String decodedValue = URLDecoder.decode(StringUtils.defaultString(value), StandardCharsets.UTF_8);
        for (String illegalParameter : ILLEGAL_PARAMETERS) {
            if (StringUtils.containsIgnoreCase(decodedValue, illegalParameter)) {
                DEException.throwException("Illegal parameter: " + illegalParameter);
            }
        }
    }
}
