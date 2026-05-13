package io.dataease.datasource.type;

import io.dataease.exception.DEException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class ObOracleTest {

    @Test
    public void getJdbcBuildsOceanBaseUrlFromHostPortAndDatabase() {
        ObOracle obOracle = new ObOracle();
        obOracle.setHost("obproxy.example.com");
        obOracle.setPort(2883);
        obOracle.setDataBase("ORCL");
        obOracle.setExtraParams("connectTimeout=5000");

        assertEquals(
                "jdbc:oceanbase://obproxy.example.com:2883/ORCL?connectTimeout=5000",
                obOracle.getJdbc()
        );
    }

    @Test
    public void getJdbcRejectsNonOceanBaseJdbcUrl() {
        ObOracle obOracle = new ObOracle();
        obOracle.setUrlType("jdbcUrl");
        obOracle.setJdbcUrl("jdbc:oracle:thin:@localhost:1521:ORCL");

        assertThrows(DEException.class, obOracle::getJdbc);
    }
}
