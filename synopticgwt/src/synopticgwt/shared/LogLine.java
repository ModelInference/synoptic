package synopticgwt.shared;

import java.io.Serializable;

public class LogLine implements Serializable {
	
    private static final long serialVersionUID = 1L;
	
    public int lineNum;
	public String line;
	public String filename;
	
	public LogLine() {}
	
	public LogLine (int lineNum, String line, String filename) {
		this.lineNum = lineNum;
		this.line = line;
		this.filename = filename;
	}
	
	public String toString() {
		return lineNum + "\t" + line + "\t" + filename;
	}
}
