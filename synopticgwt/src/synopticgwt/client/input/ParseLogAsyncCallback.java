package synopticgwt.client.input;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;

import synopticgwt.client.SynopticGWT;
import synopticgwt.client.util.AbstractErrorReportingAsyncCallback;
import synopticgwt.client.util.ProgressWheel;
import synopticgwt.shared.GWTGraph;
import synopticgwt.shared.GWTInvariantSet;
import synopticgwt.shared.GWTPair;
import synopticgwt.shared.GWTParseException;

/**
 * Callback handler for the parseLog() Synoptic service call.
 */
final class ParseLogAsyncCallback extends
        AbstractErrorReportingAsyncCallback<GWTPair<GWTInvariantSet, GWTGraph>> {

    private final InputTab inputTab;

    /**
     * @param inputPanel
     */
    public ParseLogAsyncCallback(ProgressWheel pWheel, InputTab inputPanel) {
        super(pWheel, "parseLog call");
        this.inputTab = inputPanel;
        initialize();
    }

    @Override
    public void onFailure(Throwable caught) {
        super.onFailure(caught);

        inputTab.parseErrorMsgLabel.setText(caught.getMessage());
        inputTab.parseLogButton.setEnabled(true);
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
            for (int i = 0; i < inputTab.regExpsPanel.getWidgetCount(); i++) {
                HorizontalPanel currPanel = (HorizontalPanel) inputTab.regExpsPanel
                        .getWidget(i);
                TextBox textBox = (TextBox) currPanel.getWidget(0);

                String regexes = textBox.getText();
                int pos = indexOf(regexes, regex);
                if (pos != -1) { // TextBox containing bad regex found.
                    textBox.setFocus(true);
                    textBox.setSelectionRange(pos, regex.length());
                    textBox.setStyleName("errorHighlight");
                    break;
                }
            }
        }
        if (exception.hasLogLine()) {
            String log = exception.getLogLine();
            String logs = inputTab.logTextArea.getText();
            int pos = indexOf(logs, log);
            inputTab.logTextArea.setFocus(true);
            inputTab.logTextArea.setSelectionRange(pos, log.length());
            inputTab.logTextArea.setStyleName("errorHighlight");
        }
    }

    /**
     * Returns the index of searchString as a substring of string with the
     * condition that the searchString is followed by a newline character,
     * carriage return character, or nothing(end of string). Returns -1 if
     * searchString is not found in string with the previous conditions. Throws
     * a NullPointerException if string or searchString is null.
     */
    public int indexOf(String str, String searchString) {
        if (str == null || searchString == null) {
            throw new NullPointerException();
        }

        int movingPosition = str.indexOf(searchString);
        int cumulativePosition = movingPosition;

        if (movingPosition == -1) {
            return movingPosition;
        }

        String strCopy = str;

        while (movingPosition + searchString.length() < strCopy.length()
                && !(strCopy.charAt(movingPosition + searchString.length()) == '\r' || strCopy
                        .charAt(movingPosition + searchString.length()) == '\n')) {

            strCopy = strCopy.substring(movingPosition + searchString.length());
            movingPosition = strCopy.indexOf(searchString);

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

        inputTab.parseLogButton.setEnabled(true);
        SynopticGWT.entryPoint.logParsed(ret.getLeft(), ret.getRight());
    }

    @Override
    public void clearError() {
        super.clearError();
        // Revert any style changes that highlight parse errors.
        for (int i = 0; i < inputTab.regExpsPanel.getWidgetCount(); i++) {
            HorizontalPanel currPanel = (HorizontalPanel) inputTab.regExpsPanel
                    .getWidget(i);
            TextBox textBox = (TextBox) currPanel.getWidget(0);
            textBox.removeStyleName("errorHighlight");
        }
        inputTab.logTextArea.removeStyleName("errorHighlight");
    }

}
