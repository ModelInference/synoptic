package synopticgwt.server.table;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ParseLogAction {
    private static String CREATE_QUERY = "CREATE TABLE ParseLogAction (vid INT, timestamp TIMESTAMP, parseid INT GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), result VARCHAR(255))";
	    
    private Connection conn;
    private Statement stmt;
		    
    public ParseLogAction(Connection conn, Statement stmt) {
    	this.conn = conn;
    	this.stmt = stmt;
    }
    
    /**
     * Create query in database.
     */
    public void createTable() throws SQLException {
        stmt = conn.createStatement();
        stmt.execute(CREATE_QUERY);
        stmt.close();
    }
    
    public int insert(int vID, int time, int parseResult) throws SQLException {	
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
}
