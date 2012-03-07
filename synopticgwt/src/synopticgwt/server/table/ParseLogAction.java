package synopticgwt.server.table;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

/**
 * ParseLogAction table.
 * Schema: vid, timestamp, parseid, result
 */
public class ParseLogAction extends DerbyTable {
    protected String CREATE_QUERY = "CREATE TABLE ParseLogAction (vid INT, timestamp TIMESTAMP, parseid INT GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), result VARCHAR(255))";
		    
    public ParseLogAction(Connection conn, Statement stmt) {
    	super(conn, stmt);
    }
    
    /**
     * Create query in database.
     */
    public void createTable() throws SQLException {
        stmt = conn.createStatement();
        stmt.execute(CREATE_QUERY);
        stmt.close();
    }
    
    /**
     * Inserts vid, timestamp, and parse result into the table. Returns id 
     * of auto-incremented field if exists. Else, return -1.
     * @param vID
     * @param time
     * @param parseResult
     * @throws SQLException
     */
    public int insert(int vID, Timestamp time, String parseResult) throws SQLException {
    	stmt = conn.createStatement();
        stmt.executeUpdate("insert into ParseLogAction(vid, timestamp, result) values("
	            + vID
	            + ", '"
	            + time
	            + "', '"
	            + parseResult 
	            + "')",
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
     * Returns ResultSet of "SELECT * from ParseLogAction" query.
     * Note: must call close() on ResultSet after done using it.
     * @param field
     * @param value
     * @return ResultSet of query
     * @throws SQLException
     */
    public ResultSet getSelect() 
    			throws SQLException {        
        stmt = conn.createStatement();     
        String q = "select * from ParseLogAction";        
        ResultSet rs = stmt.executeQuery(q);      
        return rs;
    }
}
