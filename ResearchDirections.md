# Introduction #

Here is a list of various topics that we might want to pursue as part of the Synoptic project. This list is not presented in any particular order.

# Formal Analysis #

  * Explore the question of whether its possible to eliminate determinism from both split and merge to create versions of these operations that are duals, e.g. such that split(merge(G)) = merge(split(G)) = G. This would be helpful in developing hybrid algorithms that use of both merge() and split() operations for exploring the representation space.

# Alternative Splitting Policies #

  * Daikon can be used to find a good split of a set of messages according to the data-fields associated with the messages. A split could be evaluated by cross-validating the messages from one candidate partition against the invariants from the other candidate partition, and vice versa. The more messages that do not satisfy the invariants, the better the split. Message fields over which structural invariants should be generated could be specified within the protobuf format.

  * Use statistical methods. If Synoptic is used to find anomalous behaviour, it might be helpful to preserve (e.g. split into independent states) messages that appear rarely.

# Other Types of Invariants #

  * Other temporal invariants. Currently we use three types of temporal invariants to (1) determine which node to split (the first invalid node in a counter-example path -- path which does not satisfy an invariant), and (2) to know when to terminate the spitting process. Other temporal invariants could be useful, but we might want to the user to turn them on\off depending on what they know about the system. For example, (a) absolute time could be used to augment the existing three invariants -- e.g. A msg is followed by a TXA msg within 3 seconds; or (b) message counts could be used -- e.g. 8 DATA msgs are always followed by an ACK.

  * Structural invariants. Daikon invariants can be used for the same purpose as temporal invariants -- they can be used to find counter-example traces, and to determine termination of the splitting process.

# Integration with Distributed Systems #

  * Using a partial ordering of messages. We currently use a total ordering of messages as input to Synoptic. With the newly integrated LTL model checker we can expand this to partial ordering, which can more accurately capture ordering of messages in a distributed systems.

# Tool Implementation #

## Offline Tool Ideas ##

  * Enable the user to jump to specific logged messages responsible for a transition/state. For example, a user could click on a transition to see the messages that satisfy this transition. This would help to de-mystify the Synoptic representation with a concrete link between the representation and the logged messages.

  * Expose to the user the (temporal) invariants discovered and used by the tool to constrain the graph exploration algorithms. These can be inspected by the user to (1) partially verify correctness of the system output, and (2) understand the constraints the hold for the representation produced by Synoptic.

  * Command line Synoptic -- expose all options and allow user to specify input\output paths. This is a simple initial step to simplify tool use.

  * Replace Dot with a visualization library that allows to control graph layout. This is helpful in cases when users want to compare graphs from different runs. With dot, the graph layout may change dramatically when the graph structure only slightly different.

  * Cache the intermediate graphs and expose them with a user interface. This would allow a user to navigate and find that graph, which is most useful (i.e. sometimes a most compact\fully detailed representations are not the most useful).

  * Provide manual control over the split\merge operations. In the initial user study the user wanted to be able to control which node is split. Besides specifying the node, the user might also want to control the splitting condition. Perhaps analogous control can be provided for merge -- e.g. the user can specify a repeating message 'pattern' that should be merged. This pattern could be temporal to start with. This might also enable users to 'undo' unfavorable splits or merges.

  * Provide a msg-by-msg visualization of logged messages on top of graph representation. For example, a msg can be visualized by highlighting the corresponding transition/state in the graph. This would help the user reason about how logged sequences of messages correspond to paths in the graph. This would be especially helpful for graphs with cycles, or with non-deterministic transitions.

## Online Tool Ideas ##

  * Integrate Synoptic into Eclipse as a plugin. This plugin may be used to capture output to stdout or interpose in some other way to give immediate feedback of the messages received/sent by the local node so far in the execution. This could be done by re-running the Synoptic pipeline each time a new message is intercepted, but perhaps there is a better way.

  * Allow the use to express break-conditions in the Synoptic plugin to help debug local programs. A use case for this would be as follows: (1) user runs the program and inspects the Synoptic representation, (2) user notices a specific transition or sequence of transitions that appears to be illegal, (3) user instruct the Synoptic plugin to break program execution when this transition is detected pat run0time, (4) the program is eventually when the condition is satisfied and the user inspects the dynamic state of the program to understand why\how the condition occurred.

# Tool Evaluation #

  * Find systems at UW that might want to experiment with our tool.
    * The reverse traceroute project would probably want to participate, especially if we met some of the requests -- e.g. visualizing multiple steps of the algorithm, and allowing manual control of split\merge of nodes.