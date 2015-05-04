# Overview #

---


Synoptic is a tool to infer a model from a textual log. The [Synoptic web-app](http://synoptic.cs.washington.edu/) provides a web GUI to the command line Synoptic tool.

For help related to the **command line** version of Synoptic, see [tutorial](DocsSynopticCmdLineTutorial.md), [usage](DocsSynopticCmdLineUsage.md), [help screen](DocsSynopticCmdLineHelpScreen.md).


# What is Synoptic? #

---


In brief, Synoptic is a tool that summarizes log files. More exactly, Synoptic takes a set of log files, and some rules that tell it how to interpret lines in those log files, and outputs a summary that concisely and accurately captures the important properties of lines in the log files. Synoptic summaries are directed graphs. Sometimes we refer to Synoptic generated summaries as models, because they describe the process that produced the logs. The two terms are equivalent.

# Workflow #

---


  1. Upload or type in a log and the regular expressions to parse the log
  1. Process the log with Synoptic
  1. Explore the generated model:
    1. Drag model nodes/edges around to understand the model
    1. Find out which log lines are mapped to which nodes in the model
    1. Find out which model paths (or sub-paths) map to which traces in the input log
  1. Explore the temporal invariants mined by Synoptic from the input log
    1. Consider invariants between a specific event type of interest
    1. List/explore invariants of a particular type (e.g., never followed by invariants)
    1. Disable any invariants that you do not wish to be used by Synoptic

The web-app interface is organized into tabs. Below we overview each of the tabs and include a few screenshots.


# Inputs Tab #

---


![http://wiki.synoptic.googlecode.com/hg/images/synoptic-web-app/inputs_tab.jpg](http://wiki.synoptic.googlecode.com/hg/images/synoptic-web-app/inputs_tab.jpg)

**Load Example Logs:** Click one of the links below to load an example log into the input fields.
  * TicketReservation: This log is taken from a hypothetical distributed trace of an online ticket-purchase system.
  * abstract: This abstract trace illustrates that synoptic selectively prefers models that delay branching decision points, but not to the very end.
  * ShoppingCart: This log is taken from a hypothetical apache access log for a shopping cart web application.
  * TwoPhaseCommit: This log is taken from a message trace for simulations of the Two Phase Commit distributed protocol.

**Log:** Click on "Text" to copy and paste log or click on "File" to load a log file.

**Regular expressions:** Input regular expression(s) to match each event instance. Click on "+" to input
additional regular expressions. If more than two regular expression fields exists, then "-" can be clicked to delete the neighboring input field.

**Partition expression:** Input a regular expression to identify which set the particular event belongs to.

**Separator expression:** Input a regular expression to indicate the boundary between distinct partitions. If your log contains partitions sequentially, then specify this instead of specifying a partition expression.


# Invariants Tab #

---


_(Only accessible after processing a log input.)_

![http://wiki.synoptic.googlecode.com/hg/images/synoptic-web-app/invariants_tab.jpg](http://wiki.synoptic.googlecode.com/hg/images/synoptic-web-app/invariants_tab.jpg)

**Invariant Table:**  Hover over an invariant to see it highlighted in the invariant graphic. Click on an invariant to activate/de-activate it. If the log is totally ordered, click on the invariant header to activate/de-activate all these invariants. Activating an invariant will make the invariant visible in the invariant graphic. Deactivating an invariant will gray out the invariant in the invariant table and make the invariant invisible in the invariant graphic. One might use this activating/deactivating feature to...

  * AlwaysPrecedes (←): If event type a always precedes event type b (a ← b), then whenever the event type b appears, the event type a always appears before b in the same trace.

  * AlwaysFollowedBy (→): If event type a is always followed by b (a → b), then whenever the event type a appears, the event type b always appears later in the same trace.

  * NeverFollowedBy (↛): If event type a is never followed by b (a ↛ b), then whenever the event type a appears, the event type b never appears later in the same trace.

  * AlwaysConcurrentWith (‖): For partially ordered input logs a is always concurrent with b if there is never a happens-before relationships between a and b.

  * NeverConcurrentWith (∦): For partially ordered input logs a is never concurrent with b if there was always a happens-before relationships between a and b (but the direction of the happens-before could change).

**Invariant Graphic:** The invariant graphic is a graphic representation of the invariant table.
  * The left half of the invariant graphic displays the AlwaysPrecedes (←) invariants. The right half of the invariant graphic displays both AlwaysFollowedBy (→) invariants and NeverFollowedBy (↛) invariants.
  * Hover over an arrow to see it highlighted and that particular invariant also highlighted in the invariant table.
  * Hover over an event type label to see invariants associated with that event type highlighted in both the invariant graphic and invariant table.

# Model Tab #

---


_(Only accessible after processing a log input.)_

![http://wiki.synoptic.googlecode.com/hg/images/synoptic-web-app/model_tab.jpg](http://wiki.synoptic.googlecode.com/hg/images/synoptic-web-app/model_tab.jpg)

**Export DOT:** Click to export a DOT file of the model.

**Export PNG:** Click to export a PNG file of the model.

**Model:** (a model is available only for totally ordered logs.)
  * Click on a node to see all of the log lines containing that event type. These log lines are listed to the left.
  * The edge labels indicate transition probability. For example, let event type A connect to event type B by an edge. Whenever event type A appears, event type A transitions to event type B a percentage of the time given by the edge probability.
  * There can be multiple nodes of the same event type (Synoptic may differentiate the nodes based on observed behavior).

You can also query the model about which paths map to concrete traces in the input log. To do so, SHIFT-CLICK the nodes along a path and click on **View Paths**. The panel on the left will show the unique traces from the input log that match up with, or map to, the abstract model nodes path you selected. You can click a radio-button for one of these to see the path highlighted in blue in the model. Here is what this looks like:

![http://wiki.synoptic.googlecode.com/hg/images/synoptic-web-app/model_paths.jpg](http://wiki.synoptic.googlecode.com/hg/images/synoptic-web-app/model_paths.jpg)