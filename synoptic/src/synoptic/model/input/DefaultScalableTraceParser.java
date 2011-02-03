package synoptic.model.input;

import synoptic.model.Graph;
import synoptic.model.MessageEvent;

public abstract class DefaultScalableTraceParser implements ITraceParser {

    @Override
    public Graph<MessageEvent> parseTraceFileLarge(String fileName,
            int linesToRead, int maxStatesPerGraph, int options) {
        Graph<MessageEvent> sg = new Graph<MessageEvent>();
        int linesRead = 0;
        while (linesRead < linesToRead) {
            sg.merge(parseTraceFile(fileName, maxStatesPerGraph, options));
            linesRead += maxStatesPerGraph;
        }
        return sg;
    }
}
