package csight.mc.spin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.io.filefilter.WildcardFileFilter;

import csight.mc.MC;
import csight.mc.MCProcess;
import csight.mc.MCResult;
import csight.mc.MCSyntaxException;
import csight.mc.mcscm.Os;

import synoptic.model.channelid.ChannelId;

/**
 * This class is a bridge for the Spin model checker. Spin itself does not
 * directly check models. Instead, it generates source code for a model checker.
 * We'll need to compile the source code and then execute the resulting program
 * to verify. The generated program is called pan.
 */
public class Spin extends MC {

    static Logger logger = Logger.getLogger("Spin");

    public Spin(String mcPath) {
        super(mcPath);
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

        // Tracking how much of our timeout is left. This is coarse-grained.
        int timeoutSecsLeft = timeoutSecs;

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
        mcProcess = new MCProcess(command, "", currentPath, timeoutSecsLeft);
        // We run Spin to generate the source for the model checker.
        timeoutSecsLeft = timeoutSecsLeft - generateMCSource(mcProcess);

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
        command = new String[] { "gcc", "-O", "-DBFS", "-DBFS_DISK", "-DREACH",
                "-o", "pan", "pan.c" };
        mcProcess = new MCProcess(command, "", currentPath, timeoutSecsLeft);
        // We compile the source from the previous step.
        timeoutSecsLeft = timeoutSecsLeft - compileMC(mcProcess);

        // Run Pan verifier.
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
        command = new String[] { panExec, "-q", "-m1000", "-n" };

        mcProcess = new MCProcess(command, "", currentPath, timeoutSecsLeft);
        mcProcess.runProcess();

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
     * Spin's result won't be the exact thing we need. We need to take an
     * additional step to get the counterexample. This is handled by SpinResult.
     */
    @Override
    public MCResult getVerifyResult(List<ChannelId> cids) throws IOException {
        List<String> lines = mcProcess.getInputStreamContent();

        logger.info("Spin returned: " + lines.toString());

        MCResult ret = new SpinResult(lines, cids, mcPath);
        logger.info(ret.toRawString());
        cleanUpFiles();
        return ret;
    }

    /**
     * This cleans up the files generated by this class. This includes the
     * Promela representation of the CFSM, the model checker source, the model
     * checker executable and the trail file.
     */
    public void cleanUpFiles() {
        File outputDir = new File(".");
        String[] filters = new String[] { "pan*", "csight.pml*" };
        FileFilter filter = new WildcardFileFilter(filters);
        File[] spinFiles = outputDir.listFiles(filter);
        for (File spinFile : spinFiles) {
            spinFile.delete();
        }
    }

}
