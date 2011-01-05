package model.input;

import model.Graph;
import model.MessageEvent;
import model.scalability.ScalableGraph;

public abstract class DefaultScalableTraceParser implements TraceParser {

	@Override
	public Graph<MessageEvent> parseTraceFileLarge(String fileName, int linesToRead,
			int maxStatesPerGraph, int options) {
		Graph<MessageEvent> sg = new Graph<MessageEvent>();
		int linesRead = 0;
		while (linesRead < linesToRead) {
			sg.merge(parseTraceFile(fileName, maxStatesPerGraph, options));
			linesRead += maxStatesPerGraph;
		}
		return sg;
	}
}
