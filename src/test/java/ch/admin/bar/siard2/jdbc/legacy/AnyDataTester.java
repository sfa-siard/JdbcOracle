package ch.admin.bar.siard2.jdbc.legacy;

import ch.admin.bar.siard2.jdbcx.OracleDataSource;
import ch.enterag.utils.jdbc.BaseDatabaseMetaDataTester;
import org.junit.*;
import org.testcontainers.containers.OracleContainer;

import java.sql.*;

public class AnyDataTester {
    private Connection connection;

    @ClassRule
    public static final OracleContainer db = new OracleContainer("gvenzl/oracle-xe:21-slim-faststart");


    @Before
    public void setUp() throws SQLException {
        System.setProperty("oracle.net.networkStatistics", "false");
        // Disable FAN and MBean registration to avoid background timers/threads
        System.setProperty("oracle.jdbc.fanEnabled", "false");
        System.setProperty("oracle.jdbc.disableMBeanRegistration", "true");
        OracleDataSource dsOracle = new OracleDataSource();
        dsOracle.setUrl(db.getJdbcUrl());
        dsOracle.setUser(db.getUsername());
        dsOracle.setPassword(db.getPassword());
        connection = dsOracle.getConnection();
        connection.setAutoCommit(false);
        System.out.println("JDBC URL: " + db.getJdbcUrl());
    }


    @Test
    public void testGetColumns() throws SQLException {
        DatabaseMetaData dmd = connection.getMetaData();
        ResultSet rs = dmd.getColumns(null, db.getUsername(), "IFS_IN_TABLE", "%");
        BaseDatabaseMetaDataTester.print(rs);
    }

    @Test
    @Ignore("seems to depend on setup @enterag")
    public void testGetObject() throws SQLException {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT USER_PROP FROM IFS_IN_TABLE");
        while (rs.next()) {
            Object o = rs.getObject(1);
            System.out.println(String.valueOf(o));
        }
    }

    @Test
    @Ignore("seems to depend on setup @enterag")
    public void testGetClob() throws SQLException {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT USER_PROP FROM IFS_IN_TABLE");
        while (rs.next()) {
            Clob clob = rs.getClob(1);
            System.out.println(clob.getSubString(1, (int) clob.length()));
        }
    }
}
