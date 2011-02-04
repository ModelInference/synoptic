package synoptic.model.input;

import synoptic.model.Graph;
import synoptic.model.LogEvent;

public interface ITraceParser {
    public Graph<LogEvent> parseTraceFile(String fileName, int linesToRead,
            int options);

    public Graph<LogEvent> parseTraceFileLarge(String fileName,
            int linesToRead, int maxStatesPerGraph, int options);
}
