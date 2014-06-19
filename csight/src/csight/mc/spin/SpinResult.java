package csight.mc.spin;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import csight.mc.MCProcess;
import csight.mc.MCResult;
import csight.mc.MCSyntaxException;
import csight.mc.MCcExample;
import csight.mc.VerifyOutputParseException;

import synoptic.model.channelid.ChannelId;
import synoptic.model.event.DistEventType;

/**
 * Holds the result of a Spin model checker run.
 */
public class SpinResult extends MCResult {

    static String traceRe = "^CSightTrace\\[(.*)\\]$";
    static String unsafeRe = "^pan: wrote (.*\\.trail)$";
    static String safeRe = "^No errors found -- did you verify all claims\\?$";
    static String syntaxErrRe = "Error: syntax error";

    // Three capture groups. Event, pid and something I'm not sure about.
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

    private void parseVerifyOutput(List<String> lines) {

        boolean detectedSafety = false;
        for (String line : lines) {
            if (line.contains(syntaxErrRe)) {
                throw new MCSyntaxException(line);
            }
            if (line.matches(unsafeRe)) {
                modelIsSafe = false;
                detectedSafety = true;
            }
        }

        if (!modelIsSafe) {
            try {
                File currentPath = new java.io.File("./test-output/");
                MCProcess trailProcess = new MCProcess(new String[] { mcPath,
                        "-t", "-T", "-v", "csight.pml" }, "", currentPath, 60);
                trailProcess.runProcess();
                trailLines = trailProcess.getInputStreamContent();
                parseCounterExample(trailLines);
            } catch (Exception e) {
                // TODO Properly handle this. For now, this is to test if this
                // works here.
            }

        }
        if (!detectedSafety) {
            throw new VerifyOutputParseException(
                    "Unable to parse verify result: cannot determine model safety");
        }

    }

    private void parseCounterExample(List<String> lines)
            throws VerifyOutputParseException {
        cExample = new MCcExample();
        Pattern p = Pattern.compile(traceRe);

        for (String line : lines) {
            Matcher m = p.matcher(line);
            if (m.find()) {
                assert m.groupCount() == 1;
                String event = m.group(1);
                DistEventType e = parseEventStr(event);
                if (e != null) {
                    cExample.addScmEventStrToPath(e);
                }
            }
        }

    }

    private DistEventType parseEventStr(String event) {
        int cIndex;
        ChannelId chId;

        Matcher m = eventTypeLocalPat.matcher(event);

        if (m.find()) {
            assert m.groupCount() == 3;
            int pid = Integer.parseInt(m.group(2));
            return DistEventType.LocalEvent(m.group(1), pid);
        }

        m = eventTypeRecvPat.matcher(event);
        if (m.find()) {
            assert m.groupCount() == 2;
            cIndex = Integer.parseInt(m.group(1));
            assert cIndex >= 0 && cIndex < cids.size();

            chId = cids.get(cIndex);
            return DistEventType.RecvEvent(m.group(2), chId);
        }

        m = eventTypeSendPat.matcher(event);
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
        for (String line : trailLines) {
            ret += line + "\n";
        }
        return ret;
    }
}
