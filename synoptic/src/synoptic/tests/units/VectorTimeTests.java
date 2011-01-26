package synoptic.tests.units;

import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

import synoptic.model.input.VectorTime;

/**
 * Tests for synoptic.model.input.VectorTime class
 * 
 * @author ivan
 */
public class VectorTimeTests {
	
	@Test
	public void constructorNoExceptionsTest() {
		VectorTime v1;
		v1 = new VectorTime("1,2,3");
		v1 = new VectorTime(Arrays.asList(new Integer[]{1,2,3}));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void constructorWithException1Test() {
		VectorTime v1;
		v1 = new VectorTime("-1,2,3");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void constructorWithException2Test() {
		VectorTime v1;
		v1 = new VectorTime(Arrays.asList(new Integer[]{1,-2,3}));
	}
	
	@Test
	public void equalityTest() {
		VectorTime v1, v2, v3, v4;
		
		v1 = new VectorTime("1,2,3");
		v2 = new VectorTime(Arrays.asList(new Integer[]{1,2,3}));
		assertTrue(v1.equals(v2));
		
		assertFalse(v1.equals(null));
		
		v3 = new VectorTime(Arrays.asList(new Integer[]{1,2,4}));
		assertFalse(v1.equals(v3));
		assertFalse(v3.equals(v1));
		
		v4 = new VectorTime(Arrays.asList(new Integer[]{1,2,3,5}));
		assertFalse(v1.equals(v4));
		assertFalse(v4.equals(v1));
	}
	
	@Test
	public void stepTest() {
		VectorTime v1, v2;
		v1 = new VectorTime("1,2,3");
		v2 = new VectorTime("1,2,4");
		
		assertFalse(v1.equals(v2));
		v1 = v1.step(2);
		assertTrue(v1.equals(v2));
	}
	
	@Test
	public void isSingularTest() {
		VectorTime v1, v2;
		v1 = new VectorTime("1,2,3");
		v2 = new VectorTime("1");
		assertFalse(v1.isSingular());
		assertTrue(v2.isSingular());
	}
	
	@Test
	public void isUnitVectorTest() {
		VectorTime v1;
		v1 = new VectorTime("0");
		assertFalse(v1.isUnitVector());
		v1 = new VectorTime("0,1,0");
		assertTrue(v1.isUnitVector());
		v1 = new VectorTime("1");
		assertTrue(v1.isUnitVector());
		v1 = new VectorTime("2");
		assertFalse(v1.isUnitVector());
		v1 = new VectorTime("0,0,2");
		assertFalse(v1.isUnitVector());
		v1 = new VectorTime("1,2,3");
		assertFalse(v1.isUnitVector());
	}
	
	@Test
	public void lessThanTest() {
		VectorTime v1, v2;
		//   Same lengths
		v1 = new VectorTime("1,2,3");
		v2 = new VectorTime("1,2,4");
		assertTrue(v1.lessThan(v2));
		assertFalse(v2.lessThan(v1));
		
		v2 = new VectorTime("1,2,3");
		assertFalse(v1.lessThan(v2));
		assertFalse(v2.lessThan(v1));

		// Different lengths: always non-comparable
		v1 = new VectorTime("1,2,3,0");
		v2 = new VectorTime("1,2,4");
		assertFalse(v1.lessThan(v2));
		assertFalse(v2.lessThan(v1));

		v1 = new VectorTime("1,2,3,1");
		v2 = new VectorTime("1,2,4");
		assertFalse(v1.lessThan(v2));
		assertFalse(v2.lessThan(v1));
	}
	
	@Test
	public void toStringTest() {
		VectorTime v1, v2;
		v1 = new VectorTime("1,2,3,1");
		String s = v1.toString();
		assertTrue(s != null);
	}
		
}
