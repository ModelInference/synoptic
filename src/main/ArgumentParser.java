package main;

import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.UnflaggedOption;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.Switch;

public class ArgumentParser {

	/**
	 * The nodes of the graph. The edges are managed by the nodes.
	 */
	private JSAP jsap = new JSAP();
	private String[] args;
	
	public JSAPResult config;
	
	public ArgumentParser(String[] args) {
		this.args = args;
	}

	public boolean Parse() throws JSAPException {
		// print parsed arguments
		for (int i = 0; i < this.args.length; i++)
			System.out.println(this.args[i]);

		/* logfile string parameter */
		FlaggedOption opt_logfile = new FlaggedOption("logfile")
				.setStringParser(JSAP.STRING_PARSER).setRequired(true)
				.setShortFlag('f').setLongFlag("logfile");
		opt_logfile.setHelp("The logfile to be processed by Synoptic");
		this.jsap.registerParameter(opt_logfile);

		/* print invariants switch */
		Switch sw_print_invs = new Switch("print_invariants").setLongFlag("p")
				.setLongFlag("print_invariants");
		sw_print_invs.setHelp("Output the list of mined invariants.");
		this.jsap.registerParameter(sw_print_invs);

		/*
		 * un-flagged option that grabs the regular expression to use on the
		 * logfile
		 */
		UnflaggedOption opt_reg_exp = new UnflaggedOption("reg_exp")
				.setStringParser(JSAP.STRING_PARSER).setRequired(true)
				.setGreedy(true);
		opt_reg_exp.setHelp("Regular expression used to parse the logfile.");
		this.jsap.registerParameter(opt_reg_exp);

		/*
		 * parse the arguments
		 */
		this.config = this.jsap.parse(this.args);

		return this.config.success();
	}
	
	public void PrintParseErrors() {
		System.err.println();
		/*
		 * print out specific error messages describing the problems with the
		 * command line
		 */
		for (java.util.Iterator errs = this.config.getErrorMessageIterator(); errs
				.hasNext();) {
			System.err.println("Error: " + errs.next());
		}
		// print usage
		System.err.println("Usage: java " + Main.class.getName());
		System.err.println("                " + this.jsap.getUsage());
		System.err.println();
		// show full help
		System.err.println(this.jsap.getHelp());
	}

}
