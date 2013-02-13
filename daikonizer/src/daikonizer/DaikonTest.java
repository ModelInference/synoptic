package daikonizer;

import java.util.Vector;

import junit.framework.TestCase;
import daikon.inv.Invariant;

public class DaikonTest extends TestCase {
	public void testDaikonOutput() throws Exception {
		Vector<String> varNames = new Vector<String>();
		varNames.add("x");
		varNames.add("y");

		Vector<DaikonVarType> varTypes = new Vector<DaikonVarType>();
		varTypes.add(DaikonVarType.INT);
		varTypes.add(DaikonVarType.INT);

		Daikonizer d = new Daikonizer("transition", varNames, varTypes);

		for (int i = 1; i < 100; i++) {
			Vector<Object> vals1 = new Vector<Object>();
			Vector<Object> vals2 = new Vector<Object>();

			vals1.add(i);
			vals1.add(i + 1);

			vals2.add(i);
			vals2.add(i + 2);

			d.addValues(vals1, vals2);
		}

		String dtraceString = d.toDtraceString();

		//System.out.println(dtraceString);

		Vector<Invariant> enterInvs = new Vector<Invariant>();
		Vector<Invariant> exitInvs = new Vector<Invariant>();
		Vector<Invariant> flow = new Vector<Invariant>();
		d.genDaikonInvariants(enterInvs, exitInvs, flow, false);

		System.out.println("Enter invariants:");
		for (int i = 0; i < enterInvs.size(); i++) {
			System.out.println("\t" + enterInvs.elementAt(i));
		}

		System.out.println("\nExit invariants:");
		for (int i = 0; i < enterInvs.size(); i++) {
			System.out.println("\t" + exitInvs.elementAt(i));
		}

	}
}