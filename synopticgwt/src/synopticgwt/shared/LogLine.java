package synopticgwt.shared;

import java.io.Serializable;

public class LogLine implements Serializable {
	
    private static final long serialVersionUID = 1L;
	
    private int lineNum;
	private String line;
	private String filename;
	
	public LogLine() {}
	
	public LogLine (int lineNum, String line, String filename) {
		this.lineNum = lineNum;
		this.line = line;
		this.filename = filename;
	}
	
	public int getLineNum() {
		return lineNum;
	}
	
	public String getLine() {
		return line;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public String toString() {
		return lineNum + "\t" + line + "\t" + filename;
	}
}
