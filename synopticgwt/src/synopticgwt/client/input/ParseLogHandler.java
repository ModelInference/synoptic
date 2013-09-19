package synopticgwt.client.input;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import synopticgwt.client.SynopticGWT;
import synopticgwt.shared.GWTSynOpts;

/**
 * Handles parse log button clicks.
 */
class ParseLogHandler implements ClickHandler {
    /**
     * The input tab this handler is associated with.
     */
    private final InputTab inputTab;

    /**
     * @param inputTab
     */
    ParseLogHandler(InputTab inputTab) {
        this.inputTab = inputTab;
    }

    @Override
    public void onClick(ClickEvent event) {
        // Disallow the user from making concurrent Parse Log calls.
        this.inputTab.parseLogButton.setEnabled(false);

        SynopticGWT.entryPoint.parsingLog();

        // Reset the parse error msg.
        this.inputTab.parseErrorMsgLabel.setText("");

        if (this.inputTab.logFileRadioButton.getValue()) {
            // Log file.
            this.inputTab.logFileUploadForm.submit();

        } else {
            // Log in text area.
            // Extract arguments for parseLog call.
            String logLines = this.inputTab.logTextArea.getText();
            List<String> regExps = this.inputTab.extractAllRegExps();
            String partitionRegExp = this.inputTab
                    .getTextBoxRegExp(this.inputTab.partitionRegExpTextBox);
            String separatorRegExp = this.inputTab
                    .getTextBoxRegExp(this.inputTab.separatorRegExpTextBox);

            // TODO: validate the arguments to parseLog.
            SynopticGWT.entryPoint.manualRefineCoarsen = this.inputTab.manualRefineCoarsen
                    .getValue();

            GWTSynOpts synOpts = new GWTSynOpts(logLines, regExps,
                    partitionRegExp, separatorRegExp,
                    this.inputTab.ignoreNonMatchedLines.getValue(),
                    this.inputTab.manualRefineCoarsen.getValue(),
                    this.inputTab.onlyMineInvs.getValue());
            // ////////////////////// Call to remote service.
            this.inputTab.getService().parseLog(
                    synOpts,
                    new ParseLogAsyncCallback(this.inputTab.getProgressWheel(),
                            this.inputTab));
            // //////////////////////
        }
    }
}