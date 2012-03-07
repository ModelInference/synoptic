package synopticgwt.server;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import synopticgwt.server.table.DerbyTable;
import synopticgwt.server.table.LogReExp;
import synopticgwt.server.table.ParseLogAction;
import synopticgwt.server.table.PartitionReExp;
import synopticgwt.server.table.ReExp;
import synopticgwt.server.table.SplitReExp;
import synopticgwt.server.table.Table;
import synopticgwt.server.table.UploadedLog;
import synopticgwt.server.table.Visitor;

/**
 * Test an instance of a Derby instance.
 */
public class DerbyDBTests extends TestCase {
    /** Database name and path */
    private static String dbPath = "." + File.separator + "test-output"
            + File.separator + "DerbyDBTests.derby";

    private DerbyDB db;
    private Visitor visitor;
    private UploadedLog uploadedLog;
    private ReExp reExp;
    private LogReExp logReExp;
    private SplitReExp splitReExp;
    private PartitionReExp partitionReExp;
    private ParseLogAction parseLogAction;
    
    /**
     * Set up database and tables for each test run.
     */
    @Override
    public void setUp() throws Exception {
    	super.setUp();
    	db = DerbyDB.getInstance(dbPath, true);
    	
    	Map<Table, DerbyTable> m = db.getTables();
    	visitor = (Visitor) m.get(Table.Visitor);
    	uploadedLog = (UploadedLog) m.get(Table.UploadedLog);
    	reExp = (ReExp) m.get(Table.ReExp);
    	logReExp = (LogReExp) m.get(Table.LogReExp);
    	splitReExp = (SplitReExp) m.get(Table.SplitReExp);
    	partitionReExp = (PartitionReExp) m.get(Table.PartitionReExp);
    	parseLogAction = (ParseLogAction) m.get(Table.ParseLogAction);
    }
    
	/**
	 * Cleans up after itself by deleting the created database.
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
    	
    	///// INSERTING INTO TABLES /////
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
    	int expected_parseid = parseLogAction.insert(expected_vid, expected_time, expected_result);
    	assertEquals(1, expected_parseid);
    	
    	logReExp.insert(expected_parseid, expected_reid, expected_logid);
    	splitReExp.insert(expected_parseid, expected_reid, expected_logid);
    	partitionReExp.insert(expected_parseid, expected_reid, expected_logid);
    	
    	///// READING FROM TABLES /////
    	
    	// Reading Visitor table
    	ResultSet rs_Visitor = visitor.getSelect();
    	int vid_Visitor = 0;
    	String ip_Visitor = null;
    	String time_Visitor = null;
    	while (rs_Visitor.next()) {
    		vid_Visitor = rs_Visitor.getInt("vid");
    		ip_Visitor = rs_Visitor.getString("IP");
            time_Visitor = rs_Visitor.getString("timestamp");
        }
    	rs_Visitor.close();
    	assertEquals(expected_vid, vid_Visitor);
    	assertEquals(expected_ip, ip_Visitor);
    	assertEquals(expected_time.toString(), time_Visitor);
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
    	
    	// Reading UploadedLog table
    	ResultSet rs_UploadedLog = uploadedLog.getSelect();
    	int logid_UploadedLog = 0;
    	String text_UploadedLog = null;
    	String hash_UploadedLog = null;
    	while (rs_UploadedLog.next()) {
    		logid_UploadedLog = rs_UploadedLog.getInt("logid");
    		text_UploadedLog = rs_UploadedLog.getString("text");
            hash_UploadedLog = rs_UploadedLog.getString("hash");
        }
    	rs_UploadedLog.close();
    	assertEquals(expected_logid, logid_UploadedLog);
    	assertEquals(expected_text, text_UploadedLog);
    	assertEquals(expected_hash, hash_UploadedLog);
    	
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
    	while(rs_LogReExp.next()) {
    		parseid_LogReExp = rs_LogReExp.getInt("parseid");
    		reid_LogReExp = rs_LogReExp.getInt("reid");
    		logid_LogReExp = rs_LogReExp.getInt("logid");
    	}
    	assertEquals(expected_parseid, parseid_LogReExp);
    	assertEquals(expected_reid, reid_LogReExp);
    	assertEquals(expected_logid, logid_LogReExp);
    	
    	// Reading SplitReExp table
    	ResultSet rs6 = logReExp.getSelect();
    	int parseid_SplitReExp = 0;
    	int reid_SplitReExp = 0;
    	int logid_SplitReExp = 0;
    	while(rs6.next()) {
    		parseid_SplitReExp = rs6.getInt("parseid");
    		reid_SplitReExp = rs6.getInt("reid");
    		logid_SplitReExp = rs6.getInt("logid");
    	}
    	assertEquals(expected_parseid, parseid_SplitReExp);
    	assertEquals(expected_reid, reid_SplitReExp);
    	assertEquals(expected_logid, logid_SplitReExp);
    	
    	// Reading PartitionReExp table
    	ResultSet rs7 = logReExp.getSelect();
    	int parseid_PartitionReExp = 0;
    	int reid_PartitionReExp = 0;
    	int logid_PartitionReExp = 0;
    	while(rs7.next()) {
    		parseid_PartitionReExp = rs7.getInt("parseid");
    		reid_PartitionReExp = rs7.getInt("reid");
    		logid_PartitionReExp = rs7.getInt("logid");
    	}
    	assertEquals(expected_parseid, parseid_PartitionReExp);
    	assertEquals(expected_reid, reid_PartitionReExp);
    	assertEquals(expected_logid, logid_PartitionReExp);
    }
}