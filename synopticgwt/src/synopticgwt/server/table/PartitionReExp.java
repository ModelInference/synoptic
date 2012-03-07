package synopticgwt.server.table;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * PartitionReExp table.
 * Schema: parseid, reid, logid
 */
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
    
    /**
     * Executes an INSERT query and returns auto incrementing identity field
     * assigned to newly created record. Returns -1 if incrementing identity
     * field doesn't exist.
     * @param parseID
     * @param reID
     * @param logID
     * @return row id of incrementing identity field from insert, -1 otherwise
     * @throws SQLException
     */
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
    
    /**
     * Returns ResultSet of "SELECT * from PartitionReExp" query.
     * Note: must call close() on ResultSet after done using it.
     * @param field
     * @param value
     * @return ResultSet of query
     * @throws SQLException
     */
    public ResultSet getSelect() 
    			throws SQLException {        
        stmt = conn.createStatement();      
        String q = "select * from PartitionReExp";
        ResultSet rs = stmt.executeQuery(q);   
        return rs;
    }
}
