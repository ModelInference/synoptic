package synoptic.main.parser.extperfume;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.logging.Logger;

import synoptic.main.parser.ParseException;
import synoptic.model.EventNode;
import synoptic.util.InternalSynopticException;

public class MultiResourceParser {

    private static Logger logger = Logger.getLogger("Parser Logger");

    // TODO: log parser that takes in one file as a trace with first line
    // specifying "event,<resource name>..." and the rest of lines as the events
    // with resource values following format of first line. Each resource for
    // one event is to be parsed as one invariant. (Eventually redesign so the
    // first line can be specified as options?)

    public MultiResourceParser() {

    }

    /**
     * Parses a trace file into a list of log events.
     * 
     * @param file
     *            File to read and then parse.
     * @param linesToRead
     *            Bound on the number of lines to read. Negatives indicate
     *            unbounded.
     * @return The parsed log events.
     * @throws ParseException
     *             when user supplied expressions are the problem
     * @throws InternalSynopticException
     *             when Synoptic code is the problem
     */
    public ArrayList<EventNode> parseTraceFile(File file, int linesToRead)
            throws ParseException, InternalSynopticException {
        String fileName = "";
        try {
            fileName = file.getAbsolutePath();
            FileInputStream fstream = new FileInputStream(file);
            InputStreamReader fileReader = new InputStreamReader(fstream);
            return parseTrace(fileReader, fileName, linesToRead);
        } catch (IOException e) {
            String error = "Error while attempting to read log file ["
                    + fileName + "]: " + e.getMessage();
            logger.severe(error);
            throw new ParseException(error);
        }
    }

    /**
     * Parses a string containing a log into a list of log events.
     * 
     * @param trace
     *            The trace, with lines separated by newlines.
     * @param traceName
     *            The name for this trace -- maps to the FILE parse group.
     * @param linesToRead
     *            Bound on the number of lines to read. Negatives indicate
     *            unbounded.
     * @return The parsed log events.
     * @throws ParseException
     *             when user supplied expressions are the problem
     * @throws InternalSynopticException
     *             when Synoptic code is the problem
     */
    public ArrayList<EventNode> parseTraceString(String trace,
            String traceName, int linesToRead) throws ParseException {
        if (trace == null) {
            throw new ParseException("Trace string cannot be null.");
        }
        if (traceName == null) {
            throw new ParseException("Trace name string cannot be null.");
        }
        StringReader stringReader = new StringReader(trace);
        try {
            return parseTrace(stringReader, traceName, linesToRead);
        } catch (IOException e) {
            String error = "Error while reading string [" + traceName + "]: "
                    + e.getMessage();
            logger.severe(error);
            throw new ParseException(error);
        }
    }

    private ArrayList<EventNode> parseTrace(StringReader stringReader,
            String traceName, int linesToRead) throws ParseException,
            IOException, InternalSynopticException {
        // TODO Auto-generated method stub
        return null;
    }
}
