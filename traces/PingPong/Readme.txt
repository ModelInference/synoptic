README for Simulator and Reader

Simulator generates random traces for various systems.  Currently the Simulator supports a TYPE of:
	random - nodes randomly send random payloads to each other every second.
	pingpong - Every second a random node sends out a ping to another node.
		The other node will respond with a pong with 80% probability.
		If it does respond, it will respond in a random (integer) interval of 0-10 seconds.
		If the other node responds with a pong, there is a 50% probability it will also send a status message.
		This status message will be sent in a random (integer) interval of 0-2 seconds after the pong.
		
To run the Simulator:
	Simulator TRACE_FILE TYPE <ARGS>
		where <ARGS> are
		TYPE random: #NODES #MESSAGES_TO_GENERATE
		TYPE pingpong: #NODES #PINGS_TO_GENERATE ORDER{node | time | structure}
		
Example of generating a pingpong trace amongst 10 nodes with 100 pings ordered by global time:
	Simulator ping_pong.trace pingpong 10 100 time

Using node ordering will group messages by source node (and ordered by time for each source node).
Using structure ordering will keep messages in logical ping/pong/status ordering (a ping is
either followed by a pong or a subsequent ping.  A pong is either followed by a status or a subsequent ping.)

To use the Reader:

	Reader TRACE_FILE

The reader will automatically determine the type of trace and print out the trace sequence to the console.