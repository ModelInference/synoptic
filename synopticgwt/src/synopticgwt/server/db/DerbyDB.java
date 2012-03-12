package synopticgwt.server.db;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import synoptic.invariants.TemporalInvariantSet;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.EventNode;
import synopticgwt.shared.GWTGraph;
import synopticgwt.shared.GWTInvariantSet;
import synopticgwt.shared.GWTSynOpts;

/**
 * Derby database that stores the following tables: Visitor, UploadedLog, ReExp,
 * LogReExp, SplitReExp, PartitionReExp, ParseLogAction Contains method
 * writeUserParsingInfo that SynopticService uses to write user data to all the
 * tables. Tables can be manipulated individually by getting a map of all the
 * tables. Casting of a table is required to access all its methods.
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

    protected LogReExpTable logReExpTable;
    protected ParseLogActionTable parseLogActionTable;
    protected PartitionReExpTable partitionReExpTable;
    protected ReExpTable reExpTable;
    protected SplitReExpTable splitReExpTable;
    protected UploadedLogTable uploadedLogTable;
    protected VisitorTable visitorTable;

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
    protected DerbyDB(String path, boolean isCreate) throws SQLException,
            InstantiationException, IllegalAccessException,
            ClassNotFoundException {

        connectionURL = "jdbc:derby:" + path;

        // Establishes a connection with the database.
        Class.forName(driver).newInstance();
        // Get a connection
        conn = DriverManager.getConnection(connectionURL + ";create="
                + isCreate);
        logger.info("Connecting to Derby database");

        // Create all the tables.
        logReExpTable = new LogReExpTable(conn);
        parseLogActionTable = new ParseLogActionTable(conn);
        partitionReExpTable = new PartitionReExpTable(conn);
        reExpTable = new ReExpTable(conn);
        splitReExpTable = new SplitReExpTable(conn);
        uploadedLogTable = new UploadedLogTable(conn);
        visitorTable = new VisitorTable(conn);

        // Populate the list of tables used below for common method calls.
        List<DerbyTable> tables = new LinkedList<DerbyTable>();
        tables.add(logReExpTable);
        tables.add(parseLogActionTable);
        tables.add(partitionReExpTable);
        tables.add(reExpTable);
        tables.add(splitReExpTable);
        tables.add(uploadedLogTable);
        tables.add(visitorTable);

        if (isCreate) {
            // Create all the tables in the database.
            for (DerbyTable table : tables) {
                table.createTable();
            }
            logger.info("Created all tables in Derby db");
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
     * Returns the MD5 hash of a String.
     * 
     * @param message
     * @return MD5 hash of given message
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     */
    public static String getHash(String message)
            throws UnsupportedEncodingException, NoSuchAlgorithmException {
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
     * Writes data about user's SynopticGWT usage after parsing a log into
     * database.
     * 
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
            GWTGraph graph, ChainsTraceGraph traceGraph,
            ArrayList<EventNode> parsedEvents, TemporalInvariantSet minedInvs,
            GWTInvariantSet invs, int miningTime) throws SQLException,
            UnsupportedEncodingException, NoSuchAlgorithmException {

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

        // TODO: add synoptictime to parseResult (time to derive final model)

        // Insert into ParseLogAction table and obtain parseID to associate
        // with the reg exps.
        Timestamp now = new Timestamp(System.currentTimeMillis());

        int parseID = parseLogActionTable.insert(vID, now, parseResult);
        logger.info("Inserted data in ParseLogAction table");

        // Inserts into reg exps tables.
        for (int i = 0; i < logReId.size(); i++) {
            logReExpTable.insert(parseID, logReId.get(i), logLineId);
            logger.info("Inserted data in LogReExp table");
        }
        splitReExpTable.insert(parseID, splitReId, logLineId);
        logger.info("Inserted data in SplitReExp table");

        partitionReExpTable.insert(parseID, partitionReId, logLineId);
        logger.info("Inserted data in PartitionReExp table");
    }

    /**
     * Checks each reg exp in list if it exists in the ReExp table already.
     * Returns list of ids for each reg exp in the ReExp table.
     */
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

    /**
     * Checks if reExp exists in the given table already. If it exists, return
     * the row id of it in the table. If it doesn't exist, insert reExp into
     * table and return row id of where it was inserted.
     */
    private int getReId(String reExp) throws UnsupportedEncodingException,
            NoSuchAlgorithmException, SQLException {
        // Clean String for single quotes.
        String cleanString = reExp.replace("'", "''");
        String hashReExp = DerbyDB.getHash(cleanString);

        int reId;
        try {
            reId = reExpTable.getIdExistingHash(hashReExp);
            logger.info("Hash found in ReExp table");
        } catch (NoSuchElementException e) {
            reId = reExpTable.insert(cleanString, hashReExp);
            logger.info("Inserted data into ReExp table");
        }

        return reId;
    }

    /**
     * Given a string of log lines, checks if log lines exists in the database.
     * If it exists, get the id of existing log from database. If it doesn't
     * exist, inserts the log lines into the database and return the id.
     */
    private int getUploadedLogId(String logLines)
            throws UnsupportedEncodingException, NoSuchAlgorithmException,
            SQLException {
        // Clean String for single quotes.
        String cleanString = logLines.replace("'", "''");
        String hashReExp = DerbyDB.getHash(cleanString);

        int reId;
        try {
            reId = uploadedLogTable.getIdExistingHash(hashReExp);
            logger.info("Hash found in UploadedLog table");
        } catch (NoSuchElementException e) {
            reId = uploadedLogTable.insert(cleanString, hashReExp);
            logger.info("Inserted data into UploadedLog table");
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
            if (conn != null) {
                DriverManager.getConnection(connectionURL + ";shutdown=true");
                conn.close();
                logger.info("Shutting down Derby database");
            }
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
    }

    /**
     * Creates/adds a new visitor record with a specific ip address and
     * timestamp.
     * 
     * @param ipAddress
     * @param now
     * @return
     * @throws SQLException
     */
    public int addNewVisitor(String ipAddress, Timestamp now)
            throws SQLException {
        return visitorTable.insert(ipAddress, now);
    }
}
