package synopticgwt.server;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
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

    // The Derby connection URL.
    private String connectionURL;

    private Connection conn = null;
    private Statement stmt = null;
    
    /** Keeps track of all the tables. */
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
    
    // Stores a collection of DerbyTable ojects.
    private void connectToTables() {
    	m.put(Table.LogReExp, new LogReExp(conn, stmt));
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
        logger.info("Connecting to Derby database");
    }

    /**
     * Create all the tables in the database.
     */
    private void createAllTables() throws SQLException {
    	for (Table key : m.keySet()) {
    		m.get(key).createTable();
    	}
    	logger.info("Created all tables in Derby db");
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
    
    /**
     * Returns a map containing all DerbyTables.
     * @return collection of DerbyTables.
     */
    public Map<Table, DerbyTable> getTables() {
    	return Collections.unmodifiableMap(m);
    }
    
    /**
     * Writes data about user's SynopticGWT usage after parsing a log
     * into database.
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
    		TemporalInvariantSet minedInvs, GWTInvariantSet invs, int miningTime) 
    			throws SQLException, UnsupportedEncodingException, NoSuchAlgorithmException {
    	
    	List<Integer> logReId = getLogReExp(synOpts.regExps);
        int partitionReId = getReId(synOpts.partitionRegExp);
        int splitReId = getReId(synOpts.separatorRegExp);
        int logLineId = getUploadedLogId(synOpts.logLines);

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
        ParseLogAction parseLogActionTable = (ParseLogAction) m.get(Table.ParseLogAction);
        int parseID = parseLogActionTable.insert(vID, now, parseResult);
        logger.info("Inserted data in ParseLogAction table");

        // Inserts into reg exps tables.
        for (int i = 0; i < logReId.size(); i++) {
        	LogReExp l = (LogReExp) m.get(Table.LogReExp);
        	l.insert(parseID, logReId.get(i), logLineId);
        	logger.info("Inserted data in LogReExp table");
		}
        SplitReExp s = (SplitReExp) m.get(Table.SplitReExp);
        s.insert(parseID, splitReId, logLineId);
        logger.info("Inserted data in SplitReExp table");

    	PartitionReExp p = (PartitionReExp) m.get(Table.PartitionReExp);
    	p.insert(parseID, partitionReId, logLineId);
    	logger.info("Inserted data in PartitionReExp table");
    }
    
    // Checks each reg exp in list if it exists in the ReExp table already.
    // Returns list of ids for each reg exp in the ReExp table.
    private List<Integer> getLogReExp(List<String> l)
            throws UnsupportedEncodingException, NoSuchAlgorithmException,
            SQLException {
        List<Integer> result = new ArrayList<Integer>();
        for (int i = 0; i < l.size(); i++) {
            int currId = getReId(l.get(i));
            result.add(currId);
        }
        return result;
    }
    
    // Checks if reExp exists in the given table already. If it exists, return
    // the row id of it in the table. If it doesn't exist, insert reExp into 
    // table and return row id of where it was inserted.
    private int getReId(String reExp)
            throws UnsupportedEncodingException, NoSuchAlgorithmException,
            SQLException {
    	// Clean String for single quotes.
        String cleanString = reExp.replace("'", "''"); 
        String hashReExp = DerbyDB.getHash(cleanString);
        
        ReExp r = (ReExp) m.get(Table.ReExp);
        int reId = r.getIdExistingHash(hashReExp);

        if (reId == -1) { // doesn't exist in database
        	reId = r.insert(cleanString, hashReExp);
			logger.info("Inserted data into ReExp table");
        } else {
            logger.info("Hash for a reg exp or log lines found in DerbyDB");
        }
        return reId;
    }
    
    // Given a string of log lines, checks if log lines exists in the database.
    // If it exists, get the id of existing log from database. If it doesn't
    // exist, inserts the log lines into the database and return the id.
    private int getUploadedLogId(String logLines) 
    		throws UnsupportedEncodingException, NoSuchAlgorithmException,
    		SQLException {
    	// Clean String for single quotes.
    	String cleanString = logLines.replace("'", "''"); 
		String hashReExp = DerbyDB.getHash(cleanString);
		
		UploadedLog u = (UploadedLog) m.get(Table.UploadedLog);	
		int reId = u.getIdExistingHash(hashReExp);

		if (reId == -1) { // doesn't exist in database
			reId = u.insert(cleanString, hashReExp);
			logger.info("Inserted data into UploadedLog table");
		} else {
			logger.info("Hash found in UploaedLog table");
		}
		return reId;
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
