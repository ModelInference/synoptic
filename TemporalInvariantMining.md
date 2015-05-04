# Invariants #

Each event belongs to exactly one event class. Invariants capture temporal properties among event classes. The following three types of invariants are considered:

  * _x_ AFby _y_ An event of class _x_ is always followed by an event of class _y_.
  * _x_ NFby _y_ An event of class _x_ is never followed by an event of class _y_.
  * _x_ AP _y_ An event of class _x_ always precedes an event of class _y_, i.e., whenever an event of class _y_ occured, an event of class _x_ has occured previously.



# Mining #
## General Idea ##
The general idea is to enumerate possible invariants and test whether they hold. This is the Daikon approach applied to temporal invariants.

## Over-approximation ##
Instead of generating all possible invariants, we use a heuristic to over-approximate the set of invariants. Over-approximation here means that invariants that hold must be found, but not all found invariants must hold. The over-approximation will still be much smaller than the naive set of all possible invariants. We benchmarked the speedup and found that on average 75% less invariants were generated compared to naive invariant template instantiation.

## The over-approximation algorithm ##
Input: a graph representing the traces.

Output: an over approximation of the set of invariants that hold over the events in the graph.

  * Generate the transitive closure TC of the input, without doing any summarization or merging of nodes (if the input has several relations, generate transitive closures for each relation separately)
  * To discover invariants for event classes _x_,_y_ of type AFby, consider all events of class _x_ and check whether each event has a direct successor event of class _y_ in TC.
  * To discover invariants for event classes _x_,_y_ of type NFby consider all events of class _x_ and check that none has a direct successor of class _y_ in TC.
  * To discover invariants for event classes _x_,_y_ of type AP consider all events of type _y_ and check that each event has a direct predecessor of class _x_ in TC.

The `TemporalInvariantSet` class implements this algorithm.

Proof that the over-approximation algorithm produces an over approximation (by contradiction):
  * Suppose the algorithm fails to produce the invariant `i` = _x_ AFby _y_, but _x_ is always followed by _y_ in the input graph.  For the algorithm to have failed to output `i`, the TC must not have had an edge from _x_ to _y_, which, in turn, means there must not have been a path from _x_ to _y_ in the original graph input.  However, that is a contradiction because we assumed that _x_ is always followed by _y_ in the input graph.
  * Suppose the algorithm fails to produce the invariant `i` = _x_ NFby _y_, but _x_ is never followed by _y_ in the input graph.  For the algorithm to have failed to output `i`, the TC must have had at least one edge from _x_ to _y_, which, in turn, means there must have been at least one path from _x_ to _y_ in the original graph input.  However, that is a contradiction because we assumed that _x_ is never followed by _y_ in the input graph.
  * Suppose the algorithm fails to produce the invariant `i` = _x_ AP _y_, but _x_ always precedes _y_ in the input graph.  For the algorithm to have failed to output `i`, the TC must have contained a least one _y_ which is not preceded by an _x_, which, in turn, means there must be a _y_ in the original graph input to which, for all _x_, no path exists from that _x_.  However, that is a contradiction because we assumed that _x_ always precedes _y_ in the input graph.

Q.E.D.

Note that the over-approximation algorithm may, in fact, return an over approximation by including invariants that do not hold.  For example:

  * AFby: Suppose the input graph has three nodes: _a_, _b_, and _c_, such that _a_ transitions to _b_ and to _c_ and no other transitions are legal.  Then the TC will look the same as the input graph and the algorithm will report _a_ AFby _b_, even though _a_ is sometims followed by _c_, not _b_.

  * NFby: (YB: did we come up with an example of a spurious NFby invariant or are these not possible?)

> (IB: I think these are not possible. Proof: Assume (1) x NFby y is false for G, and (2) there is no edge x -> y in TC of G. Then (2) implies that there exists no path from x -> y in G. But (1) implies that there is at least one path from some x to some y in G. Contradiction.)

  * AP: Suppose the input graph has three nodes: _a_, _b_, and _c_, such that _a_ transitions to _b_, _b_ transitions to _c_, _c_ transitions to _a_, and no other transitions are legal.  Then the TC will have the _a_-_b_-_c_ cycle and loops on each node.  Then the algorithm will report _a_ AP _b_ even though the original graph may encode the two traces of events _b_ _c_ _a_ and _a_ _b_ _c_.

> (MDE: I don't understand this example.  What are the edges in the input graph?  Does it already have a cycle?  Or, if there are two distinct a events, then there won't be a cycle in the approximated graph.)


(MDE: It's important that the algorithm treat each distinct "chunk" of the input, such as a file, separately, or else spurious AFby and AP events can occur.  I assume this is properly handled, since the model checker must do this also.)


## Rejecting spurious invariants ##
Each invariant discovered in the previous step is then checked using a model checker. If the invariant does not hold, it is rejected, otherwise it is retained. The set of retained invariants is the result of the mining process.

## Pseudo Code ##
Note that node reachability is provided by the transitive closure.
```
for each pair of labels as (label1,label2)
  set neverFollowed = false
  set alwaysFollowedBy = true
  set alwaysPrecededBy = true
  for each node with label1 as node1
    set followerFound = false
    set predecessorFound = false
    for each node with label2 as node1
      if node1 reaches node2 then
        neverFollowedBy = false
        followerFound = true
      end
      if node2 reaches node1 then
        predecessorFound = true
      end
    end for
    if not followerFound then
      alwaysFollowedBy = false
    end
    if not predecessorFound then
      alwaysPreceded = false
    end
  end for
  if neverFollowed then 
    issue label1 NFBy label2
  end
  if alwaysFollowedBy then
    issue label1 AFBy label2
  end
  if alwaysPreceded then
    issue label1 APBy label2
  end 
end for
```

# TODOs #
  * Prove that model checking is sound, in particular with regard to the transformation we make to the graph before applying the model checking.