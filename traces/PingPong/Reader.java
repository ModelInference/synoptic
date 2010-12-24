package trace;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import ProtoTrace.GenericMessage;
import ProtoTrace.PingPongMessage;
import ProtoTrace.Trace;

public class Reader {
	
	// Asks the user for the input trace file.  Automatically
	// determines the type of the messages in the trace
	// and prints them appropriately.
	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.err.println("Usage:  Reader TRACE_FILE");
			System.exit(-1);
		}

		// read in the trace
		Trace trace = Trace.parseFrom(new FileInputStream(args[0]));

		Print(trace);
	}
	
	public static Trace loadTrace(String name) throws FileNotFoundException, IOException {
		return Trace.parseFrom(new FileInputStream(name));
	}
	
	// Prints out the trace in the correct format for the message type.
	static void Print(Trace trace) {
		
		//Check to see if this trace is of generic messages.  If so, print them appropriately.
		if(trace.getGenericMessageCount() > 0){
			System.out.println("Printing message trace (Generic Messages): ");
		    for(GenericMessage m : trace.getGenericMessageList()){
		    	System.out.println("Src: " + m.getSrc() + " Dst: " + m.getDst() + 
		    			" Time: " + m.getTimestamp() + " PL: " + m.getPayload().toString());	
		    }
		} else if(trace.getPingPongMessageCount() > 0){
			System.out.println("Printing message trace: ");
			for(PingPongMessage m : trace.getPingPongMessageList()){
		    	System.out.println("Src: " + m.getSrc() + " Dst: " + m.getDst() + 
		    			" Time: " + m.getTimestamp() + " Type: " + m.getType());	
		    }
		}
	}
}