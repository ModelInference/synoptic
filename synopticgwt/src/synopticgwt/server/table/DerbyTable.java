package synopticgwt.server.table;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class DerbyTable {
	
	 	protected Connection conn;
	    protected Statement stmt;
	    
	    public DerbyTable(Connection conn, Statement stmt) {
	    	this.conn = conn;
	    	this.stmt = stmt;
	    }
	    
	    public abstract void createTable() throws SQLException;
}
