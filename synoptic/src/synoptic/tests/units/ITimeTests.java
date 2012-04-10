package synoptic.tests.units;

import static org.junit.Assert.*;

import org.junit.Test;

import synoptic.tests.SynopticTest;
import synoptic.util.time.DTotalTime;
import synoptic.util.time.ITime;


public class ITimeTests extends SynopticTest {
	
	@Test (expected = IllegalArgumentException.class)
	public void testNegativeDelta() {
		DTotalTime d1 = new DTotalTime(1.0);
		DTotalTime d2 = new DTotalTime(2.0);
		ITime delta = d1.computeDelta(d2);
	}
	
	@Test
	public void testPositiveDelta() {
		DTotalTime d1 = new DTotalTime(2.0);
		DTotalTime d2 = new DTotalTime(1.0);
		ITime delta = d1.computeDelta(d2);
		assertEquals(new DTotalTime(1.0), delta);
	}
}
