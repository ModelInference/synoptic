package synoptic.tests.units;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import synoptic.algorithms.graph.TransitiveClosure;
import synoptic.invariants.AllRelationsTransitiveClosure;
import synoptic.model.Action;
import synoptic.model.Graph;
import synoptic.model.LogEvent;
import synoptic.model.Relation;

public class AllRelationsTransitiveClosureTests {
	
	@Test
	public void constructorSimpleTest(){
	
		Graph <LogEvent> g = new Graph<LogEvent>();
		LogEvent a = new LogEvent(new Action ("a"));
		LogEvent b = new LogEvent(new Action("b"));
		LogEvent c = new LogEvent(new Action("c"));
		LogEvent d = new LogEvent(new Action("d"));
		
		a.addTransition(new Relation<LogEvent>(a, b, "followed by"));
		a.addTransition(new Relation<LogEvent>(a, c, "after"));
		b.addTransition(new Relation<LogEvent>(b, d, "followed by"));
		
		g.add(a);
		g.add(b);
		g.add(c);
		g.add(d);
		
		AllRelationsTransitiveClosure <LogEvent> tcs = new AllRelationsTransitiveClosure<LogEvent>(g);
		
		assertEquals(2, tcs.getRelations().size());
	}
	
	@Test
	public void isReachableTest(){
		Graph <LogEvent> g = new Graph<LogEvent>();
		LogEvent a = new LogEvent(new Action ("a"));
		LogEvent b = new LogEvent(new Action("b"));
		LogEvent c = new LogEvent(new Action("c"));
		LogEvent d = new LogEvent(new Action("d"));
		
		a.addTransition(new Relation<LogEvent>(a, b, "followed by"));
		a.addTransition(new Relation<LogEvent>(a, c, "after"));
		b.addTransition(new Relation<LogEvent>(b, d, "followed by"));
		
		g.add(a);
		g.add(b);
		g.add(c);
		g.add(d);
		
		AllRelationsTransitiveClosure <LogEvent> tcs = new AllRelationsTransitiveClosure<LogEvent>(g);
		
		
		// Initially failed - changed header of isReachable to accept a String rather than Action
		assertTrue(tcs.isReachable(a, d, "followed by"));
		assertFalse(tcs.isReachable(a, d, "after"));
		
		assertTrue(tcs.isReachable(a, c, "after"));
	}
	
	@Test
	public void getTest(){
		Graph <LogEvent> g = new Graph<LogEvent>();
		LogEvent a = new LogEvent(new Action("a"));
		LogEvent b = new LogEvent(new Action("b"));
		LogEvent c = new LogEvent(new Action("c"));
		LogEvent d = new LogEvent(new Action("d"));
		
		a.addTransition(new Relation<LogEvent>(a, b, "followed by"));
		b.addTransition(new Relation<LogEvent>(b, c, "followed by"));
		c.addTransition(new Relation<LogEvent>(c, d, "followed by"));
		c.addTransition(new Relation<LogEvent>(c, a, "pow"));
		d.addTransition(new Relation<LogEvent>(d, c, "pow"));
		
		g.add(a);
		g.add(b);
		g.add(c);
		g.add(d);
		
		AllRelationsTransitiveClosure <LogEvent> tcs = new AllRelationsTransitiveClosure<LogEvent>(g);
		
		TransitiveClosure<LogEvent> tc = new TransitiveClosure<LogEvent> (g, "followed by");
		assertTrue(tc.isEqual(tcs.get("followed by")));
		
		TransitiveClosure<LogEvent> tc2 = new TransitiveClosure<LogEvent> (g, "pow");
		assertTrue(tc2.isEqual(tcs.get("pow")));
		
		assertFalse(tc.isEqual(tcs.get("pow")));
		assertFalse(tc2.isEqual(tcs.get("followed by")));
		
	}
	
	@Test
	public void getRelationsTest(){
		Graph <LogEvent> g = new Graph<LogEvent>();
		LogEvent a = new LogEvent(new Action ("a"));
		LogEvent b = new LogEvent(new Action("b"));
		LogEvent c = new LogEvent(new Action("c"));
		LogEvent d = new LogEvent(new Action("d"));
		
		a.addTransition(new Relation<LogEvent>(a, b, "followed by"));
		a.addTransition(new Relation<LogEvent>(a, c, "after"));
		b.addTransition(new Relation<LogEvent>(b, d, "followed by"));
		
		g.add(a);
		g.add(b);
		g.add(c);
		g.add(d);
		
		AllRelationsTransitiveClosure <LogEvent> tcs = new AllRelationsTransitiveClosure<LogEvent>(g);
		
		assertEquals(2, tcs.getRelations().size());
		
		Set<String> r = new HashSet<String>();
		r.add("followed by");
		r.add("after");
		
		assertTrue(r.equals(tcs.getRelations()));
		
		r.add("meh");
		assertFalse(r.equals(tcs.getRelations()));
	}
}