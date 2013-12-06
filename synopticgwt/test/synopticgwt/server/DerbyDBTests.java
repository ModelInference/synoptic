package synopticgwt.server;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import junit.framework.TestCase;

import synopticgwt.server.db.DerbyDB;
import synopticgwt.server.db.LogReExpTable;
import synopticgwt.server.db.ParseLogActionTable;
import synopticgwt.server.db.PartitionReExpTable;
import synopticgwt.server.db.ReExpTable;
import synopticgwt.server.db.SplitReExpTable;
import synopticgwt.server.db.UploadedLogTable;
import synopticgwt.server.db.VisitorTable;

/**
 * Test an instance of a Derby instance.
 */
public class DerbyDBTests extends TestCase {

    /**
     * Used to expose internal table objects during testing.
     */
    private class TestDerbyDB extends DerbyDB {
        TestDerbyDB(String path, boolean isCreate) throws SQLException,
                InstantiationException, IllegalAccessException,
                ClassNotFoundException {
            super(path, isCreate);
        }

        LogReExpTable getLogReExpTable() {
            return logReExpTable;
        }

        ParseLogActionTable getParseLogActionTable() {
            return parseLogActionTable;
        }

        PartitionReExpTable getPartitionReExpTable() {
            return partitionReExpTable;
        }

        ReExpTable getReExpTable() {
            return reExpTable;
        }

        SplitReExpTable getSplitReExpTable() {
            return splitReExpTable;
        }

        UploadedLogTable getUploadedLogTable() {
            return uploadedLogTable;
        }

        VisitorTable getVisitorTable() {
            return visitorTable;
        }
    }

    /** Database name and path */
    private static String dbPath = "." + File.separator + "test-output"
            + File.separator + "DerbyDBTests_.derby";

    private TestDerbyDB db;
    private VisitorTable visitor;
    private UploadedLogTable uploadedLog;
    private ReExpTable reExp;
    private LogReExpTable logReExp;
    private SplitReExpTable splitReExp;
    private PartitionReExpTable partitionReExp;
    private ParseLogActionTable parseLogAction;

    /**
     * Set up database and tables for each test run.
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        db = new TestDerbyDB(dbPath, true);

        visitor = db.getVisitorTable();
        uploadedLog = db.getUploadedLogTable();
        reExp = db.getReExpTable();
        logReExp = db.getLogReExpTable();
        splitReExp = db.getSplitReExpTable();
        partitionReExp = db.getPartitionReExpTable();
        parseLogAction = db.getParseLogActionTable();
    }

    /**
     * * Cleans up after itself by deleting the created database.
     */
    @Override
    public void tearDown() {
        db.shutdown();
        try {
            // There is no drop database command. To drop a database, delete the
            // database directory with operating system commands
            FileUtils.deleteDirectory(new File(dbPath));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Checks that database and tables aren't null.
     */
    @Test
    public void testPreconditions() {
        assertNotNull(db);

        assertNotNull(visitor);
        assertNotNull(uploadedLog);
        assertNotNull(reExp);
        assertNotNull(logReExp);
        assertNotNull(splitReExp);
        assertNotNull(partitionReExp);
        assertNotNull(parseLogAction);
    }

    /**
     * Writes to
     * 
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws SQLException
     */
    @Test
    public void testWriteAndRead() throws SQLException, InstantiationException,
            IllegalAccessException, ClassNotFoundException {

        // /// INSERTING INTO TABLES /////
        String expected_ip = "24.22.234.22";
        Timestamp expected_time = new Timestamp(System.currentTimeMillis());
        int expected_vid = visitor.insert(expected_ip, expected_time);
        assertEquals(1, expected_vid);

        String expected_text = "test text";
        String expected_hash = "test hash";
        int expected_reid = reExp.insert(expected_text, expected_hash);
        assertEquals(1, expected_reid);

        int expected_logid = uploadedLog.insert(expected_text, expected_hash);
        assertEquals(1, expected_logid);

        String expected_result = "test parse result";
        int expected_parseid = parseLogAction.insert(expected_vid,
                expected_time, expected_result);
        assertEquals(1, expected_parseid);

        logReExp.insert(expected_parseid, expected_reid, expected_logid);
        splitReExp.insert(expected_parseid, expected_reid, expected_logid);
        partitionReExp.insert(expected_parseid, expected_reid, expected_logid);

        // /// READING FROM TABLES /////

        // Reading VisitorTable table
        ResultSet rs_VisitorTable = visitor.getSelect();
        int vid_VisitorTable = 0;
        String ip_VisitorTable = null;
        String time_VisitorTable = null;
        while (rs_VisitorTable.next()) {
            vid_VisitorTable = rs_VisitorTable.getInt("vid");
            ip_VisitorTable = rs_VisitorTable.getString("IP");
            time_VisitorTable = rs_VisitorTable.getString("timestamp");
        }
        rs_VisitorTable.close();
        assertEquals(expected_vid, vid_VisitorTable);
        assertEquals(expected_ip, ip_VisitorTable);
        assertEquals(expected_time.toString(), time_VisitorTable);
        // Reading ReExp table
        ResultSet rs_ReExp = reExp.getSelect();
        int reid_ReExp = 0;
        String text_ReExp = null;
        String hash_ReExp = null;
        while (rs_ReExp.next()) {
            reid_ReExp = rs_ReExp.getInt("reid");
            text_ReExp = rs_ReExp.getString("text");
            hash_ReExp = rs_ReExp.getString("hash");
        }
        rs_ReExp.close();
        assertEquals(expected_reid, reid_ReExp);
        assertEquals(expected_text, text_ReExp);
        assertEquals(expected_hash, hash_ReExp);

        // Reading UploadedLogTable table
        ResultSet rs_UploadedLogTable = uploadedLog.getSelect();
        int logid_UploadedLogTable = 0;
        String text_UploadedLogTable = null;
        String hash_UploadedLogTable = null;
        while (rs_UploadedLogTable.next()) {
            logid_UploadedLogTable = rs_UploadedLogTable.getInt("logid");
            text_UploadedLogTable = rs_UploadedLogTable.getString("text");
            hash_UploadedLogTable = rs_UploadedLogTable.getString("hash");
        }
        rs_UploadedLogTable.close();
        assertEquals(expected_logid, logid_UploadedLogTable);
        assertEquals(expected_text, text_UploadedLogTable);
        assertEquals(expected_hash, hash_UploadedLogTable);

        // Reading ParseLogAction table
        ResultSet rs_ParseLogAction = parseLogAction.getSelect();
        int vid_ParseLogAction = 0;
        String time_ParseLogAction = null;
        int parseid_ParseLogAction = 0;
        String result_ParseLogAction = null;
        while (rs_ParseLogAction.next()) {
            vid_ParseLogAction = rs_ParseLogAction.getInt("vid");
            time_ParseLogAction = rs_ParseLogAction.getString("timestamp");
            parseid_ParseLogAction = rs_ParseLogAction.getInt("parseid");
            result_ParseLogAction = rs_ParseLogAction.getString("result");
        }
        rs_ParseLogAction.close();
        assertEquals(expected_vid, vid_ParseLogAction);
        assertEquals(expected_parseid, parseid_ParseLogAction);
        assertEquals(expected_time.toString(), time_ParseLogAction);
        assertEquals(expected_result, result_ParseLogAction);

        // Reading LogReExp table
        ResultSet rs_LogReExp = logReExp.getSelect();
        int parseid_LogReExp = 0;
        int reid_LogReExp = 0;
        int logid_LogReExp = 0;
        while (rs_LogReExp.next()) {
            parseid_LogReExp = rs_LogReExp.getInt("parseid");
            reid_LogReExp = rs_LogReExp.getInt("reid");
            logid_LogReExp = rs_LogReExp.getInt("logid");
        }
        assertEquals(expected_parseid, parseid_LogReExp);
        assertEquals(expected_reid, reid_LogReExp);
        assertEquals(expected_logid, logid_LogReExp);

        // Reading SplitReExpTable table
        ResultSet rs6 = logReExp.getSelect();
        int parseid_SplitReExpTable = 0;
        int reid_SplitReExpTable = 0;
        int logid_SplitReExpTable = 0;
        while (rs6.next()) {
            parseid_SplitReExpTable = rs6.getInt("parseid");
            reid_SplitReExpTable = rs6.getInt("reid");
            logid_SplitReExpTable = rs6.getInt("logid");
        }
        assertEquals(expected_parseid, parseid_SplitReExpTable);
        assertEquals(expected_reid, reid_SplitReExpTable);
        assertEquals(expected_logid, logid_SplitReExpTable);

        // Reading PartitionReExpTable table
        ResultSet rs7 = logReExp.getSelect();
        int parseid_PartitionReExpTable = 0;
        int reid_PartitionReExpTable = 0;
        int logid_PartitionReExpTable = 0;
        while (rs7.next()) {
            parseid_PartitionReExpTable = rs7.getInt("parseid");
            reid_PartitionReExpTable = rs7.getInt("reid");
            logid_PartitionReExpTable = rs7.getInt("logid");
        }
        assertEquals(expected_parseid, parseid_PartitionReExpTable);
        assertEquals(expected_reid, reid_PartitionReExpTable);
        assertEquals(expected_logid, logid_PartitionReExpTable);
    }
}