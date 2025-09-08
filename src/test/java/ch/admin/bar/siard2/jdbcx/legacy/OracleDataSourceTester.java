package ch.admin.bar.siard2.jdbcx.legacy;

import ch.admin.bar.siard2.jdbcx.OracleDataSource;
import org.junit.*;
import org.testcontainers.containers.OracleContainer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.Assert.assertSame;

public class OracleDataSourceTester {

    @ClassRule
    public final static OracleContainer db = new OracleContainer("gvenzl/oracle-xe:21-slim-faststart");

    private static final String DB_USER = "SYSTEM";

    private static final String DB_PASSOWRD = "test";

    private OracleDataSource oracleDataSource = null;
    private Connection connection = null;

    @Before
    public void setUp() throws SQLException {
        oracleDataSource = new OracleDataSource();
    }

    @After
    public void tearDown() throws SQLException {
        if ((connection != null) && (!connection.isClosed())) {
            connection.close();
        }
    }

    @Test
    public void testConnection() throws SQLException {
        oracleDataSource.setUser(DB_USER);
        oracleDataSource.setPassword(DB_PASSOWRD);
        oracleDataSource.setUrl(db.getJdbcUrl());
        connection = oracleDataSource.getConnection();
    }

    @Test
    public void testWrapper() throws SQLException {
        Assert.assertSame("Invalid wrapper!", true, oracleDataSource.isWrapperFor(DataSource.class));
        DataSource dsWrapped = oracleDataSource.unwrap(DataSource.class);
        assertSame("Invalid wrapped class!", oracle.jdbc.pool.OracleDataSource.class, dsWrapped.getClass());
    }


    @Test
    public void testLoginTimeout() throws SQLException {
        int iLoginTimeout = oracleDataSource.getLoginTimeout();
        assertSame("Unexpected login timeout " + String.valueOf(iLoginTimeout) + "!", 0, iLoginTimeout);
    }
}
