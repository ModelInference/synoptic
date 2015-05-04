

# Synoptic overview #

---

Synoptic is a tool that extracts visually informative models from raw logs. Here's the scenario: You, the developer, have built some kind of large system, and now you are attempting to understand why it's misbehaving or behaving in some unexpected way. A typical first step in dealing with this problem is to add some logging code to find out what's executing, what events are firing, etc. If you re-ran the system and generated a small log, then great, you can skim the log and quickly zero in on the potential problem. But, if your log is large, maybe a million lines long, then good luck! Chances are, the log will be as difficult to understand as the source code to your system.

Synoptic converts an input log into a graph-like model that is easier to inspect than the original (messy, and very large) log. Synoptic has just two inputs: the log, and a set of regular expressions to match those lines in the log that you, as the developer, care about.

You can try Synoptic in your browser!

The core of Synoptic is in Java, while the web front-end is in GWT and Raphaël. Here are a few more resources:

  * This website!
  * An [academic paper](http://www.cs.ubc.ca/~bestchai/papers/esecfse2011-final.pdf) that describes Synoptic internals (warning: not an easy read).
  * A silly 30sec Synoptic [video](http://www.youtube.com/watch?v=DSp2DSr5imw)
  * Contact us: Ivan (bestchai@cs.ubc.ca) and Yuriy (brun@cs.umass.edu).

<br />
## Visualization improvements ##

---

The current [Synoptic prototype](http://synoptic.cs.washington.edu) lacks a number of essential features that are necessary to make this tool usable. Broadly, we would like to improve the look and feel of the prototype. Here are three concrete features (in order of increasing difficulty) that we would like to see implemented. We have many more (just ask us), and you can always suggest your own!

  * **Model zooming.** If your model is too large, then it looks awful. We need robust zooming support.
  * **Sensible model layout.** You'll notice that edges in the current models often intersect, the nodes may be on top of one another, etc. We would love for someone to implement a better layout algorithm!
  * **Hide edges in the model that are below or above a certain probability/frequency.** The model has weights on the edges to show how frequently a source event is followed by a target event (these can be probabilities or frequencies). Often, a developer is interested in the low probability transitions (e.g., these are rare events, so they may be good to look at in detail!). Implementing this selective filtering of nodes/edges based on edge weights would be helpful!

### Technical Challenges ###

  * You'll need to understand the existing code (mostly Java/GWT and Raphaël).
  * You'll need to learn about and hack on some of the Raphaël internals.

### Ideal Skills ###

  * Experience with Java
  * Experience with GWT
  * Experience with Javascript
  * Experience with Raphaël
  * Experience with building interactive visualizations
  * Graph theory/graphics background would be nice to have
  * Some sense of design and usability



<br />
## Support for mining richer log properties ##

---

Internally Synoptic uses _log properties_ to derive a model that is accurate. These properties are temporal, for example every trace in the log might have the property " _connect_ is always followed by _disconnect_". These properties are automatically _mined_ from the log.

The existing Synoptic property miner is all-or-nothing: either the property holds true across all traces in the log and is mined, or some trace violates the property and the property is not mined. We would like to improve the property miner in two ways:

  * **Allow the user to specify a confidence threshold and mine properties above this threshold.** One implementation of confidence is the fraction of matching events in the log that "support" the property. For example, if the above property is violated by 5 instances of a _connect_ event in the log, and is true for 10 other instances of a _connect_ event, then the support for the property is 10/15, or 2/3. If the user were to specify a confidence threshold of 1/2, this property would be mined and used in model building. The current miner can be seen as a special case of this more general approach --- it uses a fixed confidence value of 1.0.

  * **More advanced property types.** Synoptic supports just three property types: "always followed by", "never followed by", and "always precedes". These property types are simple, but powerful. However, for some systems they are clearly insufficient. We would like to add more property types to Synoptic. For example, one important property type that we are missing is "never precedes".

### Technical Challenges ###

  * You'll need to understand the existing code (Java)
  * You'll need to come up with new and adapt existing algorithms in the code to extend the property miner.

### Ideal Skills ###

  * Experience with Java
  * Exposure to algorithms and algorithmic thinking


<br />
## Tracking distributed web-app execution with vector timestamps ##

---

A tool related to Synoptic is CSight. CSight aim is to extract a model that describes a log from a distributed system (while Synoptic works on sequential systems/logs). One key challenge with using CSight is that it requires a _partially ordered log_ of events. In short, this means that CSight needs a log in which every log line has a special _vector timestamp_, which can be used to reconstruct the distributed execution (e.g., the flow of events between the browser and web server). Generating such vector timestamped logs is non-trivial --- developers have to add this support to their systems on their own.

The aim of this project is to build a Javascript library, and a few server-side libraries, that automatically add vector timestamps to logs of web applications. This will help developers use CSight, and can also lead to a bunch of other exciting follow-on projects. For example, the vector timestamped logs can be collected and visualized in real-time, to reveal how the code is processing Ajax events, and more generally, the flow of communication between the client and the web services that it interacts with.

### Technical Challenges ###

  * Learning about vector timestamps
  * Building a Javascript library that can wrap existing code an interpose on communication and certain events
  * Building a server-side library that can similarly wrap existing server-side code

### Ideal Skills ###

  * Experience with Javascript
  * Experience with at least one web framework (e.g., Django), and the underlying language (e.g., Python/C/C++)
  * Experience with some web visualization frameworks like D3.js and Raphaël


<br />
# CSight overview #

---


The goal of the CSight project is to infer a model of a distributed system from a log of the system’s behavior. That is, observing the set of all communication events and local process events CSight will infer a set of finite state machines (one per process) that communicate over FIFO channels using message passing. This kind of finite state machine is called a [communicating finite state machine](http://en.wikipedia.org/wiki/Communicating_finite-state_machine). CSight infers this model by mining temporal properties from the log of events recorded by the system and then inferring a model that preserves these properties.

<br />
## Extending a distributed model inference tool to use a different model checker ##

---


Currently, CSight relies on the [McScM](https://altarica.labri.fr/forge/projects/mcscm/wiki/) model checker to check the mined properties. However, this model checker can be very slow since it models unbounded FIFO queues (which is accurate, but expensive!).

In this project your task is to integrate CSight with [SPIN](http://spinroot.com/spin/whatispin.html), a model checker that models bounded FIFO queues. That is, SPIN models the system as a collection of per-process FSMs where two FSMs communicate with each other over FIFO queues that have a user-defined message capacity bound. Adding SPIN support to CSight will make the CSight project more broadly applicable and enable CSight to run on more complex and more interesting system logs.

### Technical Challenges ###

  * Learning Promela, a domain-specific programming language used by the SPIN model checker
  * Learning formal methods techniques, like model checking

### Ideal Skills ###

  * Knowledge of Java
  * Networking knowledge a strong plus
  * Exposure to algorithms and formal methods


<br />
## Validating a distributed model inference tool on existing systems ##

---


[Mace](http://www.macesystems.org/mace/) is a language for building distributed systems that has been used to build distributed hash tables, distributed routing protocols, and other systems. One of the most attractive features of Mace is that it relieves the developer from writing low-level code to orchestrate communication, or interspersing failure-checking code throughout the implementation. More directly, Mace is a set of extensions to C++ that allow a developer to specify their distributed system as a set of finite state machines (FSMs) that communicate using message passing. Mace supports scheduled tasks, aspects, and a separation between control and data states.

In this project your task is to (1) learn about Mace and get it installed on a host system of your choice, (2) generate a set of instrumentation wrappers for Mace applications that will output the sequence of internal transitions events (including message sends and receives) for each of the Mace FSMs, (3) apply the CSight tool, which is used to infer CFSMs from a trace of events in a distributed system, to traces generated by different Mace systems, and (4) compare the resulting CSight-generated CFSMs to the underlying Mace programs that describe the CFSM implicitly with C++ code.

The point of this project is to evaluate the benefits and shortcomings of CSight by using the existing Mace applications as test-subjects that have ground-truth models.

### Technical Challenges ###

  * Learning a Mace, a domain-specific programming language for developing distributed systems
  * Installing/using Mace and reading existing Mace programs
  * Using CSight to generate CFSM models from logs of Mace programs

### Ideal Skills ###

  * Knowledge of C++ and Java
  * Networking knowledge a strong plus
  * Exposure to algorithms and formal methods

<br />
## Your idea here! ##

---

The above three Synoptic project ideas are just the tip of iceberg. If **you** have an idea that you would like to implement in Synoptic, then contact us. A good place to start brainstorming ideas is to look at some of the outstanding [Synoptic project issues](http://code.google.com/p/synoptic/issues/list).