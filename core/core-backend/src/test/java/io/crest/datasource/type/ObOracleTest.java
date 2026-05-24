package io.crest.datasource.type;

import io.crest.exception.DEException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class ObOracleTest {

    @Test
    public void readOnlyDefaultsToEnabled() {
        ObOracle obOracle = new ObOracle();

        assertTrue(obOracle.getReadOnly());
    }

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
    public void getJdbcAllowsEmptyDatabaseForCurrentSchemaConnections() {
        ObOracle obOracle = new ObOracle();
        obOracle.setHost("127.0.0.1");
        obOracle.setPort(2881);

        assertEquals(
                "jdbc:oceanbase://127.0.0.1:2881/",
                obOracle.getJdbc()
        );
    }

    @Test
    public void getSchemaDefaultsToUppercaseUsernameBeforeTenant() {
        ObOracle obOracle = new ObOracle();
        obOracle.setUsername("test@obora");

        assertEquals("TEST", obOracle.getSchema());
    }

    @Test
    public void getSchemaDefaultsToUppercaseUsernameBeforeTenantAndCluster() {
        ObOracle obOracle = new ObOracle();
        obOracle.setUsername("test@obora#obdemo");

        assertEquals("TEST", obOracle.getSchema());
    }

    @Test
    public void getSchemaKeepsExplicitSchema() {
        ObOracle obOracle = new ObOracle();
        obOracle.setUsername("test@obora");
        obOracle.setSchema("CUSTOM_SCHEMA");

        assertEquals("CUSTOM_SCHEMA", obOracle.getSchema());
    }

    @Test
    public void getJdbcRejectsNonOceanBaseJdbcUrl() {
        ObOracle obOracle = new ObOracle();
        obOracle.setUrlType("jdbcUrl");
        obOracle.setJdbcUrl("jdbc:oracle:thin:@localhost:1521:ORCL");

        assertThrows(DEException.class, obOracle::getJdbc);
    }
}
