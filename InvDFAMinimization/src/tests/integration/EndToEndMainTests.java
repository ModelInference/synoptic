package tests.integration;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import main.DFAMain;

import org.junit.Test;

import synoptic.invariants.CanImmediatelyFollowedInvariant;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.main.Main;
import synoptic.model.PartitionGraph;
import tests.InvDFAMinimizationTest;

/**
 * Runs the DFAMain project end-to-end on a different log files.
 * 
 * @author ivan
 */
public class EndToEndMainTests extends InvDFAMinimizationTest {

    /**
     * Test on osx-login-example in traces/abstract/.
     * 
     * @throws Exception
     */
    @Test
    public void abstractLogFileTest() throws Exception {
        // TODO: make the path insensitive to current location.
        String tPath = "../traces/";

        String[] args = new String[] {"-c",
                tPath + "abstract/osx-login-example/args.txt",
                tPath + "abstract/osx-login-example/trace.txt" };
        DFAMain.main(args);
    }
    
    /**
     * Test implicit invariant mining on midbranching example in traces/abstract/
     * 
     * @throws Exception
     */
    @Test
    public void implicitInvariantsMidBranching() throws Exception {
        // TODO: make the path insensitive to current location.

        String tPath = "../traces/";
        String[] args = new String[] {
        		"--dumpInitialPartitionGraph=true",
        		"--dumpInitialGraphDotFile=false",
        		"--dumpInitialGraphPngFile=false",
        		"-c",
                tPath + "abstract/mid_branching/args.txt",
                tPath + "abstract/mid_branching/trace.txt" };
        
        Main synMain = Main.processArgs(args);
        PartitionGraph initialModel = synMain.createInitialPartitionGraph();
        TemporalInvariantSet implicitInvariants = initialModel.getImplicitInvariants();
        
    	int numEventTypes = 8;
    	assertEquals("Number of mined invariants", 
    			numEventTypes * numEventTypes, implicitInvariants.numInvariants());
    	
    	int expectedNumCIFbys = 9;
    	Set<CanImmediatelyFollowedInvariant> CIFbys = new HashSet<CanImmediatelyFollowedInvariant>();
    	for (ITemporalInvariant invariant : implicitInvariants.getSet())
    		if (invariant instanceof CanImmediatelyFollowedInvariant)
    			CIFbys.add((CanImmediatelyFollowedInvariant) invariant);
    	assertEquals("Number of CIFby invariants", expectedNumCIFbys, CIFbys.size());

    	// TODO: test that the CIFbys are correct
    }
}