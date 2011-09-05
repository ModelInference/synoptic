package synopticgwt.client.input;

public class InputExample {
    public String name;
    public String logText;
    public String regExpText;
    public String partitionRegExpText;

    public InputExample(String name, String logText, String regExpText,
            String partitionRegExpText) {
        this.name = name;
        this.logText = logText;
        this.regExpText = regExpText;
        this.partitionRegExpText = partitionRegExpText;
    }
}
