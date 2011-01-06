package synoptic.model.input;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import synoptic.model.Action;
import synoptic.model.Graph;
import synoptic.model.MessageEvent;
import synoptic.model.PartitionGraph;



public class NfsTraceParser extends DefaultScalableTraceParser {

	public final static int SPLIT_NONE = 0;
	public final static int SPLIT_BY_FILE = 1;
	
	public Graph<MessageEvent> parseTraceFile(String fileName, int linesToRead, int splitStrategy) {
		try {
			FileInputStream fstream = new FileInputStream(fileName);
			BufferedReader br = new BufferedReader(new InputStreamReader(
					fstream));
			ArrayList<String> traceLines = new ArrayList<String>();
			String strLine = null;
			int linesRead = 0;
			while ((strLine = br.readLine()) != null) {
				if (linesRead++ > linesToRead) break;
				traceLines.add(strLine);
			}
			br.close();

			// Build the graph
			switch(splitStrategy) {
			case SPLIT_NONE:
				return parseTrace(traceLines.toArray(new String[] {}));
			case SPLIT_BY_FILE:
				return parseTraceByFile(traceLines.toArray(new String[] {}));
			default:
				throw new IllegalArgumentException("");
			}
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			return null;
		}
	}

	public Graph<MessageEvent> parseTrace(String[] traceLines) {
		GraphBuilder gb = new GraphBuilder();
//		int ctr = 0;
		for (String line : traceLines) {
			Action a = parseTraceEntry(line);
			gb.append(a);
//			if (++ctr % splitInterval == 0)
//				gb.split();
		}
		return gb.getRawGraph();
	}
	
	public Graph<MessageEvent> parseTraceByFile(String[] traceLines) {
		GraphBuilder gb = new GraphBuilder();
		HashMap<String, ArrayList<Action>> sets = new HashMap<String, ArrayList<Action>>();
		for (String line : traceLines) {
			Action a = parseTraceEntry(line);
			String fileId = a.getStringArgument("fileid");
			if (fileId != null) {
				if (!sets.containsKey(fileId))
					sets.put(fileId, new ArrayList<Action>());
				sets.get(fileId).add(a);
			}
		}
		
		for (ArrayList<Action> fileRun : sets.values()) {
			for (Action a : fileRun) {
				gb.append(a);
			}
			gb.split();
		}
		return gb.getRawGraph();
	}

	private Action parseTraceEntry(String entry) {
		/*
		 * 
		 * COMMON FOR REQUEST/RESPONSE
		 * 
		 * 1. Time (seconds.microseconds) when the packet was seen.
		 * 
		 * Due to a misconfiguration of the clock on the EECS lair62 monitor,
		 * sometimes time will suddenly jump backwards for five minutes for a
		 * few moments. This seems to happen about once a week. Unfortunately I
		 * didn't notice this in time to do anything about it.
		 * 
		 * 2. Source address (iaddr.port)
		 * 
		 * 3. Destination address (iaddr.port)
		 * 
		 * 4. Transport protocol (U=udp, T=tcp). From the original traces
		 * (2001-2003) the lair packets are UDP and all the FAS packets are TCP.
		 * 
		 * 5. NFS RPC protocol version and direction. (C3 = nfs v3 call, R3 =
		 * nfs v3 response, C2 = nfs v2 call, R2 = nfs v2 call).
		 * 
		 * 6. RPC XID field.
		 * 
		 * 7. RPC function (numeric value). This is redundant with field 8.
		 * 
		 * 8. RPC function (canonical name). For example, getattr, lookup, read,
		 * write, ...
		 */
		StringTokenizer itr = new StringTokenizer(entry);
		try {
			
			
			String time = itr.nextToken(); // 1
			String src = itr.nextToken(); // 2
			String dest = itr.nextToken(); // 3
			String protocol = itr.nextToken(); // 4
			String version = itr.nextToken(); // 5
			String xid = itr.nextToken(); // 6
			String fun_num = itr.nextToken(); // 7
			String fun = itr.nextToken(); // 8
			String fun_2 = itr.nextToken(); // 9
			// TODO: use more of these fields as arguments.
			
			Action a = new Action(fun + " "  + fun_2);
			
			while(itr.hasMoreTokens()) {
				String tok = itr.nextToken();
				if (tok.equals("fileid")) {
					a.setStringArgument("fileid", itr.nextToken());
				}
			}
			
			return a;
		} catch (NoSuchElementException exn) {
			System.err.println("Could not parse NFS log entry");
			return null;
		}
	}

}
