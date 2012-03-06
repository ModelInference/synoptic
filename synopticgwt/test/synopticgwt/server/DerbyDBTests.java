package synopticgwt.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
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
     * Tests writing to the database.
     * 
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws SQLException
     */
    @Test
    public void testWrite() throws SQLException, InstantiationException,
            IllegalAccessException, ClassNotFoundException {
    	
    	Timestamp now = new Timestamp(System.currentTimeMillis());
    	int vid = visitor.insert("24.22.234.22", now);
    	assertEquals(1, vid);
    	
    	int reid = reExp.insert("test", "hash");
    	assertEquals(1, reid);
    	
    	int logid = uploadedLog.insert("test log", "hash");
    	assertEquals(1, logid);
    	
    	String parseResult = "test parsing";
    	int parseid = parseLogAction.insert(vid, now, parseResult);
    	assertEquals(1, parseid);
    	
    	logReExp.insert(parseid, reid, logid);
    	
    	
    }
}
