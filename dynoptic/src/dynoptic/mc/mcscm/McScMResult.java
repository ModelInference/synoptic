package dynoptic.mc.mcscm;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dynoptic.mc.MCResult;
import dynoptic.mc.MCcExample;
import dynoptic.model.fifosys.channel.channelid.InvChannelId;
import dynoptic.model.fifosys.channel.channelid.LocalEventsChannelId;

import synoptic.model.channelid.ChannelId;
import synoptic.model.event.DistEventType;

/**
 * The result of running the McScM verify tool. Contains parsing utilities for
 * parsing the output of the verify tool.
 */
public class McScMResult extends MCResult {
    static String safeRe = "^Result: Model is safe.";
    static String unsafeRe = "^Result: Model is unsafe.*";
    static String syntaxErrRe = "^Syntaxical error:.*";
    static String counterExRe = "^Counterexample:";
    static String counterExEventRe = "^.*\\|- (.*) -\\|.*";

    // For parsing the counter-example details.
    static Pattern eventTypeRecvPat = Pattern.compile("^(\\d+) \\? (.+)");
    static Pattern eventTypeSendPat = Pattern.compile("^(\\d+) ! (.+)");

    /**
     * Creates a new VerifyResult from the raw verify output, broken into lines.
     */
    public McScMResult(List<String> verifyRawLines, List<ChannelId> cids)
            throws VerifyOutputParseException {
        super(verifyRawLines, cids);
        parseVerifyOutput(verifyRawLines);
    }

    // //////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        if (cExample != null) {
            return "Verify-CExample: " + this.cExample.toString();
        }
        return "Verify-safe";
    }

    // //////////////////////////////////////////////////////////////////

    private void parseVerifyOutput(List<String> lines)
            throws VerifyOutputParseException {

        boolean detectedSafety = false;
        int lineCnt = 0;
        for (String line : lines) {
            if (line.matches(syntaxErrRe)) {
                throw new ScmSyntaxException(line);
            }

            if (line.matches(safeRe)) {
                this.modelIsSafe = true;
                detectedSafety = true;
            }
            if (line.matches(unsafeRe)) {
                this.modelIsSafe = false;
                detectedSafety = true;
            }
            if (line.matches(counterExRe)) {
                parseCounterExample(lines, lineCnt);
            }
            lineCnt += 1;
        }
        if (!detectedSafety) {
            throw new VerifyOutputParseException(
                    "Unable to parse verify result: cannot determine model safety");
        }
    }

    /**
     * Parses and sets the counter-example sequence of actions corresponding to
     * the property violation of the model.
     * 
     * @param lines
     * @param lineCnt
     *            Index of the line in lines that contains the "Counterexample:"
     *            header of the counterexample section in the output.
     */
    private void parseCounterExample(List<String> lines, int lineCnt) {
        cExample = new MCcExample();
        Pattern p = Pattern.compile(counterExEventRe);

        for (String line : lines.subList(lineCnt + 1, lines.size())) {
            Matcher m = p.matcher(line);
            if (m.find()) {
                assert m.groupCount() == 1;
                String event = m.group(1);
                DistEventType e = parseScmEventStr(event);
                if (e != null) {
                    cExample.addScmEventStrToPath(e);
                }
            }
        }
    }

    /**
     * Parses a string that represents an event in a counter-example produced by
     * McScM and uses the ordered list of channel Ids to create the
     * corresponding EventType instance.
     * 
     * <pre>
     * TODO: assert that the returned EventType is in the alphabet for the CFSM that we are checking.
     * </pre>
     */
    private DistEventType parseScmEventStr(String event) {

        int cIndex;
        ChannelId chId;

        // Try to parse event as a receive event.
        Matcher m = eventTypeRecvPat.matcher(event);
        if (m.find()) {
            assert m.groupCount() == 2;
            cIndex = Integer.parseInt(m.group(1));
            assert cIndex >= 0 && cIndex < cids.size();

            chId = cids.get(cIndex);
            return DistEventType.RecvEvent(m.group(2), chId);
        }

        // Try to parse event as a send event.
        m = eventTypeSendPat.matcher(event);
        if (!m.find()) {
            throw new VerifyOutputParseException(
                    "Could not parse event in an McScm counter-example: "
                            + event);
        }
        assert m.groupCount() == 2;
        cIndex = Integer.parseInt(m.group(1));
        assert cIndex >= 0 && cIndex < cids.size();

        chId = cids.get(cIndex);

        if (chId instanceof InvChannelId) {
            // The event is a synthetic event added for tracking
            // invariant-relevant
            // event types.
            return null;
        } else if (chId instanceof LocalEventsChannelId) {
            // The event is a local event, though we simulate it as a send with
            // McScM.
            return ((LocalEventsChannelId) chId).getEventType(m.group(2));
        }

        return DistEventType.SendEvent(m.group(2), chId);
    }
}
