package synopticgwt.client;

import org.junit.Test;

import com.google.gwt.user.client.rpc.AsyncCallback;

import synopticgwt.shared.GWTGraph;

/**
 * Tests the getFinalModel RPC.
 */
public class GetFinalModelTests extends SynopticGWTTestCase {
    /**
     * A test to get final model without an uploaded log.
     */
    @Test
    public void testGetFinalModelWithoutParsing() {
        try {
            service.getFinalModel(new AsyncCallback<GWTGraph>() {
                @SuppressWarnings("synthetic-access")
                @Override
                public void onFailure(Throwable caught) {
                    // Declare the test as complete.
                    finishTest();
                }

                @Override
                public void onSuccess(GWTGraph graph) {
                    fail("Unexpected RPC success.");
                }
            });
        } catch (Exception e) {
            fail(e.toString());
        }
        delayTestFinish(testFinishDelay);
    }
}
