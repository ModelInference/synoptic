package synopticgwt.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

/**
 * Derby database.
 */
public class DerbyDB {
    // Singleton instance of DerbyDB
    private static DerbyDB instance;

    public static Logger logger = Logger.getLogger("DerbyDB");

    // The driver to use.
    private static String driver = "org.apache.derby.jdbc.EmbeddedDriver";

    // Various tables.
    private static String VISITOR = "CREATE TABLE Visitor (vid INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), IP VARCHAR(15), timestamp TIMESTAMP)";
    private static String UPLOADED_LOG = "CREATE TABLE UploadedLog (logid INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), text CLOB, hash VARCHAR(32))";
    private static String RE_EXP = "CREATE TABLE ReExp (reid INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), text CLOB, hash VARCHAR(32))";
    private static String LOG_RE_EXP = "CREATE TABLE LogReExp (parseid INT, reid INT, logid INT)";
    private static String SPLIT_RE_EXP = "CREATE TABLE SplitReExp (parseid INT, reid INT, logid INT)";
    private static String PARTITION_RE_EXP = "CREATE TABLE PartitionReExp (parseid INT, reid INT, logid INT)";
    private static String PARSE_LOG_ACTION = "CREATE TABLE ParseLogAction (vid INT, timestamp TIMESTAMP, parseid INT GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), result VARCHAR(255))";

    // The Derby connection URL.
    private String connectionURL;

    private Connection conn = null;
    private Statement stmt = null;

    /**
     * Creates a connection to the database. Builds tables if isCreate is true.
     * 
     * @param path
     *            path of database
     * @param isCreate
     *            whether or not to create new tables
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws SQLException
     */
    private DerbyDB(String path, boolean isCreate) throws SQLException,
            InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        connectionURL = "jdbc:derby:" + path;
        createConnection(isCreate);
        if (isCreate) {
            createAllTables();
        }
    }

    /**
     * Returns singleton instance of DerbyDB.
     * 
     * @param path
     *            path of database
     * @param isCreate
     *            whether or not to create new tables
     * @return DerbyDB instance
     */
    public static DerbyDB getInstance(String path, boolean isCreate)
            throws SQLException, InstantiationException,
            IllegalAccessException, ClassNotFoundException {

        if (instance != null) {
            return instance;
        }
        return new DerbyDB(path, isCreate);
    }

    /**
     * Establishes a connection with the database.
     * 
     * @param isCreate
     *            whether or not to create a new database
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private void createConnection(boolean isCreate) throws SQLException,
            InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        Class.forName(driver).newInstance();
        // Get a connection
        conn = DriverManager.getConnection(connectionURL + ";create="
                + isCreate);
        logger.info("Connecting to Derby database.");
    }

    /**
     * Create all the tables in the database.
     */
    private void createAllTables() throws SQLException {
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
    public void createQuery(String query) throws SQLException {
        stmt = conn.createStatement();
        stmt.execute(query);
        stmt.close();
    }

    /**
     * Executes an update query in database.
     * 
     * @throws SQLException
     */
    public void updateQuery(String query) throws SQLException {
        stmt = conn.createStatement();
        int n = stmt.executeUpdate(query);
        stmt.close();
        logger.info(n
                + " row(s) inserted, updated, or deleted in Derby database");
    }

    /**
     * Given a SELECT statement selecting a specific row, returns the first
     * column (usually id) of row. Returns -1 if the row doesn't exist in
     * database.
     * 
     * @param String
     *            query to select a specific row
     * @return int returns first column value of row
     */
    public int getIdExistingRow(String query) throws SQLException {
        int result = -1;

        stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {
            result = rs.getInt(1);
        }

        rs.close();
        stmt.close();

        return result;
    }

    /**
     * Executes an INSERT query and returns auto incrementing identity field
     * assigned to newly created record. Returns 0 if incrementing identity
     * field doesn't exist.
     * 
     * @param query
     *            the insert query to use
     */
    public int insertAndGetAutoValue(String query) throws SQLException {
        int result = 0;

        stmt = conn.createStatement();
        int n = stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
        logger.info("Inserted " + n + " row(s) into Derby database.");

        ResultSet rs = stmt.getGeneratedKeys();

        while (rs.next()) {
            result = rs.getInt(1);
        }

        rs.close();
        stmt.close();

        return result;
    }

    /**
     * Given a SELECT query retrieving a single String type column, returns
     * values in that column (e.g. select columnname from tablename).
     * 
     * @param query
     *            select query
     * @param column
     *            the column to retrieve information from
     * @return String values selected from query
     */
    public String getString(String query, String column) throws SQLException {
        String s = "";

        stmt = conn.createStatement();
        ResultSet results = stmt.executeQuery(query);
        while (results.next()) {
            s += results.getString(column);

        }
        results.close();
        stmt.close();

        return s;
    }

    /**
     * Shutdown the database. Note: A successful shutdown always results in an
     * SQLException to indicate that Derby has shut down and that there is no
     * other exception.
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
