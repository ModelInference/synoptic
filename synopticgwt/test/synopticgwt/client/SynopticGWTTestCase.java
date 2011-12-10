package synopticgwt.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.junit.client.GWTTestCase;

/**
 * Base class for Synoptic tests that build on GWTTestCase.
 */
public abstract class SynopticGWTTestCase extends GWTTestCase {
    // Instance of the Synoptic service to test.
    ISynopticServiceAsync service = null;

    // Delay test termination by 500ms.
    static final int testFinishDelay = 500;

    @Override
    public String getModuleName() {
        return "synopticgwt.SynopticGWT";
    }

    @Override
    public void gwtSetUp() throws Exception {
        super.gwtSetUp();
        // Create a new instance of the service, for each test case instance.
        service = GWT.create(ISynopticService.class);
    }
}
