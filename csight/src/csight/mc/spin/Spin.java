package csight.mc.spin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.io.filefilter.WildcardFileFilter;

import csight.mc.MC;
import csight.mc.MCProcess;
import csight.mc.MCResult;
import csight.mc.MCSyntaxException;
import csight.mc.mcscm.Os;
import csight.util.Util;

import synoptic.model.channelid.ChannelId;

/**
 * This class is a bridge for the Spin model checker. Spin itself does not
 * directly check models. Instead, it generates source code for a model checker.
 * We'll need to compile the source code and then execute the resulting program
 * to verify. The generated program is called pan.
 * <p>
 * This class generates and deletes files in the current directory that match:
 * <ul>
 * <li>csight.pml</li>
 * <li>csight.pml.*.trail</li>
 * <li>pan</li>
 * <li>pan.*</li>
 * </ul>
 * </p>
 */
public class Spin extends MC {

    static Logger logger = Logger.getLogger("Spin");

    /**
     * Stores the returned lines from model checking. These lines need to be
     * parsed to determine which trail file to use for the counterexamples.
     * Spin's result won't be the exact thing we need. We need to take an
     * additional step to get the counterexample. This is handled by SpinResult.
     */
    Map<Integer, List<String>> returnedLines;

    /**
     * Stores the counterexamples parsed from the Spin trail file. A null
     * MCResult means that the corresponding run did not complete.
     */
    Map<Integer, MCResult> returnedResults;

    public Spin(String mcPath) {
        super(mcPath);
        returnedLines = Util.newMap();
        returnedResults = Util.newMap();
    }

    /**
     * This writes the Promela input to a file, generates and compiles the model
     * checker source. Spin does not use stdin for this. At the moment, we
     * require that the user has gcc on their system. The model checker will
     * return a result after it finishes running. To retrieve the result, call
     * getVerifyResult.
     */
    @Override
    public void verify(String input, int timeoutSecs) throws IOException,
            InterruptedException {
        prepare(input, timeoutSecs);
        verify(input, timeoutSecs, 0);
    }

    /**
     * Prepares the Spin model checker. This writes the Promela input to a file,
     * generates and compiles the model checker source. Spin does not use stdin
     * for this. At the moment, we require that the user has gcc on their
     * system.
     * 
     * @param input
     *            The input Promela string
     * @param timeoutSecs
     * @throws InterruptedException
     * @throws IOException
     */
    public void prepare(String input, int timeoutSecs) throws IOException,
            InterruptedException {
        // Empty the maps since a new mc instance is not instantiated each time.
        returnedLines.clear();
        returnedResults.clear();

        File currentPath = new java.io.File(".");

        // Write Promela file.
        File promelaFile = new java.io.File("csight.pml");
        Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(promelaFile)));
        writer.write(input);
        writer.close();

        // Run spin to create C source.
        String[] command = new String[] { mcPath, "-a", "csight.pml" };
        // Spin does not use stdin for Promela input.
        mcProcess = new MCProcess(command, "", currentPath, timeoutSecs);
        // We run Spin to generate the source for the model checker.
        generateMCSource(mcProcess);

        // Currently assuming gcc is on the system. For Windows, we're assuming
        // it's on the system PATH.

        /*
         * -DBFS sets the verify to use BFS. We use BFS over DFS since DFS
         * reaches the max search depth very quickly. Since not every state has
         * been reached, this triggers an error and creates a trail file that we
         * do not want.
         */
        /* -DBFS_DISK enables disk caching during BFS. */
        /* -DREACH checks for errors up to the default depth. */
        // The others are common gcc parameters.
        command = new String[] { "gcc", "-O", "-DBFS", "-DREACH", "-o", "pan",
                "pan.c" };
        mcProcess = new MCProcess(command, "", currentPath, timeoutSecs);
        // We compile the source from the previous step.
        compileMC(mcProcess);
    }

    /**
     * The model checker will return a result after it finishes running. This
     * result is saved. To retrieve the all of the results, call
     * getVerifyResults.
     * 
     * @param input
     *            The input Promela string
     * @param timeoutSecs
     *            Seconds before timing out.
     * @param invNum
     *            The number for the invariant we are checking.
     * @throws IOException
     * @throws InterruptedException
     */

    public void verify(String input, int timeoutSecs, int invNum)
            throws IOException, InterruptedException {

        File currentPath = new java.io.File(".");
        // Run Pan verifier for each invariant.
        String panExec;
        if (Os.isWindows()) {
            panExec = "pan.exe";
        } else {
            panExec = "./pan";
        }

        // -q makes the verifier check for empty message channels.

        // -m sets the max search depth. This may be an option later on.
        /*
         * -n suppresses the unreached states output from pan. States can be
         * unreached due to a never claim being accepted. This information is
         * not useful to us and clutters up the console when debugging.
         */

        // -N is used to specify which claim to use.

        // -t is used to specify the suffix for the trail file.
        String[] command = new String[] { panExec, "-q", "-m250", "-n", "-N",
                "never_" + invNum, "-t" + invNum + ".trail" };

        mcProcess = new MCProcess(command, "", currentPath, timeoutSecs);

        mcProcess.runProcess();
        // Save results from running Spin.
        returnedLines.put(invNum, mcProcess.getInputStreamContent());

    }

    /**
     * Spin generates the source code for the model checker. This will need to
     * be compiled and executed.
     * 
     * @param process
     * @return Time taken to run process.
     * @throws IOException
     * @throws InterruptedException
     */
    private int generateMCSource(MCProcess process) throws IOException,
            InterruptedException {
        final String syntaxErrRe = "Error: syntax error";
        final String missingPmlErrRe = "spin: error: No file";
        long startTime = System.currentTimeMillis();
        process.runProcess();
        List<String> lines = process.getInputStreamContent();
        for (String line : lines) {
            // The syntax for the Promela is incorrect.
            if (line.contains(syntaxErrRe)) {
                throw new MCSyntaxException(line);
            }
            // Spin can't access Promela file.
            if (line.contains(missingPmlErrRe)) {
                throw new IOException(line);
            }
        }
        return (int) (System.currentTimeMillis() - startTime) / 1000;
    }

    /**
     * Compiles the pan source into the model checker. There must be source code
     * files for this to work.
     * 
     * @param process
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    private int compileMC(MCProcess process) throws IOException,
            InterruptedException {
        long startTime = System.currentTimeMillis();
        process.runProcess();

        return (int) (System.currentTimeMillis() - startTime) / 1000;
    }

    /**
     * Parses a single list of returned lines into a MCResult. We keep this for
     * function for compatibility with the generic refinement loop.
     */
    @Override
    public MCResult getVerifyResult(List<ChannelId> cids) throws IOException {
        // Retrieve the only Spin response.
        assert returnedLines.size() == 1;
        List<String> lines = returnedLines.get(0);

        logger.info(lines.toString());
        MCResult ret = getVerifyResult(cids, lines);
        cleanUpFiles();
        return ret;
    }

    /**
     * Parses a single MCResult from the supplied lines.
     * 
     * @param cids
     * @param lines
     *            Lines returned from running the Spin process.
     * @return
     */
    private MCResult getVerifyResult(List<ChannelId> cids, List<String> lines) {
        logger.info("Spin returned: " + lines.toString());

        MCResult ret = new SpinResult(lines, cids, mcPath);
        return ret;
    }

    /**
     * Retrieves multiple MCResults from parsing the Spin counterexamples. It is
     * recommended that the iterator is not used for the returned result as a
     * null result is valid for the Map.
     * 
     * @param cids
     * @param numInvsInRun
     *            Number of invariants in this Spin model checking run.
     * @return
     * @throws IOException
     */
    public Map<Integer, MCResult> getMultipleVerifyResults(
            List<ChannelId> cids, int numInvsInRun) throws IOException {

        /*
         * We iterate through returnedLines based on the number of expected
         * results as we may have been interrupted while we were model checking.
         * We don't want to verify runs that did not complete.
         */
        for (int i = 0; i < numInvsInRun; i++) {
            List<String> lines = returnedLines.get(i);
            // If the run for this invariant was interrupted, it would have a
            // null entry for returnedLines. We do not attempt to parse these
            // runs.

            if (lines != null) {
                // Parse only the lines that returned when retrieving
                // counterexamples.
                MCResult ret = getVerifyResult(cids, lines);
                returnedResults.put(i, ret);
            } else {
                returnedResults.put(i, null);
            }
        }
        assert numInvsInRun == returnedResults.size();
        // Clean files once we're done with them.
        cleanUpFiles();
        return returnedResults;
    }

    /**
     * This cleans up the files generated by this class. This includes the
     * Promela representation of the CFSM, the model checker source, the model
     * checker executable and the trail file.
     */
    public void cleanUpFiles() {
        File outputDir = new File(".");
        String[] filters = new String[] { "pan", "pan.*", "csight.pml",
                "csight.pml.*.trail" };
        FileFilter filter = new WildcardFileFilter(filters);
        File[] spinFiles = outputDir.listFiles(filter);
        for (File spinFile : spinFiles) {
            spinFile.delete();
        }
    }

}
