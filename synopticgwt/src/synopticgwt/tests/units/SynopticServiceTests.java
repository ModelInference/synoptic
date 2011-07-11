package synopticgwt.tests.units;

import java.util.LinkedList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.rpc.AsyncCallback;

import synoptic.main.ParseException;
import synopticgwt.client.ISynopticService;
import synopticgwt.client.ISynopticServiceAsync;
import synopticgwt.shared.GWTGraph;
import synopticgwt.shared.GWTInvariants;
import synopticgwt.shared.GWTPair;

public class SynopticServiceTests extends GWTTestCase { // extends
                                                        // SynopticGWTTestCase {
    ISynopticServiceAsync service = null;

    @Override
    public void gwtSetUp() {
        // super.gwtSetUp();

        /**
         * Create an RPC proxy to talk to the Synoptic service
         */

    }

    @Override
    public String getModuleName() {
        return null;
    }

    /**
     * onSuccess\onFailure callback handler for parseLog()
     */
    class ParseLogAsyncCallback implements
            AsyncCallback<GWTPair<GWTInvariants, GWTGraph>> {
        @Override
        public void onFailure(Throwable caught) {

        }

        @Override
        public void onSuccess(GWTPair<GWTInvariants, GWTGraph> result) {

        }
    }

    /**
     * A test to parse a simple log without any exceptions.
     * 
     * @throws ParseException
     */
    public void testParseLog() throws ParseException {
        service = GWT.create(ISynopticService.class);
        String logLines = "hello-world";
        LinkedList<String> regExps = new LinkedList<String>();
        regExps.add("(?<TYPE>)");
        String partitionRegExp = "";
        String separatorRegExp = "";
        service.parseLog(logLines, regExps, partitionRegExp, separatorRegExp,
                new ParseLogAsyncCallback());
    }
}
