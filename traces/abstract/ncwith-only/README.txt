This is a partially ordered log that contains just a single temporal
invariant that will mined by Synoptic:

a_node1 NeverConcurrentWith b_node2


This invariant is read as:

'The "a" event at node1 is never concurrent with the "b" event at node2.'


This invariant means that whenever the 'a' event at node1 co-occurs
with event 'b' at node2, the two events are always totally ordered.
