import java.io.*;
import java.util.Collection;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.HashMap;

import model.IEvent;
import model.MessageEvent;
import model.interfaces.INode;
import model.interfaces.ITransition;

import daikon.Daikon;
import daikon.PptMap;
import daikon.PptTopLevel;
import daikon.PrintInvariants;
import daikon.PptTopLevel.PptType;
import daikon.inv.Invariant;
import daikon.inv.filter.InvariantFilter;
import daikon.inv.filter.InvariantFilters;

import daikon.ValueTuple;
import daikon.inv.Invariant;
import daikon.inv.InvariantStatus;
import daikon.util.Intern;

public class Daikonizer {
	
	public static <T extends INode<T>> void generateStructuralInvaraints(
			Set<T> hasPredecessor,
			Set<T> hasNoPredecessor,
			Set<T> isPredecessor,
			Set<T> isNoPredecessor,
			HashMap<String, ArrayList<T>> partitions,
			String label1,
			String label2
			) throws Exception {
		ArrayList<String> datafieldList = new ArrayList<String>();
		ArrayList<String> datatypes = new ArrayList<String>();
		getFields(hasPredecessor, datafieldList, datatypes);
		List<Invariant> inv = generateInvariants(hasPredecessor,
				datafieldList, datatypes);
		List<Invariant> invNo = generateInvariants(
				hasNoPredecessor, datafieldList, datatypes);
		List<Invariant> all = generateInvariants(partitions
				.get(label1), datafieldList, datatypes);
		if (inv != null && invNo != null) {
			ArrayList<Invariant> list = getRelevantInvariants(inv,
					invNo, all);
			List<Invariant> inv2 = generateInvariants(
					isPredecessor, datafieldList, datatypes);
			List<Invariant> invNo2 = generateInvariants(
					isNoPredecessor, datafieldList, datatypes);
			List<Invariant> all2 = generateInvariants(partitions
					.get(label2), datafieldList, datatypes);
			ArrayList<Invariant> list2 = getRelevantInvariants(
					inv2, invNo2, all2);
			if (list.size() > 0 || list2.size() > 0) {
				System.out.println("    " + label2 + list2
						+ "\nAP  " + label1 + list);

				for (Invariant i : list) {
					double r = getInvariantRelevance(i,
							(Collection) partitions.get(label1),
							datafieldList);
					System.out.println("Conf " + r + " for " + i);
				}
			}
		}
	}
	
	
	public static ArrayList<Invariant> getRelevantInvariants(
			List<Invariant> inv, List<Invariant> invNo, List<Invariant> all) {
		ArrayList<Invariant> list = new ArrayList<Invariant>();
		// System.out.println(inv);
		// System.out.println(invNo);
		for (Invariant i : inv) {
			boolean found = false;
			for (Invariant j : invNo) {
				if (i.isSameInvariant(j)) {
					found = true;
					break;
				}
			}
			for (Invariant j : all) {
				if (i.isSameInvariant(j)) {
					found = true;
					break;
				}
			}
			if (!found)
				list.add(i);
		}
		// System.out.println("->" + list);
		return list;
	}

	public static double getInvariantRelevance(Invariant inv,
			Collection<IEvent> events, List<String> argNames) {
		Invariant clone = inv.clone();
		double num = 0;
		for (IEvent e : events) {
			int size = argNames.size();
			Object[] vals = new Object[size];
			int[] mods = new int[size];
			int index = 0;
			for (String s : argNames) {
				if (e.getStringArgument(s) == null)
					vals[index] = Intern.internedLong(-1);
				else
					vals[index] = Intern.internedLong(Long.parseLong(e
							.getStringArgument(s)));
				mods[index] = 0;
				index++;
			}
			ValueTuple vt = new ValueTuple(vals, mods);
			InvariantStatus status = clone.add_sample(vt, 1);
			if (status != InvariantStatus.NO_CHANGE) {
				num++;
			}
		}
		return num / events.size();
	}
	
	public static <T extends INode<T>> List<Invariant> generateFlowInvariants(
			Collection<T> messages, String relation, String targetType,
			List<String> datafieldList, List<String> datatypes) throws Exception {
		Daikonizer d = new Daikonizer("foo", datafieldList, datatypes);
		boolean dont = false;
		for (T n : messages) {
			if (!(n instanceof IEvent)) {
				dont = true;
				break;
			}
			IEvent e = (IEvent) n;
			List<String> beginVals = new ArrayList<String>();
			for (String argName : datafieldList) {
				String s = e.getStringArgument(argName);
				if (s == null || s.length() == 0)
					s = "-1";
				beginVals.add(s);
			}
			IEvent e2 = null;
			for (ITransition<T> t : n.getTransitionsIterator(relation)) {
				if (t.getTarget().getLabel().equals(targetType)) {
					e2 = (IEvent) t.getTarget();
					break;
				}
			}
			List<String> endVals = new ArrayList<String>();
			if (e2 != null) {
				for (String argName : datafieldList) {
					String s = e2.getStringArgument(argName);
					if (s == null || s.length() == 0)
						s = "-1";
					endVals.add(s);
				}
			}
			d.addValues((List) beginVals, (List) endVals);
		}
		if (!dont) {
			List<Invariant> enter = new ArrayList<Invariant>();
			List<Invariant> exit = new ArrayList<Invariant>();
			List<Invariant> flow = new ArrayList<Invariant>();
			d.genDaikonInvariants(enter, exit, flow, false);
			return flow;
		}
		return null;
	}


	public static <T extends INode<T>> List<Invariant> generateFlowInvariants(
			Collection<T> messages, String relation, String targetType) throws Exception {
		ArrayList<String> datafieldList = new ArrayList<String>();
		ArrayList<String> datatypes = new ArrayList<String>();
		getFields(messages, datafieldList, datatypes);
		return generateFlowInvariants(messages, relation, targetType,
				datafieldList, datatypes);
	}
	
	private static <T> void getFields(Collection<T> hasPredecessor,
			ArrayList<String> datafieldList, ArrayList<String> datatypes) {
		Set<String> datafields = new LinkedHashSet<String>();
		for (T n : hasPredecessor) {
			if (!(n instanceof IEvent))
				break;
			IEvent e = (IEvent) n;
			datafields.addAll(e.getStringArguments());
		}
		datafieldList.addAll(datafields);
		for (String s : datafieldList)
			// s intentionally not used
			datatypes.add("int");
	}
	
	public static List<Invariant> generateInvariants(Set<MessageEvent> messages) throws Exception {
		ArrayList<String> datafieldList = new ArrayList<String>();
		ArrayList<String> datatypes = new ArrayList<String>();
		getFields(messages, datafieldList, datatypes);
		return generateInvariants(messages, datafieldList, datatypes);
	}
	
	public static <T> List<Invariant> generateInvariants(
			Collection<T> hasPredecessor, List<String> datafieldList,
			List<String> datatypes) throws Exception {
		Daikonizer d = new Daikonizer("foo", datafieldList, datatypes);
		boolean dont = false;
		for (T n : hasPredecessor) {
			if (!(n instanceof IEvent)) {
				dont = true;
				break;
			}
			IEvent e = (IEvent) n;
			List<String> vals = new ArrayList<String>();
			for (String argName : datafieldList) {
				String s = e.getStringArgument(argName);
				if (s == null || s.length() == 0)
					s = "-1";
				vals.add(s);
			}
			d.addValues((List) vals, (List) vals);
		}
		if (!dont) {
			List<Invariant> enter = new ArrayList<Invariant>();
			List<Invariant> exit = new ArrayList<Invariant>();
			List<Invariant> flow = new ArrayList<Invariant>();
			d.genDaikonInvariants(enter, exit, flow, false);
			return enter;
		}
		return null;
	}
	
	
	
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

	public void genDaikonInvariants(
			List<Invariant> enter,
			List<Invariant> exit,
			List<Invariant> flow,
			boolean norestict) throws Exception {
		String fname = "./daikonizer_" + System.currentTimeMillis()
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
