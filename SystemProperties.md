# Introduction #

We are taking messages to mean 2+ fields : source, destination, and other fields.

For each of the properties, come up with an example distributed algorithm the debugging/development/testing of which would be made easier if the property could be verified for the implementation.

Example of distributed algorithms to use: TCP, Paxos, MapReduce, Two Phase Commit, etc.


# Data-oriented Properties (on msg as well as on fields of msgs) #

  * Sequence
    * Increasing sequence of numbers (e.g. TCP seq number)

  * Equality

  * Set operations
    * Intersection, inclusion, union, difference

  * Inequality Constraints
    * Less than, greater than

  * Constant

  * Field sizes
    * Useful for understanding when the system is performing a significant data transfer, and when its sending small amounts of data


# Temporal Properties (concrete) #

  * Permutation on a set on **n** messages
    * Alternation of two messages

  * Ordering of messages on a single node
    * Always sends ACK after FIN from the same node

  * Ordering across nodes
    * Ping-pong between two nodes

  * Periodicity of messages

  * More arbitrary function on the span of time between messages
