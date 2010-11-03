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
		UnflaggedOption opt_logfiles = new UnflaggedOption("logfiles")
				.setStringParser(JSAP.STRING_PARSER).setRequired(true)
				.setGreedy(true);
		opt_logfiles.setHelp("The logfiles to analyze.");
		this.jsap.registerParameter(opt_logfiles);

		FlaggedOption opt_parser = new FlaggedOption("parser")
				.setStringParser(JSAP.STRING_PARSER).setRequired(true)
				.setShortFlag('p').setLongFlag("parser");
		opt_parser.setHelp("Log parser configuration regexes.");
		this.jsap.registerParameter(opt_parser);

/*TODO
		FlaggedOption opt_config = new FlaggedOption("config")
				.setStringParser(JSAP.STRING_PARSER).setRequired(false)
				.setShortFlag('c').setLongFlag("config");
		opt_configfile.setHelp("Log parser configuration file.");
*/

		FlaggedOption opt_filter = new FlaggedOption("filter")
				.setStringParser(JSAP.STRING_PARSER).setRequired(false)
				.setShortFlag('c').setLongFlag("filter");
		opt_filter.setHelp("Name of the variable to use to group events into sets.");
		this.jsap.registerParameter(opt_filter);
		
		/* verbosity switch */
		Switch sw_verbose = new Switch("verbose").setShortFlag('v')
				.setLongFlag("verbose");
		sw_verbose.setHelp("Show debugging / diagnostic information.");
		this.jsap.registerParameter(sw_verbose);

		/* line count cap */
		FlaggedOption opt_lines = new FlaggedOption("lines")
				.setStringParser(JSAP.INTEGER_PARSER).setRequired(false)
				.setLongFlag("lines").setDefault("-1");
		opt_lines.setHelp("Set the cap on lines to read from each log file.");
		this.jsap.registerParameter(opt_lines);
		
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
