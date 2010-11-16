package main;

import java.util.concurrent.Callable;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;
import java.lang.Integer;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Iterator;

import plume.Option;
import plume.Options;
import plume.OptionGroup;

import model.*;
import model.export.GraphVizExporter;
import model.input.GraphBuilder;


public class Main implements Callable<Integer> {
	// public class Main {
    /**
     * The current Synoptic version.
     */
    public static final String versionString = "Synoptic version 0.0.2";


    @OptionGroup("General Options")
    ////////////////////////////////////////////////////
    /**
     * Print the short usage message.  This does not include verbosity
     * or debugging options.
     */
    @Option(value="-h Print short usage message", aliases={"-help"})
    public static boolean help = false;
        
    /**
     * Print the extended usage message.  This includes verbosity and
     * debugging options but not internal options.
     */
    @Option("-H Print extended usage message (includes debugging options)")
    public static boolean allHelp = false;
    
    /**
     * Print the current Synoptic version.
     */
    @Option (value="-v Print program version", aliases={"-version"})
    public static boolean version = false;
    // end option group "General Options"


    @OptionGroup("Execution Options")
    ////////////////////////////////////////////////////
    /**
     * Be quiet, do not print much information.
     */
    @Option (value="-Q Be quiet, do not print much information", aliases={"-quiet"})
    public static boolean reallyQuiet = false;

    
    @OptionGroup("Parser Options")
    ////////////////////////////////////////////////////
    /**
     * Regular expression separator string for determining mining granularity 
     */
    @Option (value="-s Separator regular expression", aliases={"-separator"})
    public static String separator = null;

    /**
     * List of regular expression strings for parsing lines in the input files.
     */
    @Option (value="-r Parser regular expressions", aliases={"-regexps"})
    public static String regExps = null;

    /**
     * A regular expression to partition the lines of the input files.
     */
    @Option (value="-p Partition regular expression", aliases={"-partition"})
    public static String partitionRegExp = null;
    // end option group "Parser Options"
    
    
    @OptionGroup ("Input Options")
    ////////////////////////////////////////////////////
    /**
     * Input log files to run Synoptic on. 
     */
    @Option(value="-f <log-filenames> input log filenames", aliases={"-logfiles"})
    public static String logFilenames = null;
    // end option group "Input Options"

    
    @OptionGroup("Output Options")
    ////////////////////////////////////////////////////
    /**
     * Output filename which will contain dot output for the final
     * Synoptic representation
     */
    @Option(value="-o Output filename for dot output", aliases={"--output"})
    public static String outputFilename = null;
    // end option group "Output Options"


    /** 
     * NOTE: this group of options is 'unpublicized', which means that
     * it will not appear in the default usage message
     */
    @OptionGroup (value="Verbosity Options", unpublicized=true)
    ////////////////////////////////////////////////////
    /**
     * Dump the complete list of mined invariants for the set of input files
     * to stdout
     */
    @Option("Dump complete list of mined invariant to stdout")
    public static boolean dumpInvariants = false;
    
    /**
     * Dump the dot representations for intermediate Synoptic steps to
     * file. Each of these files will have a name like:
     * <outputFilename>.<S>.<N> where 'outputFilename' is the filename
     * of the final Synoptic output, 'S' is the stage (either 'r' for
     * refinement, or 'c' for coarsening), and 'N' is the step number
     * within the stage (starting from 1 for each stage).
     */
    @Option("Dump dot files from intermediate Synoptic stages to file")
    public static boolean dumpIntermediateStages = false;
    // end option group "Verbosity Options"


    /** 
     * NOTE: this group of options is 'unpublicized', which means that
     * it will not appear in the default usage message
     */
    @OptionGroup (value="Debugging Options", unpublicized=true)
    ////////////////////////////////////////////////////
    /**
     * Do not perform the coarsening stage in Synoptic, and as final
     * output use the most refined representation
     */
    @Option("Do not perform the coarsening stage")
    public static boolean noCoarsening = false;

    /**
     * Do not perform the refinement (and therefore do not perform
     * coarsening) and do not produce any representation as
     * output. This is useful for just printing the list of mined
     * invariants (using the option 'dumpInvariants' above).
     */
    @Option("Do not perform refinement")
    public static boolean noRefinement = false;
    // end option group "Debugging Options"

    
    /** One line synopsis of usage */
    private static String usage_string
        = "synoptic [options] <logfiles-to-analyze>";

   /**
    * The main method to perform the inference algorithm.  See user
    * documentation an explanation of the options.
    *
    * @param args - command-line options
    */        
    public static void main(String[] args) throws Exception {
        // this directly sets the static member options of the Main class
        Options options = new Options (usage_string, Main.class);
        // TODO: currently not using commandlineArgs for anything..
        String[] commandLineArgs = options.parse_or_usage(args);

        // Display help for all option groups, including 'unpublicized' ones
        if (allHelp) {
            System.out.println(
                options.usage("General Options",
                              "Execution Options",
                              "Parser Options",
                              "Input Options",
                              "Output Options",
                              "Verbosity Options",
                              "Debugging Options"));
            return;
        }

        // Display help just for the 'publicized' option groups
        if (help) {
            options.print_usage();
            return;
        }

        if (version) {
            System.out.println(Main.versionString);
            return;
        }

        Main mainInstance = new Main();
        Integer ret = mainInstance.call();
		System.out.println("Main.call() returned " + ret.toString());
		System.exit(ret); 
    }

    /***********************************************************/
    
    public Main() {
    	// TODO: can set up graphical state here
    }
    
    /**
     * Prints out a message unless the reallyQuiet option is set 
     * @param msg string to print
     */
    public void VerbosePrint(String msg) {
    	if (! Main.reallyQuiet) {
    		System.out.println(msg);
    	}
    }
    
    
    /**
     * Tokenizes a string of doubled quoted strings delimited with commas.
     * @param str Input string to tokenize
     * @return an array of tokens, without the quotes
     */
    public ArrayList<String> TokenizeStringOfQuotes(String str) {
    	String regex = "\"(\\([^)]*\\)|[^\"])*\"";
    	Pattern p = Pattern.compile(regex);
    	Matcher m = p.matcher(str);
    	ArrayList<String> tokens = new ArrayList<String>();
    	while(m.find()) {
    		String exp = str.substring(m.start() + 1, m.end() - 1);
    		tokens.add(exp);
    	}
    	return tokens;
    }

    	
	@Override
	public Integer call() throws Exception {
		// TODO: is there a way to print all the set Options?
		String debug_msg = 
				"logfiles: " + Main.logFilenames + 
				"\nseparator: " + Main.separator +
				"\nregExps: " + Main.regExps +
				"\npartitionRegExp: " + Main.partitionRegExp; 
		VerbosePrint(debug_msg);

		TraceParser parser = new TraceParser();
		parser.LOG = Logger.getLogger("Parser Logger");
        
		VerbosePrint("Setting up the log file parser.");
        
        if (Main.separator != null) {
            parser.addSeperator(Main.separator);
        }
        
        if (Main.regExps != null) {
        	// The regExps string is assumed to be comma delimited
        	// with each regular expression enclosed in double quotes.

        	// TODO: this is too verbose, and also repeats below.
        	//       Could this be made more concise?
        	ArrayList<String> exps = TokenizeStringOfQuotes(Main.regExps);
        	Iterator<String> expsItr = exps.iterator();
        	String exp = null;
        	while(expsItr.hasNext()) {
        		exp = expsItr.next();
        		VerbosePrint("\taddRegex with exp:" + exp);
        		parser.addRegex(exp);
        	}
        }
        
        if (Main.partitionRegExp != null) {
            parser.setPartitioner(Main.partitionRegExp);
        }
        
        // Parse all the log filenames, constructing the parsedEvents List
        List<TraceParser.Occurrence> parsedEvents = null;
        if (Main.logFilenames != null) {
        	VerbosePrint("Parsing input files..");
        	// The logFilenames string is assumed to be comma delimited
        	// with each filename enclosed in double quotes.
        	ArrayList<String> filenames = TokenizeStringOfQuotes(Main.logFilenames);
        	Iterator<String> filenamesItr = filenames.iterator();
        	String filename = null;
        	parsedEvents = new ArrayList<TraceParser.Occurrence>();
        	while(filenamesItr.hasNext()) {
        		filename = filenamesItr.next();
        		VerbosePrint("\tcalling parseTraceFile with filename:" + filename);
        		parsedEvents.addAll(parser.parseTraceFile(filename, -1));
        	}
        }
        
        // If we parses any events then run Synoptic
        if (parsedEvents != null) {
        	VerbosePrint("Running Synoptic..");
            parser.builder = new GraphBuilder();
        	parser.generateDirectTemporalRelation(parsedEvents, true);
            model.Graph<MessageEvent> synopticGraph = ((GraphBuilder) parser.builder).getRawGraph();
            
            // If we were given an output filename then export the resulting graph into this filename 
            if (Main.outputFilename != null) {
            	VerbosePrint("Exporting final graph..");
            	GraphVizExporter exporter = new GraphVizExporter();
            	exporter.exportAsDotAndPngFast(Main.outputFilename, synopticGraph);
            }
        }
		return new Integer(0);
	}
}

