package synopticgwt.server.table;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PartitionReExp extends DerbyTable {
    protected String CREATE_QUERY = "CREATE TABLE PartitionReExp (parseid INT, reid INT, logid INT)";

    public PartitionReExp(Connection conn, Statement stmt) {
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
    
    public int insert(int parseID, int reID, int logID) throws SQLException {	
    	stmt = conn.createStatement();
        stmt.executeUpdate("insert into PartitionReExp(parseid, reid, logid) values("
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
