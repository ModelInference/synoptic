package synopticgwt.server.table;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DerbyTable {
		protected String CREATE_QUERY = "";
	
	 	protected Connection conn;
	    protected Statement stmt;
	    
	    public DerbyTable(Connection conn, Statement stmt) {
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
}
