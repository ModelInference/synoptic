package synopticgwt.server.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.NoSuchElementException;

// TODO comment
public class ReExpTable extends DerbyTable {

    public ReExpTable(Connection conn) {
        super(
                conn,
                "CREATE TABLE ReExp (reid INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), text CLOB, hash VARCHAR(32))");
    }

    /**
     * Inserts text and hash into the table. Returns id of auto-incremented
     * field if exists. Else, return -1.
     * 
     * @param text
     * @param hash
     * @throws SQLException
     */
    public int insert(String text, String hash) throws SQLException {
        // Clean String for single quotes.
        String cleanString = text.replace("'", "''");

        Statement stmt = conn.createStatement();
        stmt.executeUpdate("insert into ReExp(text, hash) values('"
                + cleanString + "', '" + hash + "')",
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
     * If hash exists in table already, return the id of that row. Else, throw
     * NoSuchElementException if hash doesn't exist in table.
     * 
     * @param hash
     * @throws SQLException
     */
    public int getIdExistingHash(String hash) throws SQLException,
            NoSuchElementException {
        int result = -1;

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("select * from ReExp where hash = '"
                + hash + "'");

        while (rs.next()) {
            result = rs.getInt(1);
        }

        if (result == -1) {
            throw new NoSuchElementException("Hash doesn't exist in "
                    + "ReExp table");
        }

        rs.close();
        stmt.close();

        return result;
    }

    /**
     * Returns ResultSet of "SELECT * from ReExp" query. Note: must call close()
     * on ResultSet after done using it.
     * 
     * @param field
     * @param value
     * @return ResultSet of query
     * @throws SQLException
     */
    public ResultSet getSelect() throws SQLException {
        Statement stmt = conn.createStatement();
        String q = "select * from ReExp";
        ResultSet rs = stmt.executeQuery(q);
        return rs;
    }
}
