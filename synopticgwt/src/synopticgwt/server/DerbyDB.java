package synopticgwt.server;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import synoptic.invariants.TemporalInvariantSet;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.EventNode;
import synopticgwt.server.table.LogReExp;
import synopticgwt.server.table.ParseLogAction;
import synopticgwt.server.table.PartitionReExp;
import synopticgwt.server.table.DerbyTable;
import synopticgwt.server.table.ReExp;
import synopticgwt.server.table.SplitReExp;
import synopticgwt.server.table.Table;
import synopticgwt.server.table.UploadedLog;
import synopticgwt.server.table.Visitor;
import synopticgwt.shared.GWTGraph;
import synopticgwt.shared.GWTInvariantSet;
import synopticgwt.shared.GWTSynOpts;

/**
 * Derby database.
 */
public class DerbyDB {
    // Singleton instance of DerbyDB.
    private static DerbyDB instance;

    // Instance of MessageDigest for String hashing.
    private static MessageDigest mdInstance;
    
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
    
    private Map<Table, DerbyTable> m;

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
    	m = new HashMap<Table, DerbyTable>();
    	
        connectionURL = "jdbc:derby:" + path;
        createConnection(isCreate);
        connectToTables();
        if (isCreate) {
            createAllTables();
        }
    }
    
    private void connectToTables() {
    	m.put(Table.LogRexp, new LogReExp(conn, stmt));
    	m.put(Table.ParseLogAction, new ParseLogAction(conn, stmt));
    	m.put(Table.PartitionReExp, new PartitionReExp(conn, stmt));
    	m.put(Table.ReExp, new ReExp(conn, stmt));
    	m.put(Table.SplitReExp, new SplitReExp(conn, stmt));
    	m.put(Table.UploadedLog, new UploadedLog(conn, stmt));
    	m.put(Table.Visitor, new Visitor(conn, stmt));
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
//        createQuery(VISITOR);
//        createQuery(UPLOADED_LOG);
//        createQuery(RE_EXP);
//        createQuery(LOG_RE_EXP);
//        createQuery(SPLIT_RE_EXP);
//        createQuery(PARTITION_RE_EXP);
//        createQuery(PARSE_LOG_ACTION);
    	for (Table key : m.keySet()) {
    		m.get(key).createTable();
    	}
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
     * Returns the MD5 hash of a String.
     * @param message
     * @return MD5 hash of given message
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     */
    public static String getHash(String message) throws UnsupportedEncodingException,
            NoSuchAlgorithmException {
    	logger.info("Generating a hash");
        byte[] byteMessage = message.getBytes("UTF-8");
        if (mdInstance == null) {
        	mdInstance = MessageDigest.getInstance("MD5");
        }
     
        mdInstance.update(byteMessage, 0, byteMessage.length);
        BigInteger i = new BigInteger(1, mdInstance.digest());
        String result = i.toString(16);
        while (result.length() < 2) {
            result = "0" + result;
        }
        return result;
    }
    
 // Checks if reExp exists in the given table already. If it exists, return
    // the row id of it in
    // the table. If it doesn't exist, insert reExp into table and return row id
    // of where it was inserted.
    private int getReId(String reExp, String tableName)
            throws UnsupportedEncodingException, NoSuchAlgorithmException,
            SQLException {
        String cleanString = reExp.replace("'", "''"); // Clean String for
                                                       // single quotes.
        String hashReExp = DerbyDB.getHash(cleanString);

        int reId = getIdExistingRow("select * from " + tableName
                + " where hash = '" + hashReExp + "'");

        if (reId == -1) { // doesn't exist in database
            reId = insertAndGetAutoValue("insert into "
                    + tableName + "(text, hash) values('" + cleanString
                    + "', '" + hashReExp + "')");
            logger.info("Hash for a reg exp or log lines found in DerbyDB");
        }
        return reId;
    }

    // Checks each reg exp in list if it exists in the ReExp table already.
    // Returns list of ids
    // for each reg exp in the ReExp table.
    private List<Integer> getLogReExp(List<String> l)
            throws UnsupportedEncodingException, NoSuchAlgorithmException,
            SQLException {
        List<Integer> result = new ArrayList<Integer>();
        for (int i = 0; i < l.size(); i++) {
            int currId = getReId(l.get(i), "ReExp");
            result.add(currId);
        }
        return result;
    }
    
    /**
     * Writes to database user's SynopticGWT usage after parsing a log.
     * @param vID
     * @param synOpts
     * @param graph
     * @param traceGraph
     * @param parsedEvents
     * @param minedInvs
     * @param invs
     * @param miningTime
     * @throws SQLException
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     */
    public void writeUserParsingInfo(int vID, GWTSynOpts synOpts, 
    		GWTGraph graph, ChainsTraceGraph traceGraph, ArrayList<EventNode> parsedEvents,
    		TemporalInvariantSet minedInvs, GWTInvariantSet invs, int miningTime) throws SQLException, UnsupportedEncodingException, NoSuchAlgorithmException {
        logger.info("Writing information to Derby");
    	
    	List<Integer> logReId = getLogReExp(synOpts.regExps);
        int partitionReId = getReId(synOpts.partitionRegExp, "ReExp");
        int splitReId = getReId(synOpts.separatorRegExp, "ReExp");
        int logLineId = getReId(synOpts.logLines, "UploadedLog");

        // Create a result for summarizing log parsing.
        String parseResult = "";
        parseResult += "edges:" + graph.edges.size() + "," + "nodes:"
                + graph.nodeSet.size() + "," + "traces:"
                + traceGraph.getNodes().size() + "," + "etypes:"
                + parsedEvents.size() + ",";
        for (String key : invs.invs.keySet()) {
            parseResult += key + ":" + invs.invs.get(key).size() + ",";
        }
        parseResult += "miningtime:" + miningTime;
        logger.info("Result of parsed log: " + parseResult);

        // TODO add synoptictime to parseResult (time to derive final model)

        // Insert into ParseLogAction table and obtain parseID to associate
        // with the reg exps.
        Timestamp now = new Timestamp(System.currentTimeMillis());
        String q = "insert into ParseLogAction(vid, timestamp, result) values("
                + vID + ", '" + now + "', '" + parseResult + "')";
        int parseID = insertAndGetAutoValue(q);

        // Inserts into reg exps tables.
        for (int i = 0; i < logReId.size(); i++) {
        	updateQuery("insert into LogReExp(parseid, reid, logid) values("
	            + parseID
	            + ", "
	            + logReId.get(i)
	            + ", "
	            + logLineId + ")");
		}
    	updateQuery("insert into SplitReExp(parseid, reid, logid) values("
            + parseID
            + ", "
            + splitReId
            + ", "
            + logLineId
            + ")");
    		
		updateQuery("insert into PartitionReExp(parseid, reid, logid) values("
            + parseID
            + ", "
            + partitionReId
            + ", "
            + logLineId
            + ")");
    
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
