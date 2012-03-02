package synopticgwt.server.table;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

//TODO comment
public class ReExp extends DerbyTable {
    private static String CREATE_QUERY = "CREATE TABLE ReExp (reid INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), text CLOB, hash VARCHAR(32))";
    
    public ReExp(Connection conn, Statement stmt) {
    	super(conn, stmt);
    }
     
    /**
     * TODO comment
     * @param ipAddress
     * @param time
     * @throws SQLException 
     */
    public int insert(String text, String hash) throws SQLException {
    	// Clean String for single quotes.
    	String cleanString = text.replace("'", "''");
    	
    	stmt = conn.createStatement();
        stmt.executeUpdate("insert into ReExp(text, hash) values('"
                + cleanString 
                + "', '" 
                + hash 
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
  	 *TODO comment
     * 
     * @param String
     *            query to select a specific row
     * @return int returns first column value of row
     */
    public int getIdExistingHash(String hash) throws SQLException {
        int result = -1;

        stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("select * from  ReExp where hash = '" 
        		+ hash 
        		+ "'");

        while (rs.next()) {
            result = rs.getInt(1);
        }

        rs.close();
        stmt.close();

        return result;
    }
}
