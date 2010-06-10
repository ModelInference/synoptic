package model.input;

import model.Graph;
import model.MessageEvent;
import model.PartitionGraph;
import model.scalability.ScalableGraph;

public interface TraceParser {
	public Graph<MessageEvent> parseTraceFile(String fileName, int linesToRead, int options);

	public Graph<MessageEvent> parseTraceFileLarge(String fileName, int linesToRead,
			int maxStatesPerGraph, int options);
}
