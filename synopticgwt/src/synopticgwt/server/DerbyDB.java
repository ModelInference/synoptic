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
    private static String VISITOR = "Visitor";
    private static String UPLOADED_LOG = "UploadedLog";
    private static String RE_EXP = "ReExp";
    private static String LOG_RE_EXP = "LogReExp";
    private static String SPLIT_RE_EXP = "SplitReExp";
    private static String PARTITION_RE_EXP = "PartitionReExp";
    private static String PARSE_LOG_ACTION = "ParseLogAction";

    // The Derby connection URL.
    private static String connectionURL = "jdbc:derby:" + config.derbyDBDir;

    private static Connection conn = null;
    private static Statement stmt = null;

    /*public static void main(String[] args) {
        createConnection();
        createAllTables();
    }*/

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
        createVisitorTable();
        createUploadedLogTable();
        createReExpTable();
        createReExpTable(LOG_RE_EXP);
        createReExpTable(SPLIT_RE_EXP);
        createReExpTable(PARTITION_RE_EXP);
        createParseLogActionTable();
    }
    
    private static void createVisitorTable() {
        try {
            stmt = conn.createStatement();
            stmt.execute("CREATE TABLE " + VISITOR + " (vid INT PRIMARY KEY, IP INT, timestamp TIMESTAMP)");
            stmt.close();
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
    }
    
    private static void createUploadedLogTable() {
        try {
            stmt = conn.createStatement();
            stmt.execute("CREATE TABLE " + UPLOADED_LOG + " (logid INT PRIMARY KEY, text CLOB, hash VARCHAR(32))");
            stmt.close();
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
    }
    
    private static void createReExpTable() {
        try {
            stmt = conn.createStatement();
            stmt.execute("CREATE TABLE " + RE_EXP + " (reid INT PRIMARY KEY, text CLOB, hash VARCHAR(255))");
            stmt.close();
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
    }
    
    private static void createReExpTable(String tableName) {
        try {
            stmt = conn.createStatement();
            stmt.execute("CREATE TABLE " + tableName + " (parseid INT PRIMARY KEY,  reid INT, logid INT)");
            stmt.close();
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
    }
    
    private static void createParseLogActionTable() {
        try {
            stmt = conn.createStatement();
            stmt.execute("CREATE TABLE " + PARSE_LOG_ACTION + " (vid INT PRIMARY KEY, timestamp TIMESTAMP, parseid INT, result VARCHAR(255))");
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

}
