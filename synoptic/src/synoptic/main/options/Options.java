package synoptic.main.options;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import plume.Option;

import synoptic.util.InternalSynopticException;

/**
 * This class defines generic functionality relating to parsing and maintaining
 * a program's command line options. This class is extended/specialized by
 * Synoptic, InvariMint, and other sub-projects, all of which define the
 * specific plume options they expect.
 */
public abstract class Options {

    /**
     * List of non option arguments that are specified at the end of the command
     * line, which are treated as logFilenames in Synoptic projects.
     */
    public List<String> logFilenames = null;

    /**
     * The instance of plume.Options that we use for parsing the arguments,
     * generating usage strings, etc.
     */
    protected plume.Options plumeOptions = null;

    public Options() {
        //
    }

    /**
     * Returns a string that describes how to use the program at the high level,
     * e.g.: "synoptic [options] <logfiles-to-analyze>"
     */
    abstract public String getUsageString();

    /**
     * Users may specify a filename that contains command line arguments to use
     * (in addition to any arguments on the command line). This function returns
     * this filename. The return value may be 'null', if such a filename was not
     * specified.
     */
    abstract public String getArgsFilename();

    /**
     * Sets the options based on args array. Uses plume-lib for options
     * processing and assumes that the plume options are defined as part of
     * _this_ class instance (i.e., that it is specialized to include plume
     * options).
     */
    public void setOptions(String[] args) throws IOException {
        // Sets the fields in this class annotated with @Option.
        plumeOptions = new plume.Options(getUsageString(), this);
        String[] cmdLineArgs = plumeOptions.parse_or_usage(args);

        if (getArgsFilename() != null) {
            // Read program arguments from a file.
            InputStream argsStream = new FileInputStream(getArgsFilename());
            ListedProperties props = new ListedProperties();
            props.load(argsStream);
            String[] cmdLineFileArgs = props.getCmdArgsLine();
            // The file-based arguments become the default arguments.
            plumeOptions.parse_or_usage(cmdLineFileArgs);
        }

        // Parse the command line arguments, overriding any of the above config
        // file arguments.
        plumeOptions.parse_or_usage(args);

        // The remainder of the command line is treated as a list of non
        // optional arguments.
        logFilenames = new LinkedList<String>(Arrays.asList(cmdLineArgs));

        // Remove any empty string non-opt arguments.
        while (logFilenames.contains("")) {
            logFilenames.remove("");
        }
    }

    /**
     * Prints help for just the 'publicized' option groups.
     */
    public void printShortHelp() {
        plumeOptions.print_usage();
    }

    /**
     * Prints the values of all the options.
     * 
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public void printOptionValues() throws IllegalArgumentException,
            IllegalAccessException {
        StringBuffer optsString = new StringBuffer();
        optsString.append("Synoptic options:\n");
        for (Field field : this.getClass().getDeclaredFields()) {
            if (field.getAnnotation(Option.class) != null) {
                optsString.append("\t");
                optsString.append(field.getName());
                optsString.append(": ");
                if (field.get(this) != null) {
                    optsString.append(field.get(this).toString());
                    optsString.append("\n");
                } else {
                    optsString.append("null\n");
                }
            }
        }
        // Append options that are not annotated with @Option:
        optsString.append("\tlogFilenames: ");
        optsString.append(logFilenames.toString());
        optsString.append("\n");

        System.out.println(optsString.toString());
    }

    /**
     * Returns a command line option description for an option name for class of
     * this instance.
     */
    public String getOptDesc(String optName) throws InternalSynopticException {
        return getOptDesc(optName, this.getClass());
    }

    /**
     * Returns a command line option description for an option name
     * 
     * @param optName
     *            The option variable name
     * @return a string description of the option
     * @throws InternalSynopticException
     *             if optName cannot be accessed
     */
    public static String getOptDesc(String optName, Class<?> optsCls)
            throws InternalSynopticException {
        Field field;
        try {
            // field = SynopticOptions.class.getField(optName);
            field = optsCls.getField(optName);
            // field = this.getClass().getField(optName);
        } catch (SecurityException e) {
            throw InternalSynopticException.wrap(e);
        } catch (NoSuchFieldException e) {
            throw InternalSynopticException.wrap(e);
        }

        Option opt;
        String desc;
        try {
            opt = field.getAnnotation(Option.class);
            desc = opt.value();
        } catch (NullPointerException e) {
            throw InternalSynopticException.wrap(e);
        }

        if (desc.length() > 0 && desc.charAt(0) != '-') {
            // For options that do not have a short option form,
            // include the long option trigger in the description.
            desc = "--" + optName + " " + desc;
        }
        return desc;
    }
}
