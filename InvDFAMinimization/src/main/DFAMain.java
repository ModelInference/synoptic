package main;

import synoptic.main.Main;

/**
 * Initial functionality for InvDFAMinimization: accepts a log file and regular
 * expression arguments and uses Synoptic to parse the log and mine invariants.
 * Mined invariants are printed to the console.
 * 
 * @author Jenny
 */
public class DFAMain {
    public static void main(String[] args) throws Exception {
        Main synMain = Main.processArgs(args);
        synMain.createInitialPartitionGraph();
    }
}
