package shivector.options;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import plume.Option;
import plume.OptionGroup;
import plume.Options;

/**
 * Uses the ListedProperties class to handle a property file that encodes
 * ShiVector options, e.g. use the --useLog4J=true option to specify that the
 * target application uses the Log4J framework for logging. Attempts to read a
 * file "shiv.opts" from the current working directory. Currently only allows
 * users to specify the logging framework, though additional options may be
 * added.
 * 
 * @author jennyabrahamson
 */
public class ShiVectorOptions {

    // Look for options file in which ever directory the application is executed
    private static final String OPTIONS_FILE = "shiv.opts";

    /**
     * The instance of plume.Options that we use for parsing the arguments,
     * generating usage strings, etc.
     */
    protected Options plumeOptions = null;

    /** One line synopsis of usage */
    private static final String usageString = "Set options with properties file: "
            + OPTIONS_FILE;

    private ShiVectorOptions() {
        plumeOptions = new Options(usageString, this);
        try {
            InputStream argsStream = new FileInputStream(OPTIONS_FILE);
            ListedProperties props = new ListedProperties();
            props.load(argsStream);
            String[] cmdLineFileArgs = props.getCmdArgsLine();
            // The file-based arguments become the default arguments.
            plumeOptions.parse_or_usage(cmdLineFileArgs);
        } catch (FileNotFoundException e) {
            System.out.println("No options file, using default options");
        } catch (IOException e) {
            System.out
                    .println("Error reading options file, using default options");
        }
    }

    public static ShiVectorOptions getOptions() {
        return new ShiVectorOptions();
    }

    /**
     * Whether to prepend System.out.println logging statements with vector
     * clocks.
     */
    @OptionGroup("Logging framework options")
    @Option("Whether to prepend System.out.println logging statements with vector clocks.")
    public boolean usePrintln = true;

    /**
     * Whether to prepend log4j logging statements with vector clocks.
     */
    @Option("Whether to prepend log4j logging statements with vector clocks.")
    public boolean useLog4J = true;

    /**
     * Whether to intercept java.net.Socket streams
     */
    @OptionGroup("Network framework options")
    @Option("Whether to intercept the Jjava.net.Socket streams.")
    public boolean useSocketsAPI = false;

    /**
     * Whether to intercept the java.nio.Channel streams
     */
    @Option("Whether to intercept the java.nio.Channel streams.")
    public boolean useNioAPI = false;

    /**
     * Whether to intercept the mina API
     */
    @Option("Whether to intercept the Mina API.")
    public boolean useMinaAPI = true;

    /**
     * Whether to use a clock per thread instead of per process.
     */
    @Option("Whether to use a clock per thread instead of per process.")
    public boolean useThreadsAsHosts = false;
}
