package synoptic.model;

/**
 * <p>
 * Provides basic data structures, such as graphs, transitions, and events.
 * </p>
 * <h3>Graphs</h3>
 * <p>
 * The main data-structure in Synoptic is a graph. In the implementation a graph
 * is represented by a set of nodes. Synoptic's graphs do not contain edges
 * themselves, but leave it to the nodes to store the edges. Edges of nodes can
 * be accessed via the interface <code>INode</code> which all classes
 * representing nodes must implement.
 * </p>
 * <p>
 * There are two important classes that implement the interface
 * <code>INode</code> and thus can be used to build graphs:
 * <code>EventNode</code> and <code>Partition</code>. <code>EventNode</code> is
 * used to build input graphs from raw data. This class provides methods to add
 * edges between different nodes of type <code>EventType</code>. A typical input
 * graph has type <code>StringEventType</code>.
 * </p>
 * <p>
 * A <code>Partition</code> is a set of <code>EventNode</code> instances, and
 * plays the role of a node in the <code>PartitionGraph</code>. Partitions
 * support merging and splitting of nodes, and do not store their edges
 * explicitly. Thus they have no direct means of inserting an edge. Instead,
 * they generate edges implicitly and on the fly from the <code>EventNode</code>
 * instances they contain. This has the advantage that edges are always
 * up-to-date and <code>EventNode</code>s can be moved freely among Partitions
 * (as long as they are contained in at most one partition at every given time).
 * </p>
 */
