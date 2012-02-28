package synoptic.main;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import plume.Option;

import synoptic.util.InternalSynopticException;

public abstract class Options {

    /**
     * List of non option arguments that are specified at the end of the command
     * line, which are treated as logFilenames in Synoptic projects.
     */
    public List<String> logFilenames = null;

    protected plume.Options plumeOptions = null;

    public Options() {
        //
    }

    abstract public String getUsageString();

    abstract public String getArgsFilename();

    public void setOptions(String[] args) throws IOException {
        // Sets the fields in this class annotated with @Option
        plumeOptions = new plume.Options(getUsageString(), this);
        String[] cmdLineArgs = plumeOptions.parse_or_usage(args);

        if (getArgsFilename() != null) {
            // read program arguments from a file
            InputStream argsStream = new FileInputStream(getArgsFilename());
            ListedProperties props = new ListedProperties();
            props.load(argsStream);
            String[] cmdLineFileArgs = props.getCmdArgsLine();
            // the file-based args become the default args
            plumeOptions.parse_or_usage(cmdLineFileArgs);
        }

        // Parse the command line args to override any of the above config file
        // args
        plumeOptions.parse_or_usage(args);

        // The remainder of the command line is treated as a list of non
        // optional arguments.
        logFilenames = new LinkedList<String>(Arrays.asList(cmdLineArgs));

        // Remove any empty string non-opt args.
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
     * Returns a command line option description for an option name
     * 
     * @param optName
     *            The option variable name
     * @return a string description of the option
     * @throws InternalSynopticException
     *             if optName cannot be accessed
     */
    public static String getOptDesc(String optName)
            throws InternalSynopticException {
        Field field;
        try {
            field = SynopticOptions.class.getField(optName);
        } catch (SecurityException e) {
            throw InternalSynopticException.wrap(e);
        } catch (NoSuchFieldException e) {
            throw InternalSynopticException.wrap(e);
        }
        Option opt = field.getAnnotation(Option.class);
        String desc = opt.value();
        if (desc.length() > 0 && desc.charAt(0) != '-') {
            // For options that do not have a short option form,
            // include the long option trigger in the description.
            desc = "--" + optName + " " + desc;
        }
        return desc;
    }

}
