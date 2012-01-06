package synopticgwt.client;

import org.junit.Test;

import com.google.gwt.user.client.rpc.AsyncCallback;

import synopticgwt.shared.GWTGraph;

/**
 * Tests the coarsenOneStep RPC.
 */
public class CoarsenOneStepTests extends SynopticGWTTestCase {
    /**
     * A test to coarsen the model, without an uploaded log.
     */
    @Test
    public void testRefineWithoutParsing() {
        try {
            service.coarsenOneStep(new AsyncCallback<GWTGraph>() {
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
