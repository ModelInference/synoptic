package synoptic.model.input;

import synoptic.model.Graph;
import synoptic.model.LogEvent;

public abstract class DefaultScalableTraceParser implements ITraceParser {

    @Override
    public Graph<LogEvent> parseTraceFileLarge(String fileName,
            int linesToRead, int maxStatesPerGraph, int options) {
        Graph<LogEvent> sg = new Graph<LogEvent>();
        int linesRead = 0;
        while (linesRead < linesToRead) {
            sg.merge(parseTraceFile(fileName, maxStatesPerGraph, options));
            linesRead += maxStatesPerGraph;
        }
        return sg;
    }
}
