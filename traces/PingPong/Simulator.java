package trace.sim;

import trace.ProtoTrace.GenericMessage;
import trace.ProtoTrace.PingPongMessage;
import trace.ProtoTrace.Trace;
import trace.MessageTrace.FullTrace;
import trace.MessageTrace.WrappedMessage;
import trace.MessageTrace.TraceSet;
import trace.ProtoTrace.Abort;
import trace.ProtoTrace.Ack;
import trace.ProtoTrace.Prepare;
import trace.ProtoTrace.Commit;
import trace.ProtoTrace.TXCommit;
import trace.ProtoTrace.TXAbort;

import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.FieldDescriptor;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Random;
import java.util.Map.Entry;

class Simulator {
	static Random r = new Random();

	private static void printTypes() {
		System.err.println("\tTYPE=random #NODES #MESSAGES");
		System.err
				.println("\tTYPE=pingpong #NODES #PINGS ORDER{node | time | structure}");
		System.err.println("\tTYPE=2phase #TRANSNODES #TRANSACTIONS ERRORFLAG{true, false}");
	}

	// Ask the user for the name of the tracefile to generate, and
	// the type of trace to simulate, and create the trace.
	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			System.err.println("Usage:  Simulator TRACE_FILE TYPE <ARGS>");
			printTypes();
			System.exit(-1);
		}

		TraceSet.Builder traceSet = TraceSet.newBuilder();
		

		if (args[1].equalsIgnoreCase("random")) {
			//generateRandom(Integer.parseInt(args[2]),
			//		Integer.parseInt(args[3]), trace);
		} else if (args[1].equalsIgnoreCase("pingpong")) {
		//	generatePingPong(Integer.parseInt(args[2]), Integer
				//	.parseInt(args[3]), trace);

			// re-order the trace if requested
			if (args[4].equalsIgnoreCase("node")) {
			//	trace = numericOrder(trace, "time");
			//	trace = numericOrder(trace, "src");
			} else if (args[4].equalsIgnoreCase("time")) {
			//	trace = numericOrder(trace, "time");
			}
		} else if (args[1].equalsIgnoreCase("2phase")) { 
			generate2Phase(Integer.parseInt(args[2]), Integer.parseInt(args[3]), Boolean.valueOf(args[4]), traceSet);
		} else {
			System.err.println("Unknown simulation type.  Available types:");
			printTypes();
			System.exit(-1);
		}

		TraceSet genTraceSet = traceSet.build();
		
		// Write the trace out to disk.
		// If you need to read in a traceSet from file, simply use the following first:
		// TraceSet genTraceSet = TraceSet.parseFrom(new FileInputStream("inputFileName"));
		PrintWriter txtOut = new PrintWriter(new FileOutputStream(args[0]
				+ ".txt"));
		for (FullTrace t : genTraceSet.getFullTraceList()) {
			for(WrappedMessage m : t.getWrappedMessageList()){
				
				boolean first = true;
				for (Entry<FieldDescriptor, Object> field : m.getAllFields()
						.entrySet()) {
					if (first)
						first = false;
					else
						txtOut.print(", ");
					txtOut.print(field.getKey().getName() + " : "
							+ field.getValue());
				}
				txtOut.println();
				
				/*
				 * If accessing message contents is necessary (need more than meta info)
				 * then use code like the following:
				String msgType = m.getType();
				if(msgType.equals("Prepare")){
				
					Prepare p = Prepare.parseFrom(m.getTheMessage());
					
					p.getWhateverField()...
				
				}
				*/
			}
			txtOut.println();
			txtOut.println();
		}
		txtOut.close();

		// Write the trace out to disk.
		FileOutputStream output = new FileOutputStream(args[0]);
		genTraceSet.writeTo(output);
		output.close();
	}

	// This method creates a generic message given the input
	static GenericMessage createMessage(int src, int dst, long timestamp,
			byte[] payload) {

		GenericMessage.Builder message = GenericMessage.newBuilder();

		// stdout.print("Building message...");
		message.setSrc(src);
		message.setDst(dst);
		message.setTimestamp(timestamp);

		ByteString bytes = ByteString.copyFrom(payload);
		message.setPayload(bytes);

		return message.build();
	}

	// Generate a sequence of messages, with random lengths of random bytes
	// as the payload. The only restrictions are that src != dest, and each
	// message increases in time.
	private static void generateRandom(int nodes, int times, Trace.Builder trace) {

		for (int i = 0; i < times; i++) {
			byte[] randomBytes = new byte[r.nextInt(10)];
			r.nextBytes(randomBytes);
			int[] pair = randomPair(nodes);

			GenericMessage message = createMessage(pair[0], pair[1], i,
					randomBytes);
			trace.addGenericMessage(message);
		}
	}

	// generates a trace of ping pong messages, sending a ping every second.
	// Each ping produces a pong 80% of the time
	// and when a pong is produced half of the time it is followed by a status
	// message.
	// pings and pongs are separated by random intervals of 0-10 seconds. pongs
	// and status messages
	// are separated by random intervals of 0-2 seconds.
	// NOTE: trace will not be in time-order.
	private static void generatePingPong(int nodes, int pings,
			Trace.Builder trace) {

		for (int i = 0; i < pings; i++) {
			int[] pair = randomPair(nodes);
			int delay = r.nextInt(10)+1; // random delay between the ping and the
			// pong, up to 10 seconds
			PingPongMessage ping = createPingPong(pair[0], pair[1], i, "ping");

			trace.addPingPongMessage(ping);

			if (r.nextDouble() <= 0.8) {
				PingPongMessage pong = createPingPong(pair[1], pair[0], i
						+ delay, "pong");
				trace.addPingPongMessage(pong);
				if (r.nextDouble() <= 0.5) {
					int delay2 = r.nextInt(3)+1;
					PingPongMessage status = createPingPong(pair[0], pair[1], i
							+ delay + delay2, "status");
					trace.addPingPongMessage(status);
				}
			}
		}

	}
	
	
	//generate a trace of 2phase commit messages
	private static void generate2Phase(int nodes, int numTrans, boolean errorFlag,
			TraceSet.Builder traceSet) {
		
		//coordinator IP equal to max number of nodes
		int coord = nodes;
		int globalTime = 0;
		
		for(int j=0; j<numTrans; j++){
			FullTrace.Builder trace = FullTrace.newBuilder();
			
			//coordinator sends "prepare" message to all nodes
			for(int i=0; i<nodes; i++){
				WrappedMessage msg = createWrappedMessage(coord, i, globalTime, "Prepare", 
						Prepare.newBuilder().setNum(i).build().toByteString());
				trace.addWrappedMessage(msg);
				globalTime++;
			}
			
			
			//all nodes process the transaction up to the commit point,
			//and return thier vote of "commit" or "abort" to the cordinator
			//Currently, commit rate at 95%.
			boolean abort = false;
			for(int i=0; i<nodes; i++){
				WrappedMessage msg;
				
				
				if(r.nextDouble() <= 0.95){
					msg = createWrappedMessage(i, coord, globalTime, "Commit", 
							Commit.newBuilder().setNum(i).build().toByteString());
				} else {
					msg = createWrappedMessage(i, coord, globalTime, "Abort", 
							Abort.newBuilder().setNum(i).build().toByteString());
					abort = true;
				}
				
				trace.addWrappedMessage(msg);
				globalTime++;
			}
			
			//if all messages are commit, send tx_commit, otherwise tx_abort
			//If there is an error, invert the command sent 5% of the time
			String command;
			if(errorFlag){
				if(r.nextDouble() > 0.95){
					abort = !abort;
				}
				
			}
			
			if(!abort){
				for(int i=0; i<nodes; i++){
					WrappedMessage msg = createWrappedMessage(i, coord, globalTime, "TXCommit", 
							TXCommit.newBuilder().setNum(i).build().toByteString());
					
					trace.addWrappedMessage(msg);
					globalTime++;
				}
			} else {
				for(int i=0; i<nodes; i++){
					WrappedMessage msg = createWrappedMessage(i, coord, globalTime, "TXAbort", 
							TXAbort.newBuilder().setNum(i).build().toByteString());
		
					trace.addWrappedMessage(msg);
					globalTime++;
				}
			}
			
			//each node ack's when completed committing or aborting.
			for(int i=0; i<nodes; i++){
				WrappedMessage msg = createWrappedMessage(i, coord, globalTime, "Ack",
						Ack.newBuilder().setNum(i).build().toByteString());
				trace.addWrappedMessage(msg);
				globalTime++;
			}
			
			
			traceSet.addFullTrace(trace.build());
		}
	}

	// Construct a PingPong message
	private static PingPongMessage createPingPong(int src, int dst,
			long timestamp, String type) {
		PingPongMessage.Builder message = PingPongMessage.newBuilder();

		// stdout.print("Building message...");
		message.setSrc(src);
		message.setDst(dst);
		message.setTimestamp(timestamp);
		message.setType(type);

		return message.build();

	}
	
	// Construct a Wrapped message
	private static WrappedMessage createWrappedMessage(int src, int dst,
			long timestamp, String type, ByteString baseMessage) {
		WrappedMessage.Builder message = WrappedMessage.newBuilder();

		// stdout.print("Building message...");
		message.setSrc(src);
		message.setDst(dst);
		message.setTimestamp(timestamp);
		message.setType(type);
		message.setTheMessage(baseMessage);
		

		return message.build();

	}

	// Orders a ping-pong trace by a numeric field and returns the new trace.
	// Not the most efficient sort, but it works.
	private static Trace.Builder numericOrder(Trace.Builder trace, String field) {
		Trace.Builder trace2 = Trace.newBuilder();
		long lowest = Long.MAX_VALUE;
		boolean[] added = new boolean[trace.getPingPongMessageCount()];
		int lowestIndex = 0;
		while (trace2.getPingPongMessageCount() < trace
				.getPingPongMessageCount()) {
			for (int i = 0; i < trace.getPingPongMessageCount(); i++) {
				PingPongMessage current = trace.getPingPongMessage(i);

				long currentField;
				if (field.equals("time")) {
					currentField = current.getTimestamp();
				} else if (field.equals("src")) {
					currentField = current.getSrc();
				} else {
					currentField = current.getDst();
				}

				if (currentField < lowest && !added[i]) {
					lowestIndex = i;
					lowest = currentField;
				}
			}

			trace2.addPingPongMessage(trace.getPingPongMessage(lowestIndex));
			added[lowestIndex] = true;
			lowest = Long.MAX_VALUE;
		}

		return trace2;

	}

	// returns a 2 element array of a random distinct pair of nodes
	// (nodes numbered 0 .. nodes-1)
	private static int[] randomPair(int nodes) {
		int node1 = r.nextInt(nodes);
		int node2 = r.nextInt(nodes);
		while (node1 == node2) {
			node2 = r.nextInt(nodes);
		}
		int[] result = new int[2];
		result[0] = node1;
		result[1] = node2;

		return result;

	}
}