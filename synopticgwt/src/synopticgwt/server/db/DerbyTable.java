package synopticgwt.server.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Base type for all Derby table classes.
 */
public abstract class DerbyTable {
    // The query to use when creating the table.
    protected String createQuery = null;

    // The underlying database connection.
    protected Connection conn;

    public DerbyTable(Connection conn, String createQuery) {
        this.conn = conn;
        this.createQuery = createQuery;
    }

    /**
     * Create the table in database connected to.
     */
    public void createTable() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute(createQuery);
        stmt.close();
    }
}
