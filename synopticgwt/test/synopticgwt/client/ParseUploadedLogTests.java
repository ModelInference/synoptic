package synopticgwt.client;

import org.junit.Test;

import com.google.gwt.user.client.rpc.AsyncCallback;

import synopticgwt.shared.GWTGraph;
import synopticgwt.shared.GWTInvariantSet;
import synopticgwt.shared.GWTPair;

/**
 * A test of the parseUploadedLog RPC.
 */
public class ParseUploadedLogTests extends SynopticGWTTestCase {
    /**
     * A test to parse an uploaded file without uploading anything, with null
     * values for args.
     */
    @Test
    public void testParseUploadedLogWithoutUploading() {
        try {
            service.parseUploadedLog(null, null, null,
                    new AsyncCallback<GWTPair<GWTInvariantSet, GWTGraph>>() {
                        @SuppressWarnings("synthetic-access")
                        @Override
                        public void onFailure(Throwable caught) {
                            // Declare the test as complete.
                            finishTest();
                        }

                        @Override
                        public void onSuccess(
                                GWTPair<GWTInvariantSet, GWTGraph> ret) {
                            fail("Unexpected RPC success.");
                        }
                    });
        } catch (Exception e) {
            fail(e.toString());
        }
        delayTestFinish(testFinishDelay);
    }
}
