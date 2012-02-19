package synopticgwt.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Represents Synoptic parsing/processing options that are filled out by the
 * user on the input panel, and sent to the server for processing.
 */
public class GWTSynOpts implements IsSerializable {
    public String logLines;
    public List<String> regExps;
    public String partitionRegExp;
    public String separatorRegExp;
    public boolean ignoreNonMatchedLines;
    public boolean manualRefineCoarsen;
    public boolean onlyMineInvs;

    public GWTSynOpts() {
        // Empty constructor is necessary for serialization.
    }

    public GWTSynOpts(String logLines, List<String> regExps,
            String partitionRegExp, String separatorRegExp,
            boolean ignoreNonMatchedLines, boolean manualRefineCoarsen,
            boolean onlyMineInvs) {
        this.logLines = logLines;
        this.regExps = regExps;
        this.partitionRegExp = partitionRegExp;
        this.separatorRegExp = separatorRegExp;
        this.ignoreNonMatchedLines = ignoreNonMatchedLines;
        this.manualRefineCoarsen = manualRefineCoarsen;
        this.onlyMineInvs = onlyMineInvs;
    }
}
