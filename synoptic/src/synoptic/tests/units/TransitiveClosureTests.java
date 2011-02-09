package synoptic.tests.units;



import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

import synoptic.algorithms.graph.TransitiveClosure;
import synoptic.model.Action;
import synoptic.model.Graph;
import synoptic.model.LogEvent;
import synoptic.model.Relation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;



/**
 * Tests for synoptic.algorithms.graph.TransitiveClosure class.
 *
 */

public class TransitiveClosureTests {
	
	/**
	 * Test constructor for simple case:
	 * 
	 * a --> b --> c --> d
	 * 
	 * should yield a TransitiveClosure with the following arrows representing
	 * true values in its tc map:
	 * 
	 * a --> b		b --> c		c --> d
	 * a --> c		b --> d
	 * a --> d
	 * 
	 * and no others
	 */
	@Test
	public void constructorSimpleTest() {
		Graph <LogEvent> g = new Graph<LogEvent>();
		LogEvent a = new LogEvent(new Action ("a"));
		LogEvent b = new LogEvent(new Action("b"));
		a.addTransition(new Relation<LogEvent>(a, b, "followed by"));
		LogEvent c = new LogEvent(new Action("c"));
		b.addTransition(new Relation<LogEvent>(b, c, "followed by"));
		LogEvent d = new LogEvent(new Action("d"));
		c.addTransition(new Relation<LogEvent>(c, d, "followed by"));
		g.add(a);
		g.add(b);
		g.add(c);
		g.add(d);

		TransitiveClosure<LogEvent> tc = new TransitiveClosure<LogEvent> (g, "followed by");
		
		assertTrue(tc.getTC().containsKey(a) && tc.getTC().get(a).get(b) && tc.getTC().get(a).get(c) && tc.getTC().get(a).get(d));
		assertTrue(tc.getTC().containsKey(b) && tc.getTC().get(b).get(c) && tc.getTC().get(b).get(d));
		assertTrue(tc.getTC().containsKey(c) && tc.getTC().get(c).get(d));
		
		assertFalse(tc.getTC().get(b).containsKey(a));
		assertFalse(tc.getTC().get(c).containsKey(b));
		assertFalse(tc.getTC().get(c).containsKey(a));
		assertFalse(tc.getTC().containsKey(d));
	}

	@Test
	public void isReachableSimpleTest(){
		Graph <LogEvent> g = new Graph<LogEvent>();
		LogEvent a = new LogEvent(new Action ("a"));
		LogEvent b = new LogEvent(new Action("b"));
		a.addTransition(new Relation<LogEvent>(a, b, "followed by"));
		LogEvent c = new LogEvent(new Action("c"));
		b.addTransition(new Relation<LogEvent>(b, c, "followed by"));
		LogEvent d = new LogEvent(new Action("d"));
		c.addTransition(new Relation<LogEvent>(c, d, "followed by"));
		g.add(a);
		g.add(b);
		g.add(c);
		g.add(d);
		
		TransitiveClosure<LogEvent> tc = new TransitiveClosure<LogEvent> (g, "followed by");
		
		ArrayList<LogEvent> list = new ArrayList<LogEvent>();
		list.add(a);
		list.add(b);
		list.add(c);
		list.add(d);
		
		for(LogEvent z : list)
			for(LogEvent y : list)
				if(list.indexOf(z) < list.indexOf(y))
					assertTrue(tc.isReachable(z, y));
				else
					assertFalse(tc.isReachable(z, y));
	}
	
	
	@Test
	public void constructorEmptyGraphTest(){
		Graph <LogEvent> g = new Graph<LogEvent>();
		TransitiveClosure<LogEvent> tc = new TransitiveClosure<LogEvent> (g, "followed by");
		assertTrue(tc.getTC().isEmpty());
	}
	
	@Test
	public void constructorNullRelationTest(){
		Graph <LogEvent> g = new Graph<LogEvent>();
		LogEvent a = new LogEvent(new Action ("a"));
		LogEvent b = new LogEvent(new Action("b"));
		a.addTransition(new Relation<LogEvent>(a, b, "followed by"));
		LogEvent c = new LogEvent(new Action("c"));
		b.addTransition(new Relation<LogEvent>(b, c, "followed by"));
		LogEvent d = new LogEvent(new Action("d"));
		c.addTransition(new Relation<LogEvent>(c, d, "followed by"));
		g.add(a);
		g.add(b);
		g.add(c);
		g.add(d);

		TransitiveClosure<LogEvent> tc = new TransitiveClosure<LogEvent> (g, null);
		assertTrue(tc.getTC().isEmpty());
	}
	
	/*
	 * 
	 * FAILS: TC cannot be passed null graph
	@Test
	public void constructorNullGraphTest(){
		TransitiveClosure<LogEvent> tc = new TransitiveClosure<LogEvent> (null, null);
		assertTrue(tc.getTC().isEmpty());
	}
	*
	*/
	
	@Test
	public void constructorTCCase(){
		Graph <LogEvent> g = new Graph<LogEvent>();
		LogEvent a = new LogEvent(new Action ("a"));
		LogEvent b = new LogEvent(new Action("b"));
		a.addTransition(new Relation<LogEvent>(a, b, "followed by"));
		LogEvent c = new LogEvent(new Action("c"));
		b.addTransition(new Relation<LogEvent>(b, c, "after"));
		LogEvent d = new LogEvent(new Action("d"));
		c.addTransition(new Relation<LogEvent>(c, d, "followed by"));
		g.add(a);
		g.add(b);
		g.add(c);
		g.add(d);
		
		TransitiveClosure<LogEvent> tc = new TransitiveClosure<LogEvent> (g, "followed by");
		
		HashMap<LogEvent, HashMap<LogEvent, Boolean>> tc2 = new HashMap<LogEvent, HashMap<LogEvent, Boolean>>();
		tc2.put(a, new HashMap<LogEvent, Boolean>());
		tc2.get(a).put(b, true);
		tc2.put(c, new HashMap<LogEvent, Boolean>());
		tc2.get(c).put(d, true);
		
		assertTrue(tc2.equals(tc.getTC()));
	
		tc2.put(d, new HashMap<LogEvent, Boolean>());
		
		assertFalse(tc2.equals(tc.getTC()));
		
	
	}
	
	
	@Test
	public void isEqualSimpleTest(){
		Graph <LogEvent> g = new Graph<LogEvent>();
		LogEvent a = new LogEvent(new Action ("a"));
		LogEvent b = new LogEvent(new Action("b"));
		a.addTransition(new Relation<LogEvent>(a, b, "followed by"));
		LogEvent c = new LogEvent(new Action("c"));
		b.addTransition(new Relation<LogEvent>(b, c, "followed by"));
		LogEvent d = new LogEvent(new Action("d"));
		c.addTransition(new Relation<LogEvent>(c, d, "followed by"));
		g.add(a);
		g.add(b);
		g.add(c);
		g.add(d);
		
		TransitiveClosure<LogEvent> tc = new TransitiveClosure<LogEvent> (g, "followed by");
		
		Graph <LogEvent> g2 = new Graph<LogEvent>();
		g2.add(a);
		g2.add(b);
		g2.add(c);
		g2.add(d);
		
		TransitiveClosure<LogEvent> tc2 = new TransitiveClosure<LogEvent> (g, "followed by");
		
		assertTrue(tc.isEqual(tc2));
		
	}
	
	/**
	 * Missing link case
	 */
	@Test
	public void constructorSimple2Test(){
		Graph <LogEvent> g = new Graph<LogEvent>();
		LogEvent a = new LogEvent(new Action ("a"));
		LogEvent b = new LogEvent(new Action("b"));
		a.addTransition(new Relation<LogEvent>(a, b, "followed by"));
		LogEvent c = new LogEvent(new Action("c"));
		b.addTransition(new Relation<LogEvent>(b, c, "after"));
		LogEvent d = new LogEvent(new Action("d"));
		c.addTransition(new Relation<LogEvent>(c, d, "followed by"));
		g.add(a);
		g.add(b);
		g.add(c);
		g.add(d);
		
		TransitiveClosure<LogEvent> tc = new TransitiveClosure<LogEvent> (g, "followed by");
		
		assertTrue(tc.getTC().containsKey(a) && tc.getTC().get(a).get(b));
		assertTrue(tc.getTC().containsKey(c) && tc.getTC().get(c).get(d));
		
		assertFalse(tc.getTC().get(a).containsKey(c));
		assertFalse(tc.getTC().get(a).containsKey(d));
		assertFalse(tc.getTC().get(c).containsKey(b));
		assertFalse(tc.getTC().get(c).containsKey(a));
		assertFalse(tc.getTC().containsKey(d));
		assertFalse(tc.getTC().containsKey(b));
	}
	
	/**
	 * Circular case
	 */
	@Test
	public void constructorSimple3Test(){
		Graph <LogEvent> g = new Graph<LogEvent>();
		LogEvent a = new LogEvent(new Action ("a"));
		LogEvent b = new LogEvent(new Action("b"));
		a.addTransition(new Relation<LogEvent>(a, b, "followed by"));
		LogEvent c = new LogEvent(new Action("c"));
		b.addTransition(new Relation<LogEvent>(b, c, "followed by"));
		LogEvent d = new LogEvent(new Action("d"));
		c.addTransition(new Relation<LogEvent>(c, d, "followed by"));
		d.addTransition(new Relation<LogEvent>(d, a, "followed by"));
		g.add(a);
		g.add(b);
		g.add(c);
		g.add(d);
		
		TransitiveClosure<LogEvent> tc = new TransitiveClosure<LogEvent> (g, "followed by");
		
		for(LogEvent z : g.getNodes())
			for(LogEvent y : g.getNodes())
				assertTrue(tc.getTC().containsKey(z) && tc.getTC().get(z).get(y));
	}
}