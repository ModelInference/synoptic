package synopticgwt.client.input;

import com.google.gwt.user.client.ui.TextBox;

import synopticgwt.client.SynopticGWT;
import synopticgwt.client.util.ErrorReportingAsyncCallback;
import synopticgwt.client.util.ProgressWheel;
import synopticgwt.shared.GWTGraph;
import synopticgwt.shared.GWTInvariantSet;
import synopticgwt.shared.GWTPair;
import synopticgwt.shared.GWTParseException;

/**
 * Callback handler for the parseLog() Synoptic service call.
 */
class ParseLogAsyncCallback extends
        ErrorReportingAsyncCallback<GWTPair<GWTInvariantSet, GWTGraph>> {

    private final InputPanel inputPanel;

    /**
     * @param inputPanel
     */
    public ParseLogAsyncCallback(ProgressWheel pWheel, InputPanel inputPanel) {
        super(pWheel, "parseLog call");
        this.inputPanel = inputPanel;
    }

    @Override
    public void onFailure(Throwable caught) {
        super.onFailure(caught);

        inputPanel.parseErrorMsgLabel.setText(caught.getMessage());
        inputPanel.parseLogButton.setEnabled(true);
        if (!(caught instanceof GWTParseException)) {
            return;
        }
        GWTParseException exception = (GWTParseException) caught;
        // If the exception has both a regex and a logline, then only
        // the TextArea
        // that sets their highlighting last will have highlighting.
        // A secret dependency for TextArea highlighting is focus.
        // As of now, 9/12/11, SerializableParseExceptions do not get
        // thrown with both a regex and a logline.
        if (exception.hasRegex()) {
            String regex = exception.getRegex();
            // TODO: currently error handling only for first reg exps
            // text box, extend to all extra reg exp text box also.
            // Noted in Issue152
            String regexes = ((TextBox) inputPanel.regExpsPanel.getWidget(0))
                    .getText();
            int pos = indexOf(regexes, regex);
            ((TextBox) inputPanel.regExpsPanel.getWidget(0)).setFocus(true);
            ((TextBox) inputPanel.regExpsPanel.getWidget(0)).setSelectionRange(
                    pos, regex.length());
        }
        if (exception.hasLogLine()) {
            String log = exception.getLogLine();
            String logs = inputPanel.logTextArea.getText();
            int pos = indexOf(logs, log);
            inputPanel.logTextArea.setFocus(true);
            inputPanel.logTextArea.setSelectionRange(pos, log.length());

        }
    }

    /**
     * Returns the index of searchString as a substring of string with the
     * condition that the searchString is followed by a newline character,
     * carriage return character, or nothing(end of string). Returns -1 if
     * searchString is not found in string with the previous conditions. Throws
     * a NullPointerException if string or searchString is null.
     */
    public int indexOf(String string, String searchString) {
        if (string == null || searchString == null) {
            throw new NullPointerException();
        }

        int movingPosition = string.indexOf(searchString);
        int cumulativePosition = movingPosition;

        if (movingPosition == -1) {
            return movingPosition;
        }

        while (movingPosition + searchString.length() < string.length()
                && !(string.charAt(movingPosition + searchString.length()) == '\r' || string
                        .charAt(movingPosition + searchString.length()) == '\n')) {

            string = string.substring(movingPosition + searchString.length());
            movingPosition = string.indexOf(searchString);

            if (movingPosition == -1) {
                return movingPosition;
            }

            cumulativePosition += movingPosition + searchString.length();
        }
        return cumulativePosition;
    }

    @Override
    public void onSuccess(GWTPair<GWTInvariantSet, GWTGraph> ret) {
        super.onSuccess(ret);

        inputPanel.parseLogButton.setEnabled(true);
        SynopticGWT.entryPoint.logParsed(ret.getLeft(), ret.getRight());
    }
}