package csight.mc.parallelizer;

import java.util.List;

/**
 * A MC Parallelizer task that contains a command to the Parallelizer, the
 * corresponding inputs, and the current refinement counter in CSightMain. The
 * START_K command should have inputs of size K = min{numParallel,
 * invsToCheck.size()}. The START_ONE command should have inputs of size = 1.
 * The STOP_ALL command should have corresponding input of null.
 */
public class ParallelizerTask {

    public enum ParallelizerCommands {

        START_K,

        START_ONE,

        STOP_ALL;
    }

    protected final ParallelizerCommands cmd;
    protected final List<ParallelizerInput> inputs;
    protected final int refinementCounter;

    public ParallelizerTask(ParallelizerCommands cmd,
            List<ParallelizerInput> inputs, int refinementCounter) {
        this.cmd = cmd;
        this.inputs = inputs;
        this.refinementCounter = refinementCounter;
    }

}
