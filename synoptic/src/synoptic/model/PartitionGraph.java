package synoptic.model;


import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import synoptic.algorithms.graph.IOperation;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.model.interfaces.IGraph;
import synoptic.model.interfaces.IModifiableGraph;
import synoptic.model.interfaces.ITransition;


/**
 * This class implements a partition graph. Nodes are sets of messages ({@code MessageEvent}) and edges 
 * are not maintained explicitly, but generated on-the-fly by class {@code Partition}. PartitionGraph maintains
 * a member {@code stateGraph} which represents the state based graph version of the partition graph. To ensure
 * that {@code stateGraph} reflects the current shape of the graph, PartitionGraphs can only be modified via 
 * the method {@code apply} which takes a object implementing {@code IOperation}. Operations must perform changes
 * on both representations. 
 * 
 * @author sigurd
 *
 */
public class PartitionGraph implements IGraph<Partition> {
	/** holds all partitions in this graph */
	private LinkedHashSet<Partition> partitions = null;
	/**
	 * holds all initial messages in this graph, grouped by the relation w.r.t.
	 * which they are initial
	 * We keep track of initial (rhombus) partitions by keeping track of the initial messages
	 * but we need to do this for every relation, which is specified by the first string arg in the hashmap
	 */
	private LinkedHashMap<String, Set<MessageEvent>> initialMessages = new LinkedHashMap<String, Set<MessageEvent>>();

	/** holds synoptic.invariants that were mined when the graph was created */
	private TemporalInvariantSet invariants;

	/** holds all relations known to exist in this graph */
	private Set<String> relations = new LinkedHashSet<String>();

	public PartitionGraph(IGraph<MessageEvent> g) {
		this(g, false);
	}

	/**
	 * Construct a PartitionGraph. Invariants from {@code g} will be extracted
	 * and stored. If partitionByLabel is true, all messages with identical
	 * labels in {@code g} will become one partition. Otherwise, every message
	 * gets its own partition (useful if only coarsening is to be performed)
	 * 
	 * @param g - the initial graph
	 * @param partitionByLabel - whether initial partitioning by label should be done
	 */
	public PartitionGraph(IGraph<MessageEvent> g, boolean partitionByLabel) {
		for (String relation : g.getRelations()) {
			addInitialMessages(g.getInitialNodes(relation), relation);
			relations.add(relation);
		}
		if (partitionByLabel)
			partitionByLabels(g.getNodes(), g.getInitialNodes());
		else
			partitionSeparately(g.getNodes());
		
		/* compute the synoptic.invariants for the graph! */
		invariants = TemporalInvariantSet.computeInvariants(g);
		/*****************************************/
		
		//System.out.println(synoptic.invariants.size() + " synoptic.invariants found.");
	}

	private void addInitialMessages(Set<MessageEvent> initialMessages,
			String relation) {
		if (!this.initialMessages.containsKey(relation))
			this.initialMessages.put(relation, new LinkedHashSet<MessageEvent>());
		this.initialMessages.get(relation).addAll(initialMessages);
	}

	public TemporalInvariantSet getInvariants() {
		return invariants;
	}

	public Partition partitionFromMessage(MessageEvent message) {
		return message.getParent();
	}

	public IOperation apply(IOperation op) {
		return op.commit(this, modifiableInterface);
	}

	private void partitionByLabels(Collection<MessageEvent> messages,
			Set<MessageEvent> initial) {
		partitions = new LinkedHashSet<Partition>();
		final Map<String, Partition> prepartitions = new LinkedHashMap<String, Partition>();
		for (MessageEvent message : messages) {
			for (ITransition<MessageEvent> t : message.getTransitions())
				relations.add(t.getRelation());
			if (!prepartitions.containsKey(message.getLabel())) {
				final Partition partition = new Partition(
						new LinkedHashSet<MessageEvent>());
				partitions.add(partition);
				prepartitions.put(message.getLabel(), partition);
			}
			prepartitions.get(message.getLabel()).addMessage(message);
		}
	}

	private void partitionByLabelsAndInitial(Collection<MessageEvent> messages,
			Set<MessageEvent> initial) {
		partitions = new LinkedHashSet<Partition>();
		final Map<String, Partition> prepartitions = new LinkedHashMap<String, Partition>();
		for (MessageEvent message : messages) {
			for (ITransition<MessageEvent> t : message.getTransitions())
				relations.add(t.getRelation());
			if (!prepartitions.containsKey(message.getLabel())) {
				final Partition partition = new Partition(
						new LinkedHashSet<MessageEvent>());
				prepartitions.put(message.getLabel(), partition);
			}
			prepartitions.get(message.getLabel()).addMessage(message);
		}
		for (Partition t : prepartitions.values()) {
			LinkedHashSet<MessageEvent> iSet = new LinkedHashSet<MessageEvent>();
			for (MessageEvent e : t.getMessages()) {
				if (initial.contains(e))
					iSet.add(e);
			}
			if (iSet.size() == 0)
				partitions.add(t);
			else {
				t.removeMessages(iSet);
				partitions.add(t);
				partitions.add(new Partition(iSet));
			}
		}
	}

	private void partitionSeparately(Collection<MessageEvent> messages) {
		partitions = new LinkedHashSet<Partition>();
		final Map<MessageEvent, Partition> prepartitions = new LinkedHashMap<MessageEvent, Partition>();
		for (MessageEvent message : messages) {
			if (!prepartitions.containsKey(message)) {
				final Partition partition = new Partition(
						new LinkedHashSet<MessageEvent>());
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
		Set<Partition> initial = new LinkedHashSet<Partition>();
		for (String relation : getRelations())
			initial.addAll(getInitialNodes(relation));
		return initial;
	}

	@Override
	public Set<Partition> getInitialNodes(String relation) {
		Set<Partition> initialNodes = new LinkedHashSet<Partition>();
		Set<MessageEvent> initMsgs = initialMessages.get(relation);
		if (initMsgs == null) {
			return initialNodes;
		}
		for (MessageEvent m : initMsgs) {
			initialNodes.add(m.getParent());
		}
		return initialNodes;
	}

	public Set<Partition> getPartitions() {
		return partitions;
	}

	@Override
	public Set<String> getRelations() {
		return relations;
	}
	
	/**
	 * An anonymous class instance implementing ModifiableGraph<Partition>, which
	 * can be passed to methods that must modify this PartitionGraph instance.
	 */
	private IModifiableGraph<Partition> modifiableInterface = new IModifiableGraph<Partition>() {
		public void add(Partition node) {
			for (MessageEvent m : node.getMessages())
				relations.addAll(m.getRelations());
			partitions.add(node);
		}

		public void addInitial(Partition initialNode, String relation) {
			partitions.add(initialNode);
			addInitialMessages(initialNode.getMessages(), relation);
		}

		public void remove(Partition node) {
			partitions.remove(node);
		}

		public Set<Partition> getInitialNodes() {
			return PartitionGraph.this.getInitialNodes();
		}

		public Set<Partition> getInitialNodes(String relation) {
			return PartitionGraph.this.getInitialNodes(relation);
		}

		public Set<Partition> getNodes() {
			return PartitionGraph.this.getNodes();
		}

		public Set<String> getRelations() {
			return PartitionGraph.this.getRelations();
		}
	};

	public Set<MessageEvent> getInitialMessages() {
		Set<MessageEvent> initial = new LinkedHashSet<MessageEvent>();
		for (Set<MessageEvent> set : initialMessages.values())
			initial.addAll(set);
		return initial;
	}

	/**
	 * Check that all partitions are non-empty and disjunct.
	 */
	public void checkSanity() {
		int totalCount = 0;
		Set<MessageEvent> all = new LinkedHashSet<MessageEvent>();
		for (Partition p : getNodes()) {
			if (p.size() == 0)
				throw new RuntimeException("bisim produced empty partition!");
			all.addAll(p.getMessages());
			totalCount += p.size();
		}
		if (totalCount != all.size())
			throw new RuntimeException(
					"partitions are not partitioning messages (overlap)!");
	}
}