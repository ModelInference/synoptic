package synopticgwt.client;

import org.junit.Test;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Tests the various export RPCs.
 */
public class ExportTests extends SynopticGWTTestCase {
    /**
     * A test to export the dot file without an uploaded log.
     */
    @Test
    public void testExportDotWithoutParsing() {
        try {
            service.exportDot(new AsyncCallback<String>() {
                @SuppressWarnings("synthetic-access")
                @Override
                public void onFailure(Throwable caught) {
                    // Declare the test as complete.
                    finishTest();
                }

                @Override
                public void onSuccess(String fname) {
                    fail("Unexpected RPC success.");
                }
            });
        } catch (Exception e) {
            fail(e.toString());
        }
        delayTestFinish(testFinishDelay);
    }

    /**
     * A test to export the dot file without an uploaded log.
     */
    @Test
    public void testExportPngWithoutParsing() {
        try {
            service.exportPng(new AsyncCallback<String>() {
                @SuppressWarnings("synthetic-access")
                @Override
                public void onFailure(Throwable caught) {
                    // Declare the test as complete.
                    finishTest();
                }

                @Override
                public void onSuccess(String fname) {
                    fail("Unexpected RPC success.");
                }
            });
        } catch (Exception e) {
            fail(e.toString());
        }
        delayTestFinish(testFinishDelay);
    }
}
