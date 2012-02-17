package synopticgwt.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;

public class DerbyDB extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static DerbyDB instance;
    
    private static AppConfiguration config;
    
    // The driver to use.
    private static String driver = "org.apache.derby.jdbc.EmbeddedDriver";
       
    // The database name along with its location on disk.
    //private static String dbName="/Users/Kevin/Desktop/DerbyTutorials/synoptictest";
    
    // Various tables.
    private static String VISITOR = "CREATE TABLE Visitor (vid INT PRIMARY KEY, IP INT, timestamp TIMESTAMP)";
    private static String UPLOADED_LOG = "CREATE TABLE UploadedLog (logid INT PRIMARY KEY, text CLOB, hash VARCHAR(32))";
    private static String RE_EXP = "CREATE TABLE ReExp (reid INT PRIMARY KEY, text CLOB, hash VARCHAR(255))";
    private static String LOG_RE_EXP = "CREATE TABLE LogReExp (reid INT PRIMARY KEY, text CLOB, hash VARCHAR(255))";
    private static String SPLIT_RE_EXP = "CREATE TABLE SplitReExp (reid INT PRIMARY KEY, text CLOB, hash VARCHAR(255))";
    private static String PARTITION_RE_EXP = "CREATE TABLE PartitionReExp (reid INT PRIMARY KEY, text CLOB, hash VARCHAR(255))";
    private static String PARSE_LOG_ACTION = "CREATE TABLE ParseLogAction (vid INT PRIMARY KEY, timestamp TIMESTAMP, parseid INT, result VARCHAR(255))";

    // The Derby connection URL.
    private static String connectionURL = "jdbc:derby:" + config.derbyDBDir;

    private static Connection conn = null;
    private static Statement stmt = null;
    
    private DerbyDB() {
        ServletContext context = getServletConfig().getServletContext();
        DerbyDB.config = AppConfiguration.getInstance(context);
        if (config.derbyDBExists) {
            createConnection(false); // don't create a new database.
        } else {
            createConnection(true);
        }
    }
    
    public static DerbyDB getInstance() {
        if (instance != null) {
            return instance;
        }
        return new DerbyDB();
    }

    private static void createConnection(boolean isCreate) {
        try {
            Class.forName(driver).newInstance();
            //Get a connection
            conn = DriverManager.getConnection(connectionURL + ";create=" + isCreate); 
        } catch (Exception except) {
             except.printStackTrace();
        }
    }
    
    public static void createAllTables() {    
        createTable(VISITOR);
        createTable(UPLOADED_LOG);
        createTable(RE_EXP);
        createTable(LOG_RE_EXP);
        createTable(SPLIT_RE_EXP);
        createTable(PARTITION_RE_EXP);
        createTable(PARSE_LOG_ACTION);
    }
    
    private static void createTable(String query) {
        try {
            stmt = conn.createStatement();
            stmt.execute(query);
            stmt.close();
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
    }
    
    public static void shutdown() {
        try {
            if (stmt != null) {
                stmt.close();
            }
            if (conn != null) {
                DriverManager.getConnection(connectionURL + ";shutdown=true");
                conn.close();
            }           
        } catch (SQLException sqlExcept) {  
        }

    }
  

}
