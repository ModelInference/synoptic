package algorithms.graph;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import util.IterableIterator;


import model.Action;
import model.Graph;
import model.MessageEvent;
import model.MessageToPartitionIterator;
import model.Partition;
import model.PartitionGraph;
import model.Relation;
import model.SystemState;
import model.input.IBuilder;
import model.interfaces.IGraph;
import model.interfaces.INode;
import model.interfaces.ISuccessorProvider;
import model.interfaces.ITransition;
import model.nets.Event;
import model.nets.Net;

public class GraphUtil {
	public static Graph<SystemState<Partition>> convertPartitionGraphToStateGraph(PartitionGraph pg) {
		Graph<SystemState<Partition>> graph = new Graph<SystemState<Partition>>();
		Set<Partition> initialPartitions = pg.getInitialNodes();
		final String relation = "";

		for (final Partition p : initialPartitions) {
			SystemState<Partition> initial = new SystemState<Partition>("I-"
					+ p.getLabel());
			initial.addSuccessorProvider(new ISuccessorProvider<Partition>() {
				Set<MessageEvent> messages = new HashSet<MessageEvent>(p.getMessages());
				
				public IterableIterator<Partition> getSuccessorIterator() {
					return new MessageToPartitionIterator(messages.iterator());
				}

				@Override
				public void setTarget(SystemState<Partition> s) {
					// Do Nothing. We are successor provider for the initial node
				}

				@Override
				public IterableIterator<Partition> getSuccessorIterator(String relation) {
					return new MessageToPartitionIterator(messages.iterator(), relation);
				}
			});
			p.addSource(initial);
			graph.add(initial);
			graph.addInitial(initial, relation);
		}

		for (Partition p : pg.getNodes()) {
			SystemState<Partition> s = new SystemState<Partition>("P-"
					+ p.getLabel());
			graph.add(s);
			p.setTarget(s);
			s.addSuccessorProvider(p);
		}

		for (Partition m : pg.getNodes()) {
			for (Iterator<Relation<Partition>> iter = m
					.getTransitionsIterator(); iter.hasNext();) {
				ITransition<Partition> t = iter.next();
				INode<Partition> n = t.getTarget();
				((Partition) n).addSource(m.getTarget());
			}
		}
		return graph;
	}
	
	public static <T extends INode<T>, U> void copyTo(IGraph<T> graph, IBuilder<U> builder) {
		HashMap<T, U> map = new HashMap<T, U>();
		for (T node : graph.getNodes())
			map.put(node, builder.insert(new Action(node.getLabel())));
		for (T node : graph.getNodes()) {
			boolean foundTransition = false;
			for (ITransition<T> t : node.getTransitionsIterator()) {
				foundTransition = true;
				builder.connect(map.get(t.getSource()), map.get(t.getTarget()), t.getAction());
			}
			if (!foundTransition)
				builder.setTerminal(map.get(node));
		}
	}
	
	public static <T extends INode<T>, U> void copyReverseTo(IGraph<T> graph, IBuilder<U> builder) {
		HashMap<T, U> map = new HashMap<T, U>();
		for (T node : graph.getNodes())
			map.put(node, builder.insert(new Action(node.getLabel())));
		for (T node : graph.getNodes()) {
			boolean foundTransition = false;
			for (ITransition<T> t : node.getTransitionsIterator()) {
				foundTransition = true;
				builder.connect(map.get(t.getTarget()), map.get(t.getSource()), t.getAction());
			}
			if (!foundTransition)
				builder.setTerminal(map.get(node));
		}
	}

	public static void heuristicTransitiveReduction(Graph<MessageEvent> graph, String relation) {
		TransitiveClosure<MessageEvent> tc = new TransitiveClosure<MessageEvent>(graph, relation);
		HashSet<ITransition<MessageEvent>> essential = new HashSet<ITransition<MessageEvent>>();
		boolean change = true;
		while (change) {
			Relation<MessageEvent> removed = null;
			MessageEvent m = null;
			for (MessageEvent node : graph.getNodes()) {
				for (Relation<MessageEvent> t : node.getTransitionsIterator()) {
					if (!essential.contains(t)) {
						removed = t;
						m = node;
						break;
					}
				}
			}
			if (removed == null)
				break;
			m.removeTransitions(Collections.singletonList(removed));
			
			TransitiveClosure<MessageEvent> newTc = new TransitiveClosure<MessageEvent>(graph, relation);
			if (!tc.isEqual(newTc)) {
				essential.add(removed);
				m.addTransition(removed);
			}
		}
	}

	public static <T> void copyNetTo(Net net, IBuilder<T> gBuilder) {
		HashMap<Event, T> map = new HashMap<Event, T>();
		String relation = "";
		for (Event e : net.getEvents()) {
			T t = gBuilder.insert(new Action(e.getName()));
			map.put(e, t);
			if (e.getPostEvents().size() == 0)
				gBuilder.setTerminal(t);
			if (net.getPreEvents(e).size() == 0)
				gBuilder.addInitial(t, relation );
		}
		for (Event e : net.getEvents()) {
			for (Event f : e.getPostEvents()) {
				gBuilder.connect(map.get(e), map.get(f), relation);
			}
		}
	}

}
