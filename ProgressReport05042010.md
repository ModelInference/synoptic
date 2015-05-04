**Table of Contents:**




# Last Week : ProgressReport04272010 #

---


# This Week #

---


We integrated Daikon with Synoptic, and tried to mine structural invariants to enhance the final representation. The first trace we tried was the uptime monitor with status request. A trace looks in general like

```
ping -> pong -> status
```

In this trace `status` only follows `pong` if the payload of `pong` is 1.

The graph obtained from per-node traces is:

![http://wiki.synoptic.googlecode.com/hg/images/ping-pong-invariants/ping-minimized.dot.png](http://wiki.synoptic.googlecode.com/hg/images/ping-pong-invariants/ping-minimized.dot.png)


Without node separation, we get the following graph:

![http://wiki.synoptic.googlecode.com/hg/images/ping-pong-invariants/ping-minimized-nosep.dot.png](http://wiki.synoptic.googlecode.com/hg/images/ping-pong-invariants/ping-minimized-nosep.dot.png)



## Issues Encountered ##

---


  * In the above, there are multiple redundant invariants. Ideally we could output the minimal equivalent set of invariants. In the above graph this would be exactly "payload == 0" and "payload == 1" for the two branches. Unfortunately we were not able to compile [Simplify](http://www.hpl.hp.com/downloads/crl/jtk/download-simplify.html), which is necessary for this task -- we could not get a working PM3 compiler (compiling the compiler failed), and CM3 did not work for the project.

## Thoughts on the approach ##

---


  1. Bisim (as well as GK-Tail) assume that a message is characterized by the message that follows. In the presence of concurrency this assumption does not hold.

  1. Besides concurrency, there is a closely related problem of **node interaction** (described below). We roughly mean that a node can decide to handle an event A initiated by another node before or after it itself initiates an event B. In a distributed system, interaction must be assumed (and is in some sense a requirement for the system to be a distributed system.)

  1. Interaction - as concurrency - invalidates the assumption stated in point (1).

  1. This bleak view may mean that we either must redefine out goals, or deal with concurrency.


### Interaction ###

---


In the following picture the leftmost connected component describes relay nodes that _do not have internal concurrency_. They are single threaded, but connected to an event loop. This means, that even without **intra-node** concurrency, there is **inter-node** concurrency that must be taken care of. Both types of concurrency introduce non-determinism. If we had full concurrency support, this would be a linear trace with concurrency markers and not the bottom-up tree structure we are seeing right now.

![http://wiki.synoptic.googlecode.com/hg/images/peterson-norand.png](http://wiki.synoptic.googlecode.com/hg/images/peterson-norand.png)

The picture exhibits another phenomenon which is related to the assumption stated above in point (1) -- the initial states are different for all traces, which is clearly wrong: Initially, the nodes behaviour is not fixed, instead it depends on the messages received, thus at least the `send` states should be merged.

The nodes are split because the behaviour (i.e. the trace of messages) that follows is different, and we assume this indicates different messages.

# Next Week : ProgressReport05112010 #

---
