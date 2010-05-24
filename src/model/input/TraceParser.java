package model.input;

import model.PartitionGraph;
import model.scalability.ScalableGraph;

public interface TraceParser {
	public PartitionGraph parseTraceFile(String fileName, int linesToRead, int options);

	public ScalableGraph parseTraceFileLarge(String fileName, int linesToRead,
			int maxStatesPerGraph, int options);
}
