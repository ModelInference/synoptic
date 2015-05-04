# Test Generation #
## Motivation ##

One of the possible applications of Synoptic and Dynoptic models is test generation. A Synoptic/Dynoptic model describes an inferred behavior of a System Under Test. We can derive an abstract test suite from this model. Each abstract test case is a valid trace through the model. Concrete test cases can be derived from abstract test cases.

Synoptic/Dynoptic models are generative. They predict executions that might be feasible, but were not observed in the logs. These predicted executions are particularly useful for test generation.

Concrete tests often involves data values, however, abstract tests derived from regular Synoptic/Dynoptic models do not provide information about data values. In principle, models with constraints on data values should be better for test generation than models without value constraints, because it is easier to derive valid concrete tests.

Note that predicted test cases might be illegal, regardless of whether data constraints are present. That is, there is no concrete test that maps to the abstract test. This is a failure of the prediction process i.e., of Synoptic’s model inference.

## Hypothesis ##

Concrete tests derived from abstract models with data invariants are more likely to be valid and useful than those derived from models without data invariants.

## Idea ##

We use both regular Synoptic models and stateful Synoptic models to generate abstract tests. A stateful Synoptic model is a Synoptic model that has Daikon invariants annotated on its states.
  * Derive abstract tests from a model:
> We derive an abstract test suite from a model by taking paths through the model. Some of the techniques are:
    1. Allow the tests to move randomly through the state model, taking any available action out of a state.
    1. “Chinese Postman tour”, touches every action in the state model as efficiently as possible.
> We have 2 kinds of abstract test:
    1. Synoptic abstract test - a sequence of events
    1. Stateful Synoptic abstract test - a sequence of events along with data value constraints.
  * Derive concrete tests from an abstract test:
    * From Synoptic abstract test:
> > For each event in the test sequence, we write script to do some action according to the event type. If the action involves producing data values, choose the values randomly (or use predetermined values).
    * From stateful Synoptic abstract test:
> > Similar to the above. But if the action involves producing data values, choose the values that satisfy the constraint immediately following the event.
  * Derive test oracles from the model:

> Simple test oracle - determines whether a concrete test results in an unexpected exception or crash.
> // TODO: how do we get this test oracle?

## Evaluation ##

For a particular system, generate 2 concrete test suites: one is from Synoptic model and the other is from stateful Synoptic model. Every test in one suite has a corresponding test in the other suite, which is identical to it except for maybe data values.

Count the number of valid tests in both suites. A valid test is an execution that is legal in the system.
// TODO: elaborate more

## References ##

  * Robinson, Harry; Finite State Model-Based Testing on a Shoestring, STAR West 1999
  * One evaluation of model-based testing and its automation
  * Finding all paths in a Directed Acyclic Graph (DAG)

# Stateful Synoptic #
## Motivation ##

Synoptic and Dynoptic assume that their input logs only contain sequences of events. Therefore, the models they produce do not have information about states. However, logs can also contain state information, that is, some properties of a system at a given time. Users might want to know, from an inferred model, that some properties hold true before or after an event occurs. For example, after an event “initialize thread” occurs, the thread’s status is “ready”.

## Idea ##

The process of inferring a model will be the same, but we will allow an input log to contain information about states. Note that execution traces in a log need not be alternating between states and events. Once we have a final model (which has only events at this point), we add the states to the model by taking each execution trace from the log and matching states to edges (or nodes, depending on the model). Each edge (or node) that represents a system state could have different values assigned to it. We then take these values and figure the likely invariants of them, and assign those invariants to that edge or node.


## Definitions ##

State = a set of identifier-value pairs, defined at a point in time (in an execution trace)

## Assumptions ##

  1. A log line that represents a state contains a set of key-value pairs. For example, “x = 1, connected = true, msg = [‘a’,’b’,’c’], obj.size = 5”.
  1. A trace cannot contain 2 consecutive states without an event (or more) in between.


## Implementation ##

### Overview ###

This project is based on Synoptic.The log parser needs to change so that it can parse state. To label edges of the inferred model with states, take each execution trace from the log and match it to a path in the model by following the sequence of events in the trace. Each edge will have a set of states. Next, format these data and pass them to Daikon to compute likely invariants, then assign those likely invariants to that edge.

### Parsing states ###

The special field that determines states is STATE. The user supplies a regex (?`<STATE`>regex) for capturing states. Since a state can contain an arbitrary number of identifier-value pairs, it is not possible to use groups to capture all pairs. Instead, we assume the format of a state to be id=value, id=value, …, id=value.

We keep states in EventNode structure as preEventState and postEventState. (TODO: this is a hacky solution. The methods parseLine() and parseTrace() should be refactored so that State is separated from EventNode.)