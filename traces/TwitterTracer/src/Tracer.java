import MessageTrace.FullTrace;
import MessageTrace.WrappedMessage;
import MessageTrace.TraceSet;
import com.google.protobuf.ByteString;

// Tracer class to create a set of traces, log each entry in each trace,
// and return the final TraceSet.
public class Tracer {
    static TraceSet.Builder traceSet;
    static FullTrace.Builder singleTrace;

    // Initialize the set of Traces and the first single trace
    public static void create() {
        traceSet = TraceSet.newBuilder();
        singleTrace = FullTrace.newBuilder();
    }

    // Builds the previous single trace, adds it to the trace set,
    // and initializes a new single trace
    public static void newTrace() {
        traceSet.addFullTrace(singleTrace.build());
        singleTrace = FullTrace.newBuilder();
    }

    // Builds the current single trace and adds it to the trace set
    public static void finishSingle() {
        traceSet.addFullTrace(singleTrace.build());
    }

    // Builds the trace set and returns it.
    public static TraceSet completeSet() {
        return traceSet.build();
    }

    // Logs a single message in the current trace
    public static void log(int src, int dst, long timeStamp,
            String messageType, ByteString message) {
        WrappedMessage.Builder resultMessage = WrappedMessage.newBuilder();
        stdout.print("Building message...");
        resultMessage.setSrc(src);
        resultMessage.setDst(dst);
        resultMessage.setTimestamp(timeStamp);
        resultMessage.setType(messageType);
        resultMessage.setTheMessage(message);

        singleTrace.addWrappedMessage(resultMessage.build());
    }
}