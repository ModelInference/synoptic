Introduction
============

Synoptic is a tool that summarizes log files. More exactly, Synoptic
takes a set of log files, and some rules that tell it how to interpret
lines in those logs, and outputs a model that concisely and accurately
captures the important properties of the process that generated the
lines in the logs. Synoptic summaries are directed graphs with some
number of initial nodes, and some number of terminal nodes. Synoptic
is especially intended for use with large systems logs which include
multiple (e.g., hundreds) of logical executions. For example, a server
that processes a request might handle a thousand requests, and log
information about how it handled each one. Each of these handled
requests can then be considered as an independent execution by
Synoptic.


Dependencies
============

To output graphs Synoptic depends on a tool called dot, which you can
install as part of the Graphviz suite: http://www.graphviz.org/.

Synoptic is known to work on OSX, Linux, and Windows platforms with
JRE 6 or higher.


Testing your installation
=========================

The Synoptic release you unpacked (which includes this file) is
composed of a set of jar files. Synoptic is written in Java and to
start it you must invoke some commands from the command line. As a
test of your installation make sure that you can do the following:

Show command line arguments help by running:
> java -jar synoptic.jar -h

Successfully run the Synoptic unit tests with:
> java -jar synoptic.jar --runTests


References
============

For help with the installation see:
http://code.google.com/p/synoptic/wiki/DocsInstallation

For usage information see:
http://code.google.com/p/synoptic/wiki/DocsTutorial
http://code.google.com/p/synoptic/wiki/DocsUsage

You may also want to read the command line arguments help, which can
by displayed by running:
> java -jar synoptic.jar -h


Contact
=======

If you'd like to relay a bug report, influence feature selection in
future releases, or simply comment on the project, write emails to:
ivan@cs.washington.edu
