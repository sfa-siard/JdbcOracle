package ch.admin.bar.siard2.jdbc;

import ch.admin.bar.siard2.ConsoleLogConsumer;
import ch.admin.bar.siard2.OracleDatasourceFactory;
import ch.admin.bar.siard2.SqlScripts;
import ch.admin.bar.siard2.TestResourcesResolver;
import lombok.val;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.utility.MountableFile;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class OracleDatabaseMetaData_getTables_Test {

    static OracleContainer db = new OracleContainer("gvenzl/oracle-xe:21-slim-faststart")
            .withLogConsumer(new ConsoleLogConsumer())
            .withCopyFileToContainer(MountableFile.forHostPath(TestResourcesResolver.resolve(SqlScripts.MULTIPLE_USERS)
                                                                                    .toPath()), "/container-entrypoint-initdb.d/00_create_schemas.sql");

    private static OracleDatabaseMetaData oracleDatabaseMetaData;

    @BeforeAll
    static void beforeAll() throws SQLException {
        db.start();
        val oracleDataSource = new OracleDatasourceFactory().create(db, "user_a", "password_a");
        val connection = (OracleConnection) oracleDataSource.getConnection();
        connection.setAutoCommit(false);
        oracleDatabaseMetaData = (OracleDatabaseMetaData) connection.getMetaData();
    }

    @AfterAll
    static void afterAll() {
        db.stop();
    }

    @Test
    void get_number_of_rows_from_getTables_query() throws SQLException {
        ResultSet tableResultSet = oracleDatabaseMetaData.getTables(null, "%", "%", new String[]{"TABLE"});
        int i = countTables(tableResultSet);
        assertEquals(2, i);
    }
    @Test
    void get_tables() throws SQLException {
        ResultSet resultSet = oracleDatabaseMetaData.getTables(null, "%", "%", new String[]{"TABLE"});

        resultSet.next();
        assertEquals("TABLE_A", resultSet.getString("TABLE_NAME"));
        assertEquals("USER_A", resultSet.getString("TABLE_SCHEM"));
        assertEquals("TABLE", resultSet.getString("TABLE_TYPE"));

        resultSet.next();
        assertEquals("TABLE_B", resultSet.getString("TABLE_NAME"));
        assertEquals("USER_B", resultSet.getString("TABLE_SCHEM"));
        assertEquals("TABLE", resultSet.getString("TABLE_TYPE"));
    }

    private int countTables(ResultSet tableResultSet) throws SQLException {
        // stupid way of doing this... should be done using a single query
        int i = 0;
        while (tableResultSet.next()) i++;
        tableResultSet.close();
        return i;
    }

}