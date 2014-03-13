Some notes on how the generate TCP traces for use with CSight. All of
these are for a Linux/OSX box that has ipfw.

The basic set-up is to use local firewall rules to redirect/shape
local traffic that is generated manually by a netcat (nc)
session. This traffic captured/logged, and can then be used as input
to CSight (with manually added vector-timestamps).

NOTE: the ports 2020 and 8080 and the localhost 127.0.0.1 IP are
hardcoded in the scripts, so you should re-use these or update the
scripts to take arguments for these values.

1. Install the firewall rules that allow you to redirect/shape traffic:
   $ sudo ./start.sh


2. Start the script to capture traffic into a file:
   $ ./capture.sh traffic.log


3. Start the netcat server on 127.0.0.1:2020
   $ nc -l -4 -n -v -b lo0 2020


4. Start the netcat client and connect on 8080 to lo0:2020
   $ nc -4 -n -v -p 8080 127.0.0.1 2020


5. Interact with the netcat to generate some traffic.


6. Stop both of the netcat sessions -- the client and the server.


7. Stop the capturing script.


8. Read the captured traffic and output it to the screen:
   $ ./read.sh traffic.log
   
   (Redirect the output to a file, to process it with Synoptic.)


9. Un-install all of the firewall rules:
   $ ./stop.sh


10. To run CSight on the generated traces, you have to add vector
    timestamps (array of 2 integers) to each of the logged lines. Look
    at po_tcp_log.txt for an example of this.
