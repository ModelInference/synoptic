RADIUS Authentication:

<p>The RADIUS protocol authenticates clients on a network. To
authenticate, a client first sends a username and password in an
<b>Access-Request</b> message. The server can respond in three
different ways. It can grant the user access with an
<b>Access-Accept</b> message, or it can reject access with an
<b>Access-Reject</b> message. In either case, authentication
ends. Alternatively, the server can request additional credentials
with an <b>Access-Challenge</b> message, after which the above steps
repeat: the client sends another <b>Access-Request</b> message, and
the server again responds.</p>"


Caching Web Browser:

<p>Web browsers often cache previously-viewed pages and resources to
disk so that subsequent requests to the same pages are faster. A
browser might record when it caches a page to disk
(<b>cache-page</b>), retrieves a page from disk
(<b>retrieve-page</b>), caches an image to disk (<b>cache-image</b>),
and retrieves an image from disk (<b>retrieve-image</b>). The
log/model below represents captured traces for such caching and
retrieving events along with the total number of KB read from and
written to disk.</p>"


Connection Tester:

<p>Connection Tester is a tool that diagnoses network issues. It tests
a client's bandwidth, executes a series of queries, and then
classifies the network path as ""normal"" or ""abnormal"" based on the
results.</p>"
