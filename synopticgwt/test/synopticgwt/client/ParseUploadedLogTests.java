package synopticgwt.client;

import org.junit.Test;

import com.google.gwt.user.client.rpc.AsyncCallback;

import synopticgwt.shared.GWTGraph;
import synopticgwt.shared.GWTInvariantSet;
import synopticgwt.shared.GWTPair;
import synopticgwt.shared.GWTSynOpts;

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
        GWTSynOpts synOpts = new GWTSynOpts(null, null, null, null, false,
                false, false);
        try {
            service.parseUploadedLog(synOpts,
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
