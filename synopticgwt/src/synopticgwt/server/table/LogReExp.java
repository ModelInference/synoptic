package synopticgwt.server.table;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class LogReExp extends DerbyTable {
    protected String CREATE_QUERY = "CREATE TABLE LogReExp (parseid INT, reid INT, logid INT)";
        
    public LogReExp(Connection conn, Statement stmt) {
    	super(conn, stmt);
    }
    
    /**
     * Create the table in database connected to.
     */
    public void createTable() throws SQLException {
        stmt = conn.createStatement();
        stmt.execute(CREATE_QUERY);
        stmt.close();
    }
    
    /**
     * Executes an INSERT query and returns auto incrementing identity field
     * assigned to newly created record. Returns 0 if incrementing identity
     * field doesn't exist.
     * 
     * @param query
     *            the insert query to use
     */
    public int insert(int parseID, int reID, int logID) throws SQLException {	
    	stmt = conn.createStatement();
        stmt.executeUpdate("insert into LogReExp(parseid, reid, logid) values("
	            + parseID
	            + ", "
	            + reID
	            + ", "
	            + logID 
	            + ")",
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
}
