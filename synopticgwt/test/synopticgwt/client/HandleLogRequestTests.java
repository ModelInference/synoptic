package synopticgwt.client;

import java.util.List;

import org.junit.Test;

import com.google.gwt.user.client.rpc.AsyncCallback;

import synopticgwt.shared.LogLine;

/**
 * A test for the handleLogRequest RPC.
 */
public class HandleLogRequestTests extends SynopticGWTTestCase {
    /**
     * A test to handle log request without an uploaded log, and random nodeID.
     */
    @Test
    public void testHandleLogReqWithoutParsing() {
        try {
            service.handleLogRequest(0, new AsyncCallback<List<LogLine>>() {
                @SuppressWarnings("synthetic-access")
                @Override
                public void onFailure(Throwable caught) {
                    // Declare the test as complete.
                    finishTest();
                }

                @Override
                public void onSuccess(List<LogLine> lines) {
                    fail("Unexpected RPC success.");
                }
            });
        } catch (Exception e) {
            fail(e.toString());
        }
        delayTestFinish(testFinishDelay);
    }
}
