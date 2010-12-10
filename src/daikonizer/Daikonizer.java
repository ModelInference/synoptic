package daikonizer;

import java.io.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import daikon.Daikon;
import daikon.PptMap;
import daikon.PptTopLevel;
import daikon.PrintInvariants;
import daikon.PptTopLevel.PptType;
import daikon.inv.Invariant;
import daikon.inv.filter.InvariantFilter;
import daikon.inv.filter.InvariantFilters;

public class Daikonizer {
	public ProgramPoint ppt;
	public DaikonTrace trace;

	public Daikonizer(String pptName, List<String> varNames,
			List<String> varTypes) {
		Vector<DaikonVar> vars = new Vector<DaikonVar>(varNames.size());

		assert (varNames.size() == varTypes.size());

		for (int i = 0; i < varNames.size(); i++) {
			DaikonVar v = new DaikonVar(varNames.get(i), varTypes.get(i));
			vars.addElement(v);
		}

		this.ppt = new ProgramPoint(pptName, vars);
		this.trace = new DaikonTrace(this.ppt);
	}

	public void addValues(List<Object> enter_vals, List<Object> exit_vals) {
		trace.addInstance(enter_vals, exit_vals);
	}

	public String toDtraceString() {
		String ret = "decl-version 2.0\n" + "var-comparability implicit\n\n";
		ret += trace.toString();
		return ret;
	}

	public void checkInvariants() {

	}

	public void genDaikonInvariants(List<Invariant> enter, List<Invariant> exit, List<Invariant> flow, boolean norestict) throws Exception {
		String fname = "daikon-tmp/daikonizer_" + System.currentTimeMillis()
				+ ".dtrace";
		// System.out.println("using fname: " + fname);

		// write dtrace file for daikon to use as input
		this.writeDtraceFile(fname);

		// formulate args for daikon
		String[] daikonArgs = new String[1];
		daikonArgs[0] = fname;
		// daikonArgs[1] = "--suppress_redundant";

		// preserve old stdout/stderr streams in case they might be useful
		PrintStream oldStdout = System.out;

		// new output stream to use for stdout while Daikon is running
		ByteArrayOutputStream daikonOutputStream = new ByteArrayOutputStream();

		// redirect system.out to our custom print stream
		System.setOut(new PrintStream(daikonOutputStream));

		// execute daikon
		daikon.Daikon.mainHelper(daikonArgs);

	

		for (PptTopLevel ppt : daikon.Daikon.all_ppts.all_ppts()) {
			// System.out.println("PPT-Name: " + ppt.name());
			// System.out.println("#Samples: " + ppt.num_samples());
			for (Invariant inv : ppt.getInvariants()) {
				if (ppt.type == PptType.ENTER && (norestict || inv.enoughSamples()) /*&& inv.isObvious() == null*/)
					enter.add(inv);
				else if (ppt.type == PptType.EXIT && (norestict || inv.enoughSamples()) /*&& inv.isObvious() == null*/)
					exit.add(inv);
				 else if (ppt.type == PptType.SUBEXIT && (norestict || (inv.enoughSamples() && inv.isObvious() == null)))
					flow.add(inv);
			}
		}	
		
		// reset output to previous stream
		System.setOut(oldStdout);
		enter = filter_invs(enter);
		exit = filter_invs(exit);
		flow = filter_invs(flow);
	}

	private List<Invariant> filter_invs(List<Invariant> invs) throws Exception {
		invs = PrintInvariants.sort_invariant_list(invs);
		
		//XXX
		throw new Exception("filter_invs cannot be accessed within Daikon.jar");
		// TODO: fix this for daikon integration
		// List<Invariant> filtered = Daikon.filter_invs(invs);

//		InvariantFilters fi = InvariantFilters.	defaultFilters();
//		
//		List<Invariant> filterFI = new ArrayList<Invariant>();
//		
//		for (Invariant i : filtered) {
//			if (fi.shouldKeep(i) == null)
//				filterFI.add(i);				
//		}
//		invs = filterFI;
//		invs
//	      = InvariantFilters.addEqualityInvariants(invs);
//		return invs;
	}

	private boolean writeDtraceFile(String filename) {
		FileOutputStream fout;
		PrintStream ps;

		try {
			fout = new FileOutputStream(filename);
		} catch (IOException e) {
			System.err.println("unable to open file " + filename
					+ " for writing");
			return false;
		}

		ps = new PrintStream(fout);
		ps.print(this.toDtraceString());

		// close the file
		try {
			fout.close();

		} catch (IOException e) {
			System.err.println("unable to close file " + filename);
			return false;
		}
		return true;
	}

}
