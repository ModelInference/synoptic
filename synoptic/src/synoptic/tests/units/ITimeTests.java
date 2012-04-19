package synoptic.tests.units;

import static org.junit.Assert.*;

import org.junit.Test;

import synoptic.tests.SynopticTest;
import synoptic.util.time.DTotalTime;
import synoptic.util.time.ITime;
import synoptic.util.time.ITotalTime;
import synoptic.util.NotImplementedException;
import synoptic.util.time.VectorTime;
import java.util.LinkedList;


public class ITimeTests extends SynopticTest {
	
	@Test (expected = IllegalArgumentException.class)
	@SuppressWarnings ("unused")
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
	
	@Test (expected = NotImplementedException.class)
	public void testExceptionVectorTimeIntConstructor() {
	    VectorTime v1 = new VectorTime(1); //arbitrary init number.
	    VectorTime v2 = new VectorTime(2);
	    
	    v2.computeDelta(v1);
	}
	
	@Test (expected = NotImplementedException.class)
	public void testExceptionVectorTimeVectorConstructor() {
	    LinkedList<Integer> l1 = new LinkedList<Integer>();
	    LinkedList<Integer> l2 = new LinkedList<Integer>();
	    
	    // Fill the list with some ints.
	    for (int i = 0; i < 5; i++) {
	        l1.add(i);
	        l2.add(i);
	    }
	    
	    VectorTime v1 = new VectorTime(l1);
	    VectorTime v2 = new VectorTime(l2);
	    
	    v1.computeDelta(v2);
	}
	
	@Test
	public void testDivisionITotalTIme() {
	    ITime t1 = new ITotalTime(10);
	    ITime t2 = t1.divBy(2);
	    ITime oracle = new ITotalTime(5);
	    
	    assertEquals(oracle, t2);
	}
	
	@Test
	public void testDivisionDTotalTime() {
	    ITime t1 = new DTotalTime(10);
	    ITime t2 = t1.divBy(2);
	    ITime oracle = new DTotalTime(5);
	    assertEquals(oracle, t2);
	}
	
	@Test
	public void testITotalTimeIncr() {
	    ITime t1 = new ITotalTime(1);
	    ITime t2 = new ITotalTime(5);
	    ITime result = t1.incrBy(t2);
	    ITime oracle = new ITotalTime(6);
	    assertEquals(oracle, result);
	}
	
	@Test (expected = NotImplementedException.class)
	public void testExceptionVectorTimeIncr() {
	    VectorTime v1 = new VectorTime(1);
	    VectorTime v2 = new VectorTime(2);
	    v1.incrBy(v2);
	}
}
