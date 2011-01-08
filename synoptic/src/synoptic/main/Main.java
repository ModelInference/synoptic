package synoptic.main;

import java.lang.Integer;
import java.net.URISyntaxException;
import java.util.concurrent.Callable;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.Logger;
import java.util.logging.Handler;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.io.FilenameUtils;

import org.junit.runner.JUnitCore;

import plume.Option;
import plume.Options;
import plume.OptionGroup;
import synoptic.algorithms.bisim.Bisimulation;
import synoptic.model.MessageEvent;
import synoptic.model.PartitionGraph;
import synoptic.model.export.GraphVizExporter;
import synoptic.model.input.GraphBuilder;



public class Main implements Callable<Integer> {
	public static Logger logger = null;

    /**
     * The current Synoptic version.
     */
    public static final String versionString = "Synoptic version 0.0.2";


    ////////////////////////////////////////////////////
    /**
     * Print the short usage message.  This does not include verbosity
     * or debugging options.
     */
    @OptionGroup("General Options")
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

    
    ////////////////////////////////////////////////////
    /**
     * Be quiet, do not print much information. Sets the log level to WARNING.
     */
    @OptionGroup("Execution Options")
    @Option (value="-Q Be quiet, do not print much information", aliases={"-quiet"})
    public static boolean logLvlQuiet = false;
    
    /**
     * Be verbose, print extra detailed information. Sets the log level to FINEST.  
     */
    @Option (value="-V Print extra detailed information.", aliases={"-verbose"})
    public static boolean logLvlVerbose = false;
    
    /**
     * Use the new FSM checker instead of the LTL checker. 
     */
    @Option (value="-f Use FSM checker instead of the default LTL checker.", aliases={"-use-fsm-checker"})
    public static boolean useFSMChecker = false;
    // end option group "Execution Options"
    
    
    ////////////////////////////////////////////////////
    /**
     * Regular expression separator string for determining mining granularity 
     */
    @OptionGroup("Parser Options")
    @Option (value="-s Separator regular expression", aliases={"-separator"})
    public static String separator = null;

    /**
     * List of regular expression strings for parsing lines in the input files.
     */
    @Option (value="-r Parser regular expression(s)", aliases={"-regexp"})
    public static List<String> regExps = null;

    /**
     * A substitution expression to partition the lines of the input files.
     */
    @Option (value="-p Partition regular expression", aliases={"-partition"})
    public static String partitionRegExp = null;
    
    /**
     * This flag allows users to get away with sloppy\incorrect regular expressions
     * that might not fully cover the range of log lines appearing in the log files.
     */
    @Option (value="-i Ignore and recover from parse errors as much as possible.", aliases={"-ignore-parse-errors"})
    public static boolean recoverFromParseErrors = false;
    // end option group "Parser Options"
    

    ////////////////////////////////////////////////////
    /**
     * Command line arguments input filename to use.
     */
    @OptionGroup ("Input Options")
    @Option(value="-c Command line arguments input filename", aliases={"-argsfile"})
    public static String argsFilename= null;
    // end option group "Input Options"

    
    ////////////////////////////////////////////////////
    /**
     * Store the final Synoptic representation output in outputFilename.dot
     */
    @Option(value="-o Output filename for dot output", aliases={"--output"})
    @OptionGroup("Output Options")
    public static String outputFilename = null;
    // end option group "Output Options"


    ////////////////////////////////////////////////////
    /**
     * Dump the complete list of mined synoptic.invariants for the set of input files
     * to stdout.
     * 
     * This option is <i>unpublicized</i>; it will not be listed appear in the default usage message
     */
    @OptionGroup (value="Verbosity Options", unpublicized=true)
    @Option("Dump complete list of mined invariant to stdout")
    public static boolean dumpInvariants = false;
    
    /**
     * Dump the dot representation of the initial graph to file. The file
     * will have the name <outputFilename>.initial.dot, where 'outputFilename'
     * is the filename of the final Synoptic output.
     * 
     * This option is <i>unpublicized</i>; it will not be listed appear in the default usage message
     */
    @Option("Dump the initial graph to file <outputFilename>.initial.dot")
    public static boolean dumpInitialGraph = true;
    
    /**
     * Dump the dot representations for intermediate Synoptic steps to
     * file. Each of these files will have a name like:
     * outputFilename.stage-S.round-R.dot where 'outputFilename' is the
     * filename of the final Synoptic output, 'S' is the name of the stage
     * (e.g. r for refinement, and c for coarsening), and 'R' is the round number
     * within the stage.
     * 
     * This option is <i>unpublicized</i>; it will not be listed appear in the default usage message
     */
    @Option("Dump dot files from intermediate Synoptic stages to files of form outputFilename.stage-S.round-R.dot")
    public static boolean dumpIntermediateStages = false;
    // end option group "Verbosity Options"


    ////////////////////////////////////////////////////
    /**
     * Do not perform the coarsening stage in Synoptic, and as final
     * output use the most refined representation.
     * 
     * This option is <i>unpublicized</i>; it will not be listed appear in the default usage message
     */
    @OptionGroup (value="Debugging Options", unpublicized=true)
    @Option("Do not perform the coarsening stage")
    public static boolean noCoarsening = false;
    
    /**
     * Perform benchmarking and output benchmark information
     * 
     * This option is <i>unpublicized</i>; it will not be listed appear in the default usage message
     */
    @Option("Perform benchmarking and output benchmark information")
    public static boolean doBenchmarking = false;
    
    /**
     * Run all the synoptic.tests, and then terminate. 
     * 
     * This option is <i>unpublicized</i>; it will not be listed appear in the default usage message
     */
    @Option("Run all the synoptic.tests, and then terminate.")
    public static boolean runTests = false;

    /**
     * Do not perform the refinement (and therefore do not perform
     * coarsening) and do not produce any representation as
     * output. This is useful for just printing the list of mined
     * synoptic.invariants (using the option 'dumpInvariants' above).
     * 
     * This option is <i>unpublicized</i>; it will not be listed appear in the default usage message
     */
    @Option("Do not perform refinement")
    public static boolean noRefinement = false;
    // end option group "Debugging Options"

    
    /**
     * Input log files to run Synoptic on. These should appear without any
     * options as the final elements in the command line.
     */
    public static List<String> logFilenames = null;

    
    /** One line synopsis of usage */
    private static String usage_string
        = "synoptic [options] <logfiles-to-analyze>";

   /**
    * The synoptic.main method to perform the inference algorithm.  See user
    * documentation for an explanation of the options.
    *
    * @param args - command-line options
    */        
    public static void main(String[] args) throws Exception {
        // this directly sets the static member options of the Main class
        Options options = new Options (usage_string, Main.class);
        String[] cmdLineArgs = options.parse_or_usage(args);
        
        if (argsFilename != null) {
			// read program arguments from a file
			InputStream argsStream = new FileInputStream(argsFilename);
			ListedProperties props = new ListedProperties();
			props.load(argsStream);
			String[] cmdLineFileArgs = props.getCmdArgsLine();
			// the file-based args become the default args
			options.parse_or_usage(cmdLineFileArgs);
		}
		
		// Parse the command line args to override any of the above config file args
		options.parse_or_usage(args);

        // The remainder of the command line is treated as a list of log
        // filenames to process 
        logFilenames = Arrays.asList(cmdLineArgs);
        
        SetUpLogging();

        // Display help for all option groups, including unpublicized ones
        if (allHelp) {
        	System.out.println("Usage: " + usage_string);
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
        
        if (runTests) {
        	runTests();
        }
        
		if (logFilenames.size() == 0) {
			logger.severe("No log filenames specified, exiting. Use -h for help.");
			return;
		}
		
		Main mainInstance = new Main();
        Integer ret = mainInstance.call();
        logger.fine("Main.call() returned " + ret.toString());
		System.exit(ret); 
    }
    
    
    /**
     * Runs all the synoptic unit tests
     * 
     * @throws URISyntaxException if Main.class can't be located
     */
    public static void runTests() throws URISyntaxException {
    	// If we are running from within a jar then jarName contains the path to the jar
    	// otherwise, it contains the path to where Main.class is located on the filesystem 
    	String jarName = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
    	System.out.println("Looking for tests in: " + jarName);
    	        	
    	// We assume that the tests we want to run are classes within the following
    	// packageName, which can be found with the corresponding packagePath filesystem offset
    	String packageName = "synoptic.tests.units.";
    	String packagePath = packageName.replaceAll("\\.", File.separator);
    	
    	ArrayList<String> testClasses = new ArrayList<String>();
    	
        try{
        	// Case1: running from within a jar
        	// Open the jar file and locate the tests by their path
        	JarInputStream jarFile = new JarInputStream(new FileInputStream(jarName));
        	JarEntry jarEntry;
        	while (true) {
        		jarEntry = jarFile.getNextJarEntry();
        		if (jarEntry == null){
        			break;
        		}
        		String className = jarEntry.getName();
        		if ((className.startsWith(packagePath)) &&
        				(className.endsWith(".class")) ) {
        			int endIndex = className.lastIndexOf(".class");
        			className = className.substring(0, endIndex);
        			testClasses.add(className.replaceAll("/", "\\."));
        		}
        	}
        }
        catch (java.io.FileNotFoundException e) {
        	// Case2: not running from within a jar
        	// Find the tests by walking through the directory structure
        	File folder = new File(jarName + packagePath);
        	File[] listOfFiles = folder.listFiles();
        	for (int i = 0; i < listOfFiles.length; i++) {
        		String className = listOfFiles[i].getName();
        		if (listOfFiles[i].isFile() && className.endsWith(".class")) {
        			int endIndex = className.lastIndexOf(".class");
        			className = className.substring(0, endIndex);
        			testClasses.add(packageName + className);
        		}
        	}
        }
        catch( Exception e){
        	e.printStackTrace ();
        	return;
        }
        
        System.out.println("Running tests: " + testClasses);
        String[] testClassesAr = new String[testClasses.size()];
        testClassesAr = testClasses.toArray(testClassesAr);
        JUnitCore.main(testClassesAr);
    	return; 
    }
    

    /**
     * Sets up and configures the Main.logger object based on command line
     * arguments
     */
    public static void SetUpLogging() {
    	// Get the top Logger instance
        logger = Logger.getLogger("");
        
        // Handler for console (reuse it if it already exists)
        Handler consoleHandler = null;
        
        // See if there is already a console handler
        for (Handler handler : logger.getHandlers()) {
        	if (handler instanceof ConsoleHandler) {
        		consoleHandler = handler;
        		break;
        	}
        }
        
	    if (consoleHandler == null) {
	    	// No console handler found, create a new one
	    	consoleHandler = new ConsoleHandler();
	    	logger.addHandler(consoleHandler);
	    }
	    
	    // The consoleHandler will write out anything the logger gives it
	    consoleHandler.setLevel(Level.ALL);

	    // consoleHandler.setFormatter(new CustomFormatter());
        
	    // Set the logger's log level based on command line arguments
        if (logLvlQuiet) {
        	logger.setLevel(Level.WARNING);
        } else if (logLvlVerbose) {
        	logger.setLevel(Level.FINEST);
        } else {
        	logger.setLevel(Level.INFO);
        }

        return;
    }
    
    public static File[] getFiles(String fileArg) {
    	int wildix = fileArg.indexOf("*");
    	if (wildix == -1) {
    		return new File[]{ new File(fileArg) };
    	} else {
    		String uptoWild = fileArg.substring(0, wildix);
    		String path = FilenameUtils.getFullPath(uptoWild);
    		String filter = FilenameUtils.getName(uptoWild) + fileArg.substring(wildix);
    		File dir = new File(path);
    		return dir.listFiles((FileFilter)new WildcardFileFilter(filter));
    	}
    }
    
    /**
     * Returns the filename for an intermediate dot file based on the given
     * stage name and round number. Adheres to the convention specified above
     * in usage, namely that the filename is of the format:
     * outputFilename.stage-S.round-R.dot
     * 
     * @param stageName Stage name string, e.g. "r" for refinement
     * @param roundNum Round number within the stage
     * @return
     */
    public static String getIntermediateDumpFilename(String stageName, int roundNum) {
    	return new String(outputFilename + ".stage-" + stageName + ".round-"+ roundNum + ".dot");
    }

    /***********************************************************/
    
    public Main() {
    	// TODO: can set up graphical state here
    }
    
    /**
     *  The workhorse method, which uses TraceParser to parse the input files, and calls
     *  the primary Synoptic functions to perform refinement\coarsening and
     *  finally outputs the final graph to the output file (specified as a
     *  command line option).
     */
	@Override
	public Integer call() throws Exception {
		// TODO: is there a way to print all the set Options?
		String debug_msg = 
				"logfiles: " + Main.logFilenames + " size: " + Main.logFilenames.size() +  
				"\n\tseparator: " + Main.separator +
				"\n\tregExps: " + Main.regExps + " size: " + Main.regExps.size() +  
				"\n\tpartitionRegExp: " + Main.partitionRegExp;
		
		logger.fine(debug_msg);

		TraceParser parser = new TraceParser();
		
		logger.fine("Setting up the log file parser.");
		
		if (!Main.regExps.isEmpty()) {
			for (String exp : Main.regExps) {
				logger.fine("\taddRegex with exp:" + exp);
				parser.addRegex(exp);
			}
			
			parser.setPartitioner(Main.partitionRegExp != null ? Main.partitionRegExp :
				"\\k<FILE>");
		} else {
			parser.addRegex("^\\s*$(?<SEPCOUNT++>)");
			parser.addRegex("(?<TIME>)?(?<TYPE>.*)");
			parser.setPartitioner(Main.partitionRegExp != null ? Main.partitionRegExp :
				"\\k<SEPCOUNT>\\k<FILE>");
		}

		if (Main.separator != null) {
			parser.addSeparator(Main.separator);
		}

		// Parses all the log filenames, constructing the parsedEvents List.
		List<TraceParser.Occurrence> parsedEvents = new ArrayList<TraceParser.Occurrence>();
		
		logger.fine("Parsing input files..");
		
		for (String fileArg : Main.logFilenames) {
			logger.fine("\tprocessing fileArg: " + fileArg);
			for (File file : getFiles(fileArg)) {
				logger.fine("\tcalling parseTraceFile with file: " + file.getAbsolutePath());
				try {
					parsedEvents.addAll(parser.parseTraceFile(file, -1));
				} catch (ParseException e) {
					logger.severe("Caught ParseException -- unable to continue, exiting. Use -h for help.");
					return new Integer(1);
				}
			}
		}
		
		// If we parsed any events, then run Synoptic.
		logger.fine("Running Synoptic..");
		parser.generateDirectTemporalRelation(parsedEvents, true);
		synoptic.model.Graph<MessageEvent> inputGraph = ((GraphBuilder) parser.builder).getRawGraph();
		
		if (dumpInitialGraph) {
            // If we were given an output filename then export the resulting graph 
			// into outputFilename.initial.dot
            if (Main.outputFilename != null) {
                logger.fine("Exporting initial graph..");
                GraphVizExporter exporter = new GraphVizExporter();
                exporter.exportAsDotAndPngFast(Main.outputFilename + ".initial.dot", inputGraph);
            } else {
            	logger.warning("Cannot output initial graph as outputFilename is not specified");            	
            }
        }		
		
		PartitionGraph result = new PartitionGraph(inputGraph, true);
		/* TemporalInvariantSet synoptic.invariants = result.getInvariants();
		FsmModelChecker<MessageEvent> checker = new FsmModelChecker<MessageEvent>(synoptic.invariants, inputGraph);
		List<RelationPath<MessageEvent>> paths = checker.getCounterexamples();
		if (paths.isEmpty()) {
			System.out.println("synoptic.model checker ok.");
		} */		
		
		logger.fine("Splitting..");
		Bisimulation.refinePartitions(result);
		
		logger.fine("Merging..");
		Bisimulation.mergePartitions(result);

		// TODO: check that none of the initially mined synoptic.invariants are unsatisfied in the result		
		
		// export the resulting graph
		if (Main.outputFilename != null) {
			logger.fine("Exporting final graph with " + result.getNodes().size() + " nodes..");
			GraphVizExporter exporter = new GraphVizExporter();
			exporter.edgeLabels = false;
			exporter.exportAsDotAndPngFast(Main.outputFilename, result);
		}
		
		return new Integer(0);
	}
}