package daikonizer;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;

// a program point has a name and a set of variables
public class ProgramPoint {
	public String ProgramPointName;
	public Vector vars;

	public ProgramPoint(String ProgramPointName, Vector vars) {
		this.ProgramPointName = ProgramPointName;
		this.vars = vars;
	}

	public boolean containsVar(DaikonVar var) {
		return vars.contains(var);
	}

	private String varsDeclString() {
		Enumeration e;
		String ret = "";
		int comparability =0;
		for (e = this.vars.elements(); e.hasMoreElements();) {
			DaikonVar var = (DaikonVar) e.nextElement();
			ret += " variable " + var.vname + "\n";
			ret += "  var-kind variable\n";
			ret += "  dec-type " + var.vtype + "\n";
			ret += "  rep-type " + var.vtype + "\n";
			ret += "  comparability " + comparability++ + "\n";
		}
		return ret;
	}

	private String exitString() {
		String ret = "ppt " + ProgramPointName + ":::EXIT1\n"
				+ "ppt-type subexit\n";
		return ret + this.varsDeclString();
	}

	private String enterString() {
		String ret = "ppt " + ProgramPointName + ":::ENTER\n"
				+ "ppt-type enter\n";
		return ret + this.varsDeclString();
	}

	public String toString() {
		return this.enterString() + "\n\n" + this.exitString() + "\n";
	}

	public String getExitName() {
		return ProgramPointName + ":::EXIT1";
	}

	public String getEnterName() {
		return ProgramPointName + ":::ENTER";
	}

}
