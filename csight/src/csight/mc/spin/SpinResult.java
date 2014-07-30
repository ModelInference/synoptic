package csight.mc.spin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import csight.mc.MCProcess;
import csight.mc.MCResult;
import csight.mc.MCcExample;
import csight.mc.VerifyOutputParseException;

import synoptic.model.channelid.ChannelId;
import synoptic.model.event.DistEventType;

/**
 * Holds the result of a Spin model checker run. From the returned verify lines
 * from Spin, we parse for the regular expression that indicates that we have a
 * trail file. After finding this line, we run Spin in guided simulation mode to
 * retrieve the counterexample.
 */
public class SpinResult extends MCResult {

    static String traceRe = "^CSightTrace\\[(.*)\\]$";
    static String unsafeRe = "^pan: wrote (.*\\.trail)$";
    static String safeRe = "^State-vector \\d* byte, depth reached \\d*, errors: 0$";

    // Three capture groups. Event, pid and the name of the process.
    static Pattern eventTypeLocalPat = Pattern
            .compile("^(.*)p([\\d]+)L_([\\d]+)$");

    // So far, two capture groups. Channel id and event.
    static Pattern eventTypeRecvPat = Pattern
            .compile("^(\\d*)-.*:\\d*->\\d* \\? (.*)$");
    static Pattern eventTypeSendPat = Pattern
            .compile("^(\\d*)-.*:\\d*->\\d* ! (.*)$");

    public List<String> trailLines;

    private String mcPath;

    public SpinResult(List<String> verifyRawLines, List<ChannelId> cids,
            String mcPath) throws VerifyOutputParseException {
        super(verifyRawLines, cids);
        this.mcPath = mcPath;
        parseVerifyOutput(verifyRawLines);
    }

    /**
     * Parses the verification output for the safety of the model and calls
     * parseCounterExample if the model is unsafe.
     * 
     * @param lines
     */
    private void parseVerifyOutput(List<String> lines) {

        boolean detectedSafety = false;
        String trailName = "";
        for (String line : lines) {
            if (line.matches(unsafeRe)) {
                modelIsSafe = false;
                detectedSafety = true;
                // Obtain the file name of the trail. We are guaranteed to find
                // a match if we are in this if statement.
                Matcher m = Pattern.compile(unsafeRe).matcher(line);
                m.find();
                trailName = m.group(1);
                continue;
            }
            if (line.matches(safeRe)) {
                modelIsSafe = true;
                detectedSafety = true;
                continue;
            }
        }

        if (!modelIsSafe) {
            try {
                File currentPath = new java.io.File(".");
                // Run a new instance of Spin to read the trail file.
                // The warnings can be ignored for now. They are a result of
                // nesting atomics and d_steps as a result of inlines.

                // -t triggers a guided simulation using the trail file.
                // -T suppresses indentation from print statements.
                // -k selects the trail to parse for the counterexample.
                MCProcess trailProcess = new MCProcess(new String[] { mcPath,
                        "-t", "-T", "-k", trailName, "csight.pml" }, "",
                        currentPath, 20);
                trailProcess.runProcess();
                trailLines = trailProcess.getInputStreamContent();
                parseCounterExample(trailLines);

            } catch (InterruptedException e) {
                // TODO KS Properly handle this. For now, this is to test if
                // this works here.
                throw new VerifyOutputParseException(
                        "Unable to parse verify result: Spin interrupted during parsing.");
            } catch (IOException e) {
                throw new VerifyOutputParseException(
                        "Unable to parse verify result: cannot access trail file.");
            }

        }

        if (!detectedSafety) {
            throw new VerifyOutputParseException(
                    "Unable to parse verify result: cannot determine model safety");
        }

    }

    /**
     * Parses a counterexample from the error trail.
     * 
     * @param lines
     *            An error trail from Spin.
     * @throws VerifyOutputParseException
     */
    private void parseCounterExample(List<String> lines)
            throws VerifyOutputParseException {
        cExample = new MCcExample();
        Pattern p = Pattern.compile(traceRe);

        for (String line : lines) {
            Matcher m = p.matcher(line);
            if (m.find()) {
                assert m.groupCount() == 1;
                // We retrieve an event string from the counterexample line and
                // parse an event from it.
                String event = m.group(1);
                DistEventType e = parseEventStr(event);
                if (e != null) {
                    cExample.addScmEventStrToPath(e);
                }
            }
        }

    }

    /**
     * Parses an event from an event string in the counterexample.
     * 
     * @param event
     * @return
     */
    private DistEventType parseEventStr(String event) {
        int cIndex;
        ChannelId chId;

        // Matching local events.
        Matcher m = eventTypeLocalPat.matcher(event);

        if (m.find()) {
            assert m.groupCount() == 3;
            int pid = Integer.parseInt(m.group(2));
            return DistEventType.LocalEvent(m.group(1), pid);
        }

        // Matching receive events.
        m = eventTypeRecvPat.matcher(event);
        if (m.find()) {
            assert m.groupCount() == 2;
            cIndex = Integer.parseInt(m.group(1));
            assert cIndex >= 0 && cIndex < cids.size();

            chId = cids.get(cIndex);
            return DistEventType.RecvEvent(m.group(2), chId);
        }

        // Matching send events.
        m = eventTypeSendPat.matcher(event);
        // If nothing matches, we have a problem.
        if (!m.find()) {
            throw new VerifyOutputParseException(
                    "Could not parse event in Spin trail: " + event);
        }
        assert m.groupCount() == 2;
        cIndex = Integer.parseInt(m.group(1));
        assert cIndex >= 0 && cIndex < cids.size();

        chId = cids.get(cIndex);
        return DistEventType.SendEvent(m.group(2), chId);
    }

    public String toRawString() {
        String ret = "";
        for (String line : verifyRawLines) {
            ret += line + "\n";
        }
        if (modelIsSafe()) {
            ret += "Model is safe.\n";
        } else {
            if (trailLines != null) {
                for (String line : trailLines) {
                    ret += line + "\n";
                }
            } else {
                ret += ("Trail is empty.\n");
            }
        }
        return ret;
    }
}
