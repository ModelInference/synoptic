package csight.mc;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.TimeoutException;

import csight.util.Util;

/** Represents a system process that hosts the model checker execution. */
public class MCProcess {

    // The started underlying MC process.
    private Process process = null;

    String[] command;
    String stdinInput;
    File processDir;
    int timeoutSecs;

    public MCProcess(String[] command, String stdinInput, File processDir,
            int timeoutSecs) {
        assert timeoutSecs > 0;
        assert !processDir.equals("");
        assert command.length > 0;

        this.command = command;
        this.stdinInput = stdinInput;
        this.processDir = processDir;
        this.timeoutSecs = timeoutSecs;
    }

    /**
     * Creates and executes a command in processDir, and passes along an
     * stdinInput to its stdin (if it's not the empty string). The process will
     * be forcibly terminated after timeoutSecs -- timeout in seconds to wait
     * for the process to terminate before killing it.
     * 
     * @return the process
     * @throws IOException
     * @throws InterruptedException
     *             when the started process had to be killed forcibly
     * @throws TimeoutException
     *             when the started process is killed after timing out
     */
    public void runProcess() throws IOException, InterruptedException,
            TimeoutException {
        ProcessBuilder pBuilder = new ProcessBuilder(command);
        pBuilder.directory(processDir);

        // Start the process.
        process = pBuilder.start();

        // Write an input to the stdin of the process.
        if (!stdinInput.equals("")) {
            OutputStream oStream = process.getOutputStream();
            oStream.write(stdinInput.getBytes());
            oStream.close();
        }

        // Timer setup.
        ProcessKillTimer killTimerThread = new ProcessKillTimer(process,
                timeoutSecs);
        Thread t = new Thread(killTimerThread);
        t.start();

        while (true) {
            try {
                // Wait until the verify process terminates.
                process.waitFor();
            } catch (InterruptedException e) {
                // The current thread was interrupted, so stop the process
                // and throw InterruptedException.
                killTimerThread.killed = true;

                process.destroy();
                t.interrupt();

                throw new InterruptedException("MC Process killed.");

            }
            break;
        }

        // Clean up the timer thread.
        if (!killTimerThread.killed) {
            // The killed flag is false: verify process terminated naturally.
            // Make sure that the timer thread stops waiting.
            killTimerThread.killed = true;
            t.interrupt();
        } else {
            // Otherwise: the process had to be killed by the timer thread.
            throw new TimeoutException("MC process timed out.");
        }
    }

    /**
     * Reads and caches content from inputStream. Can only be invoked after
     * runProcess.
     * 
     * @return a list of lines from inputStream.
     * @throws IOException
     */
    public List<String> getInputStreamContent() throws IOException {
        assert process != null;

        InputStream inputStream = process.getInputStream();

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
                // Sleep for timeout seconds and then kill the MC process if it
                // hasn't been killed yet.
                wait(timeout * 1000L);
            }
        } catch (InterruptedException e) {
            // This happens when either (1) the main thread calls interrupt(),
            // which happens if the process p terminated naturally, or (2) we
            // were interrupted by the JVM or executor service higher up the
            // stack. Either way, we want to stop the process, possibly a second
            // time.
            if (killed == true) {
                return;
            }
        }
        // Kill the process and set the flag so we know we killed it.
        killed = true;
        proc.destroy();
    }
}
