package synoptic.model.input;

import synoptic.model.Graph;
import synoptic.model.MessageEvent;
import synoptic.model.PartitionGraph;
import synoptic.model.scalability.ScalableGraph;

public interface ITraceParser {
	public Graph<MessageEvent> parseTraceFile(String fileName, int linesToRead, int options);

	public Graph<MessageEvent> parseTraceFileLarge(String fileName, int linesToRead,
			int maxStatesPerGraph, int options);
}
