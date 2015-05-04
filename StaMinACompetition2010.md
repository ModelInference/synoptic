# Introduction #

---


The goal of an algorithm competing in the [StaMinA](http://stamina.chefbe.net/) competition is as follows: given a list of strings that satisfy (positive examples) and do not satisfy (negative examples) some _hidden_ FSM, produce this FSM or a similar FSM solely based on the input behavior. The output FSM is tested against a different set of data (not the input) for a score. This competition lasts until December, so we have plenty of time to participate.

# Data Details #

---


Input training set looks like:
```
+ 1 0 0 0 0 0 0 0 0
+ 0 1 1 0 0 0 1 0 1 0 1 0 1 0 0
- 0 1 0 0 0
+ 0 0
- 1 1 0 0 0 0 0 0 0 1 0 0
```

Input test set looks like:
```
? 0 1 0 0 0 0 0 1 1 1 0 1
? 0 1 1 0 1 0 0 0 0 0 1 1 1 0 0 0
? 0 1 0 0 0 1 0 1 1 1 0 0 0 0 1
? 0 0 1 1 1 1 0 1 0 1 0 1 1
? 1 0 0 0 1 1 0 1 1 1 1 0 1 0 0 1 1 1 0 1 0 0 0 0 0 1 1 1 0 1
```

Output for the test set above might look like:
```
01011
```

Where a bit in position i indicates whether the string on line i of the test set is accepted by the hidden FSM (1) or not accepted (0).


# Adapting Synoptic #

---


There are few ways in which we may minimally adapt Bikon to see how it performs in this competition:

  1. Add a new symbol to the FSM alphabet -- Accept.
  1. Modify input strings such that positive strings terminate with Accept symbol.
  1. Consider only the positive strings as input to Bikon, and run the algorithm until it terminates.
  1. Add an implicit invariant that negative examples are NeverFollowed by an Accept symbol.
  1. Consider the negative strings as counter-examples to some possibly `hidden` invariant that we may not surmise. If a negative string is not satisfied by the graph from the previous step then we find the first non-satisfying node and split it (as before). We do so under the constraint that after each such split the resulting graph must continue to satisfy the positive strings.

The changes above merely adapt Bikon. It would be interesting to see how this version of the algorithm would perform in the competition. An example of a more in-depth change to Bikon would be to augment the set of Bikon invariants to reason about negative strings. Also note that the symbols\messages in this competition do not have any structural data. So adding Daikon should not make a difference since no structural invariants can be derived.