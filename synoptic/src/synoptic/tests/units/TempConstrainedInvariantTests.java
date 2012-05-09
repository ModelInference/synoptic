package synoptic.tests.units;

import static org.junit.Assert.*;

import org.junit.Test;

import synoptic.invariants.AlwaysFollowedInvariant;
import synoptic.invariants.BinaryInvariant;
import synoptic.invariants.constraints.IThresholdConstraint;
import synoptic.invariants.constraints.TempConstrainedInvariant;
import synoptic.invariants.constraints.UpperBoundConstraint;
import synoptic.invariants.miners.ChainWalkingTOInvMiner;
import synoptic.main.parser.ParseException;
import synoptic.tests.SynopticTest;
import synoptic.util.time.DTotalTime;

public class TempConstrainedInvariantTests extends SynopticTest {

	@Override
    public void setUp() throws ParseException {
        super.setUp();
    }
	
	@Test
	public void unconstrainedAndConstrainedInvEqualityTest() {
		AlwaysFollowedInvariant unconstrainedInv = new AlwaysFollowedInvariant("a", "b", "t");
		
		IThresholdConstraint upper = new UpperBoundConstraint(new DTotalTime(2.0));
		TempConstrainedInvariant<AlwaysFollowedInvariant> constrInv = new TempConstrainedInvariant<AlwaysFollowedInvariant>(
                 unconstrainedInv, upper);
		 
		assertFalse(unconstrainedInv.equals(constrInv));
	}

}
