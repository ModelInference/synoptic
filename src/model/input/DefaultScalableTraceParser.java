package model.input;

import model.scalability.ScalableGraph;

public abstract class DefaultScalableTraceParser implements TraceParser {

	@Override
	public ScalableGraph parseTraceFileLarge(String fileName, int linesToRead,
			int maxStatesPerGraph, int options) {
		ScalableGraph sg = new ScalableGraph();
		int linesRead = 0;
		while (linesRead < linesToRead) {
			sg.addGraph(parseTraceFile(fileName, maxStatesPerGraph, options));
			linesRead += maxStatesPerGraph;
		}
		return sg;
	}
}
