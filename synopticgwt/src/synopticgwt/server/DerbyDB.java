package synopticgwt.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;


/**
 * Derby database.
 */
public class DerbyDB {
    private static DerbyDB instance;
        
    public static Logger logger = Logger.getLogger("DerbyDB");
    
    // The driver to use.
    private static String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    
    // Various tables.
    private static String VISITOR = "CREATE TABLE Visitor (vid INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), IP VARCHAR(15), timestamp TIMESTAMP)";
    private static String UPLOADED_LOG = "CREATE TABLE UploadedLog (logid INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), text CLOB, hash VARCHAR(32))";
    private static String RE_EXP = "CREATE TABLE ReExp (reid INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), text CLOB, hash VARCHAR(255))";
    private static String LOG_RE_EXP = "CREATE TABLE LogReExp (reid INT PRIMARY KEY, text CLOB, hash VARCHAR(255))";
    private static String SPLIT_RE_EXP = "CREATE TABLE SplitReExp (reid INT PRIMARY KEY, text CLOB, hash VARCHAR(255))";
    private static String PARTITION_RE_EXP = "CREATE TABLE PartitionReExp (reid INT PRIMARY KEY, text CLOB, hash VARCHAR(255))";
    private static String PARSE_LOG_ACTION = "CREATE TABLE ParseLogAction (vid INT PRIMARY KEY, timestamp TIMESTAMP, parseid INT GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), result VARCHAR(255))";

    // The Derby connection URL.
    private String connectionURL;

    private Connection conn = null;
    private Statement stmt = null;
    
    private DerbyDB(String path, boolean isCreate) {
      connectionURL = "jdbc:derby:" + path;
      createConnection(isCreate);
      if (isCreate) {
          createAllTables();
      }
    }
    
    public static DerbyDB getInstance(String path, boolean isCreate) {
        if (instance != null) {
            return instance;
        }
        return new DerbyDB(path, isCreate);
    }
    
    /**
     * Establishes a connection with the database.
     * @param isCreate whether or not to create a new database
     */
    private void createConnection(boolean isCreate) {
        try {
            Class.forName(driver).newInstance();
            //Get a connection
            conn = DriverManager.getConnection(connectionURL + ";create=" + isCreate);
            logger.info("Connecting to Derby database.");
        } catch (Exception except) {
             except.printStackTrace();
        }
    }
    
    /**
     * Create all the tables in the database.
     */
    private void createAllTables() {    
        createQuery(VISITOR);
        createQuery(UPLOADED_LOG);
        createQuery(RE_EXP);
        createQuery(LOG_RE_EXP);
        createQuery(SPLIT_RE_EXP);
        createQuery(PARTITION_RE_EXP);
        createQuery(PARSE_LOG_ACTION);
    }
    
    /**
     * Executes a create query in database.
     */
    public void createQuery(String query) {
        try {
            stmt = conn.createStatement();
            stmt.execute(query);
            stmt.close();
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
    }
    
    /**
     * Executes an update query in database.
     */
    public void updateQuery(String query) {
        try {
            stmt = conn.createStatement();
            int n = stmt.executeUpdate(query);
            stmt.close();
            logger.info("Inserted " + n + " row(s) into Derby database.");
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
    }
    
    /**
     * Returns -1 if the row doesn't exist in database for select
     * query. Returns the first column (usually id) of row if it does.
     */
    public int getIdExistingRow(String query) {
        int result = -1;
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                result = rs.getInt(1);
            }        
            
            rs.close();
            stmt.close();
        } catch (Exception e) {
            
        }
        return result;
    }
    
    /**
     * Executes an INSERT query and returns auto incrementing identity field assigned 
     * to newly created record.
     * @param query
     */
    public int insertAndGetAutoValue(String query) {
        int result = 0;
        try {
            stmt = conn.createStatement();
            int n = stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
            logger.info("Inserted " + n + " row(s) into Derby database.");

            ResultSet rs = stmt.getGeneratedKeys();
            
            while (rs.next()) {
                result = rs.getInt(1);
            }

            rs.close();
            stmt.close();
            
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        return result;
    }
    
    /*public String getStringColumn(String query, String column) {
        String result = "";
        try {
            stmt = conn.createStatement();
            ResultSet results = stmt.executeQuery(query);
            results.
            while(results.next()) {
                results.getString(column);
            }
            results.close();
            stmt.close();
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        return result;
    }*/
    
    /**
     * Shutdown the database.
     */
    public void shutdown() {
        try {
            if (stmt != null) {
                stmt.close();
            }
            if (conn != null) {
                DriverManager.getConnection(connectionURL + ";shutdown=true");
                conn.close();
                logger.info("Shutting down Derby database");
            }           
        } catch (SQLException sqlExcept) {  
            sqlExcept.printStackTrace();
        }
    }
}
