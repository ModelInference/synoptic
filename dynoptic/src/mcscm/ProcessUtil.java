package mcscm;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;

import dynoptic.util.Util;

/** Some process handling routines. */
public class ProcessUtil {

    /**
     * Creates and executes a command in processDir, and passes along an
     * scmString on its stdin. The process will be forcibly terminated after
     * timeoutSecs -- timeout in seconds to wait for the process to terminate
     * before killing it.
     * 
     * @return the process
     * @throws IOException
     * @throws InterruptedException
     *             when the started process had to be killed forcibly
     */
    public static Process runVerifyProcess(String[] command, String scmInput,
            File processDir, int timeoutSecs) throws IOException,
            InterruptedException {
        ProcessBuilder pBuilder = new ProcessBuilder(command);
        pBuilder.directory(processDir);
        Process p = null;

        // Start the verify process.
        p = pBuilder.start();

        // Write to the scm input to the stdin of the verify process.
        OutputStream oStream = p.getOutputStream();
        oStream.write(scmInput.getBytes());
        oStream.close();

        // Timer setup.
        ProcessKillTimer pkt = new ProcessKillTimer(p, timeoutSecs);
        Thread t = new Thread(pkt);
        t.start();

        // Wait until the verify process terminates.
        p.waitFor();

        // Clean up the timer thread.
        if (!pkt.killed) {
            // The killed flag is false: verify process terminated naturally.
            // Make sure that the timer thread stops waiting.
            t.interrupt();
        } else {
            // Otherwise: the process had to be killed by the timer thread.
            throw new InterruptedException("Verify process killed.");
        }

        return p;
    }

    /**
     * Reads and caches content from inputStream.
     * 
     * @return a list of lines from inputStream.
     * @throws IOException
     */
    public static List<String> getInputStreamContent(InputStream inputStream)
            throws IOException {
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(inputStream));

        List<String> lines = Util.newList();
        String line;

        while ((line = bufferedReader.readLine()) != null) {
            lines.add(line);
        }

        return lines;
    }
}

/**
 * A timer thread that kills a specific process after a timeout.
 */
final class ProcessKillTimer implements Runnable {

    private Process proc;
    private int timeout;
    volatile boolean killed = false;

    /**
     * @param p
     *            the process to be killed after a timeout
     * @param timeout
     *            in seconds
     */
    ProcessKillTimer(Process p, int timeout) {
        this.proc = p;
        this.timeout = timeout;
    }

    @Override
    public void run() {
        try {
            synchronized (this) {
                wait(timeout * 1000L);
            }
        } catch (InterruptedException e) {
            // This happens when the main thread calls interrupt(), which can
            // only happen if the process p terminated naturally.
            return;
        }
        // Kill the process and set the flag so we know we killed it.
        killed = true;
        proc.destroy();
    }
}
