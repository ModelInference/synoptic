Apache access logs:

Synoptic should be able to parse apache logs to determine common threads of user behavior on websites. Attempts thus far 
have mined GET requests as events and partioned by unique users. This approach generates huge and confusing graphs on large logs. 
(short 142 example in args file parsed 774 events with running time > 1 hour)

Some ideas:
   -Experiment with mining the referrer rather than GET request
   -Make final graphs easier to read by emphasizing particularly well-traveled paths by making them larger
   -Allow users to remove particular invariants between runs
   -Synoptic will allow clients to identify groups of similar users by examining which traces are merged

The format of apache logs is:
   1. IP address of remote host	
   2. Remote logname of user	
   3. Authuser: user id determined by HTTP authentication	
   4. Date and time of request	
   5. Request	
   6. Status code	
   7. Bytes transferred	
   8. Referrer – the user’s previous URL	
   9. User agent – software the user claims to be using

A typical log in this form:
128-208-98-171 - - [06/Jan/2011:08:28:21 -0800] "GET /education/courses/cse142/11wi/images/message_board_icon.png” 304 - "http://www.cs.washington.edu/education/courses/cse142/11wi/homework.shtml" "Mozilla/5.0 Gecko/20100722 Firefox/3.6.8"



