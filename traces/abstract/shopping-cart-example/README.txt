This short log snippet (in trace.txt) is taken from a hypothetical
apache access log for a shopping cart web application.  The initial
model (www-example.initial.png) shows that the log contains three
traces.

The resulting final model illustrates how synoptic makes it easy to
spot a bug, which would have been difficult to find by inspecting the
log manually: after entering an invalid-coupon the user is able to
reduce the shopping cart's price before checking out
(www-example.png).

