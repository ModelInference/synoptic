package synopticgwt.server.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

/**
 * Visitor table. Schema: vid, IP, timestamp
 */
public class VisitorTable extends DerbyTable {

    public VisitorTable(Connection conn) {
        super(
                conn,
                "CREATE TABLE Visitor (vid INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), IP VARCHAR(15), timestamp TIMESTAMP)");
    }

    /**
     * Inserts ipAddress and time into the table. Returns id of auto-incremented
     * field if exists. Else, return -1.
     * 
     * @param ipAddress
     * @param time
     * @throws SQLException
     */
    public int insert(String ipAddress, Timestamp time) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("insert into Visitor(IP, timestamp) values('"
                + ipAddress + "', '" + time + "')",
                Statement.RETURN_GENERATED_KEYS);
        int result = -1;

        ResultSet rs = stmt.getGeneratedKeys();

        while (rs.next()) {
            result = rs.getInt(1);
        }

        rs.close();
        stmt.close();

        return result;
    }

    /**
     * Returns ResultSet of "SELECT * from Visitor" query. Note: must call
     * close() on ResultSet after done using it.
     * 
     * @param field
     * @param value
     * @return ResultSet of query
     * @throws SQLException
     */
    public ResultSet getSelect() throws SQLException {
        Statement stmt = conn.createStatement();
        String q = "select * from Visitor";
        ResultSet rs = stmt.executeQuery(q);
        return rs;
    }
}
