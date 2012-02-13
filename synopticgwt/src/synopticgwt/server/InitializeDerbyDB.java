package synopticgwt.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class InitializeDerbyDB {
    // The driver to use.
    private static String driver = "org.apache.derby.jdbc.EmbeddedDriver";
       
    // The database name along with its location on disk.
    private static String dbName="/Users/Kevin/Desktop/DerbyTutorials/synoptictest";
    
    // Various tables.
    private static String VISITOR = "Visitor";
    private static String UPLOADED_LOG = "UploadedLog";
    private static String RE_EXP = "ReExp";
    private static String LOG_RE_EXP = "LogReExp";
    private static String SPLIT_RE_EXP = "SplitReExp";
    private static String PARTITION_RE_EXP = "PartitionReExp";
    private static String PARSE_LOG_ACTION = "ParseLogAction";

    // The Derby connection URL.
    private static String connectionURL = "jdbc:derby:" + dbName + ";create=true";

    private static Connection conn = null;
    private static Statement stmt = null;

    public static void main(String[] args) {
        createConnection();
        createAllTables();
        shutdown();
    }

    private static void createConnection() {
        try {
            Class.forName(driver).newInstance();
            //Get a connection
            conn = DriverManager.getConnection(connectionURL); 
        } catch (Exception except) {
             except.printStackTrace();
        }
    }
    
    private static void createAllTables() {
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
            stmt.execute("CREATE TABLE " + UPLOADED_LOG + " (logid INT PRIMARY KEY, text CLOB, hash VARCHAR(255))");
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
    
    public static void createParseLogActionTable() {
        try {
            stmt = conn.createStatement();
            stmt.execute("CREATE TABLE " + PARSE_LOG_ACTION + " (vid INT PRIMARY KEY, timestamp TIMESTAMP, parseid INT, result VARCHAR(255))");
            stmt.close();
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
    }
    
    private static void shutdown() {
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
