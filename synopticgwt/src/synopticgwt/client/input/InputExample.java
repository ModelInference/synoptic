package synopticgwt.client.input;

public class InputExample {
    public String name;
    public String logText;
    public String regExpText;
    public String partitionRegExpText;
    public String separatorRegExpText;

    public InputExample(String name, String logText, String regExpText,
            String partitionRegExpText, String separatorRegExpText) {
        this.name = name;
        this.logText = logText;
        this.regExpText = regExpText;
        this.partitionRegExpText = partitionRegExpText;
        this.separatorRegExpText = separatorRegExpText;
    }
}
