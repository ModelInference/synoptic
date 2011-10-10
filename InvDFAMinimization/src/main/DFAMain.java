package main;

import synoptic.main.Main;

/*
Tested in Eclipse with arguments:
./../traces/PetersonLeaderElection/5processes_trace/combined.txt
--dumpInvariants=true
--onlyMineInvariants=true
-r ^(?:#.*|\\s*|.*round-done.*)(?<HIDE=>true)$
-r (?<nodename>)(?<VTIME>)(?<TYPE>)(?:(?<mtype>)(?:(?<roundId>)(?:(?<payload>)(?:(?<id>))?)?)?)?
-m \\k<FILE>\\k<nodename>
 */

/**
 * Initial functionality for InvDFAMinimization: accepts a log file and regular expression arguments
 * and uses Synoptic to parse the log and mine invariants. Mined invariants are printed to the console.
 *
 * @author Jenny
 */
public class DFAMain {
	public static void main(String[] args) throws Exception {
		Main synMain = Main.processArgs(args);
		synMain.createInitialPartitionGraph();
	}
}
