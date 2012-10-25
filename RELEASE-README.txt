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


Running Synoptic
=========================

The Synoptic release you unpacked (which includes this file) is
composed of a set of jar files. Synoptic is written in Java and to
start it you must invoke some commands from the command line.

The release includes two scripts: synoptic-jar.sh (for *nix platforms)
and synoptic-jar.bat (for Windows). Run the script for your platform
with -h or -H options to see more usage help. For example:

> ./synoptic-jar.sh -H

If the scripts do not work for you, then invoke java directly:

> java -jar ./lib/synoptic.jar -H


Testing your installation
=========================

As a test of your installation make sure that you can successfully run
the Synoptic unit tests with:

> ./synoptic-jar.sh --runTests


Documentation and help
=======================

For help with the installation see:
http://code.google.com/p/synoptic/wiki/DocsInstallation

For usage information see:
http://code.google.com/p/synoptic/wiki/DocsCmdLineTutorial
http://code.google.com/p/synoptic/wiki/DocsCmdLineUsage


Contact
=======

If you'd like to relay a bug report, influence feature selection in
future releases, or simply comment on the project, write emails to:
ivan@cs.washington.edu
