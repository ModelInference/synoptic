package daikonizer;

import java.util.List;

/**
 * Represents a program point, which has a name and a list of variables.
 *
 */
public class ProgramPoint {
	public String pptName;
	public List<DaikonVar> vars;

	public ProgramPoint(String pptName, List<DaikonVar> vars) {
		this.pptName = pptName;
		this.vars = vars;
	}

	public boolean containsVar(DaikonVar var) {
		return vars.contains(var);
	}

	private String varsDeclString() {
		String ret = "";
		int comparability =0;
		for (DaikonVar var : vars) {
			ret += " variable " + var.vname + "\n";
			ret += "  var-kind variable\n";
			ret += "  dec-type " + var.vtype + "\n";
			ret += "  rep-type " + var.vtype + "\n";
			//ret += "  comparability " + comparability++ + "\n";
		}
		return ret;
	}

	private String exitString() {
		String ret = "ppt " + pptName + ":::EXIT1\n"
				+ "ppt-type subexit\n";
		return ret + this.varsDeclString();
	}

	private String enterString() {
		String ret = "ppt " + pptName + ":::ENTER\n"
				+ "ppt-type enter\n";
		return ret + this.varsDeclString();
	}

	public String toString() {
		return this.enterString() + "\n\n" + this.exitString() + "\n";
	}

	public String getExitName() {
		return pptName + ":::EXIT1";
	}

	public String getEnterName() {
		return pptName + ":::ENTER";
	}

}
