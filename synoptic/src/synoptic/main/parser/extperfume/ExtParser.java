package synoptic.main.parser.extperfume;

/**
 * Extended Parser for Perfume to parse multiple performance metrics,
 * intermediate models, and refinement instructions.
 */
public class ExtParser {
    // TODO: parser for extended Perfume functions. Parsing intermediate models,
    // parsing refinement commands.

    // TODO: design a communication protocol. One for parsing an intermediate
    // model input with corresponding output tool, another that parses
    // refinement commands.

    // TODO: log parser that takes in one file as a trace with first line
    // specifying "event,<resource name>..." and the rest of lines as the events
    // with resource values following format of first line. Each resource for
    // one event is to be parsed as one invariant. (Eventually redesign so the
    // first line can be specified as options?)

}
