NOTE:
=====

The file trace.txt is used by a test in POLogInvariantMiningTests.java
file. If this file is moved/renamed or if its format changes, the test
file dependency must be updated.

====


This log is taken from a hypothetical distributed trace of an online
ticket-purchase system.

There are five system traces, each comprised of log lines and
corresponding vector clock timestamps for a web application that sells
airplane tickets. In the traces, two clients access a single server.

This log has interesting invariants. For example:

- available_server <- buy_client-0
- available_server <- buy_client-1
- sold_out_server -/-> buy_client-0
- sold_out_server -/-> buy_client-1
..

Here are some example of examples of false-positive invariants from
this log:

- buy_client-0 -> sold_out_server
- buy_client-1 -> sold_out_server
- buy_client-0 <- sold-out_server
