package synopticgwt.client.util;

/**
 * A non-abstract version of the base AbstractAsyncCallback class.
 * 
 * <pre>
 * NOTE: this class can only be sub-classed by classes that have an
 * empty constructor. All other classes must extend AbstractAsyncCallback
 * and maintain its invariant of initialize() as final method in a constructor.
 * </pre>
 * 
 * @param <T>
 *            The callback return value type.
 */
public class ErrorReportingAsyncCallback<T> extends AbstractErrorReportingAsyncCallback<T> {

    public ErrorReportingAsyncCallback(ProgressWheel pWheel,
            String defaultErrorMsg) {
        super(pWheel, defaultErrorMsg);
        initialize();
    }

    /**
     * Constructor for the case when there is no progress wheel associated with
     * an RPC.
     */
    public ErrorReportingAsyncCallback(String defaultErrorMsg) {
        this(null, defaultErrorMsg);
    }
}
