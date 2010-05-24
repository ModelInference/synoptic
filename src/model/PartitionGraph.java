package model;

import invariants.TemporalInvariantSet;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import model.interfaces.IGraph;
import model.interfaces.IModifiableGraph;
import model.interfaces.ITransition;

import algorithms.graph.GraphUtil;
import algorithms.graph.Operation;


public class PartitionGraph implements IGraph<Partition> {
	/** holds all partitions in this graph */
	private Set<Partition> partitions = null;
	/**
	 * holds all initial messages in this graph, grouped by the relation w.r.t.
	 * which they are initial
	 */
	private HashMap<Action, Set<MessageEvent>> initialMessages = new HashMap<Action, Set<MessageEvent>>();

	/** maintains the corresponding state graph */
	private Graph<SystemState<Partition>> stateGraph;

	/** holds invariants that were mined when the graph was created */
	private TemporalInvariantSet invariants;

	/** holds all relations known to exist in this graph */
	private Set<Action> relations = new HashSet<Action>();

	public PartitionGraph(IGraph<MessageEvent> g) {
		this(g, false);
	}

	public PartitionGraph(IGraph<MessageEvent> g, boolean partitionByLabel) {
		for (Action relation : g.getRelations()) {
			addInitialMessages(g.getInitialNodes(relation), relation);
			relations.add(relation);
		}
		if (partitionByLabel)
			partitionByLabels(g.getNodes());
		else
			partitionSeparately(g.getNodes());
		stateGraph = GraphUtil.convertPartitionGraphToStateGraph(this);
		invariants = TemporalInvariantSet.computeInvariants(g);
	}

	private void addInitialMessages(Set<MessageEvent> initialMessages, Action relation) {
		if (!this.initialMessages.containsKey(relation))
			this.initialMessages.put(relation, new HashSet<MessageEvent>());
		this.initialMessages.get(relation).addAll(initialMessages);
	}

	public IGraph<SystemState<Partition>> getSystemStateGraph() {
		return stateGraph;
	}

	public TemporalInvariantSet getInvariants() {
		return invariants;
	}

	public Partition partitionFromMessage(MessageEvent message) {
		return message.getParent();
	}
	
	public Operation apply(Operation op) {
		return op.commit(this, modifiableInterface, stateGraph);
	}

	private void partitionByLabels(Collection<MessageEvent> messages) {
		partitions = new HashSet<Partition>();
		final Map<String, Partition> prepartitions = new HashMap<String, Partition>();
		for (MessageEvent message : messages) {
			for (ITransition<MessageEvent> t : message.getTransitions())
				relations.add(t.getAction());
			if (!prepartitions.containsKey(message.getLabel())) {
				final Partition partition = new Partition(
						new HashSet<MessageEvent>(),
						new HashSet<SystemState<Partition>>(), null);
				partitions.add(partition);
				prepartitions.put(message.getLabel(), partition);
			}
			prepartitions.get(message.getLabel()).addMessage(message);
		}
	}

	private void partitionSeparately(Collection<MessageEvent> messages) {
		partitions = new HashSet<Partition>();
		final Map<MessageEvent, Partition> prepartitions = new HashMap<MessageEvent, Partition>();
		for (MessageEvent message : messages) {
			if (!prepartitions.containsKey(message)) {
				final Partition partition = new Partition(
						new HashSet<MessageEvent>(),
						new HashSet<SystemState<Partition>>(), null);
				partitions.add(partition);
				prepartitions.put(message, partition);
			}
			prepartitions.get(message).addMessage(message);
		}
	}

	@Override
	public Set<Partition> getNodes() {
		return partitions;
	}

	@Override
	public Set<Partition> getInitialNodes() {
		Set<Partition> initial = new HashSet<Partition>();
		for (Action relation : getRelations())
			initial.addAll(getInitialNodes(relation));
		return initial;
	}

	@Override
	public Set<Partition> getInitialNodes(Action relation) {
		Set<Partition> initialNodes = new HashSet<Partition>();
		for (MessageEvent m : initialMessages.get(relation)) {
			initialNodes.add(m.getParent());
		}
		return initialNodes;
	}

	public Set<Partition> getPartitions() {
		return partitions;
	}

	@Override
	public Set<Action> getRelations() {
		return relations;
	}

	private IModifiableGraph<Partition> modifiableInterface = new IModifiableGraph<Partition>() {
		public void add(Partition node) {
			for (MessageEvent m : node.getMessages())
					relations.addAll(m.getRelations());
			partitions.add(node);
		}

		public void addInitial(Partition initialNode, Action relation) {
			partitions.add(initialNode);
			addInitialMessages(initialNode.getMessages(), relation);
		}

		public void remove(Partition node) {
			partitions.remove(node);
		}

		public Set<Partition> getInitialNodes() {
			return PartitionGraph.this.getInitialNodes();
		}

		public Set<Partition> getInitialNodes(Action relation) {
			return PartitionGraph.this.getInitialNodes(relation);
		}

		public Set<Partition> getNodes() {
			return PartitionGraph.this.getNodes();
		}

		public Set<Action> getRelations() {
			return PartitionGraph.this.getRelations();
		}
	};

	public Set<MessageEvent> getInitialMessages() {
		Set<MessageEvent> initial = new HashSet<MessageEvent>();
		for (Set<MessageEvent> set : initialMessages.values())
			initial.addAll(set);
		return initial;
	}
}