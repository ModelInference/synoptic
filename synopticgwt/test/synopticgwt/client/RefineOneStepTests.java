package synopticgwt.client;

import org.junit.Test;

import com.google.gwt.user.client.rpc.AsyncCallback;

import synopticgwt.shared.GWTGraphDelta;

/**
 * Tests the refineOneStep RPC.
 */
public class RefineOneStepTests extends SynopticGWTTestCase {
    /**
     * A test to refine the model, without an uploaded log.
     */
    @Test
    public void testRefineWithoutParsing() {
        try {
            service.refineOneStep(new AsyncCallback<GWTGraphDelta>() {
                @SuppressWarnings("synthetic-access")
                @Override
                public void onFailure(Throwable caught) {
                    // Declare the test as complete.
                    finishTest();
                }

                @Override
                public void onSuccess(GWTGraphDelta graph) {
                    fail("Unexpected RPC success.");
                }
            });
        } catch (Exception e) {
            fail(e.toString());
        }
        delayTestFinish(testFinishDelay);
    }
}
