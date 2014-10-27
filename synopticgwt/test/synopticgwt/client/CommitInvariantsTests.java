package synopticgwt.client;

import org.junit.Test;

import com.google.gwt.user.client.rpc.AsyncCallback;

import synopticgwt.shared.GWTGraph;

/**
 * Tests the commitInvaraints RPC.
 */
public class CommitInvariantsTests extends SynopticGWTTestCase {
    /**
     * A test to commit invariants without an uploaded log and with a null set
     * of hashes.
     */
    @Test
    public void testCommitInvariantsWithoutParsing() {
        try {
            service.commitInvariants(null, new AsyncCallback<GWTGraph>() {
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
