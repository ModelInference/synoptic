import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;

import plume.Option;
import plume.OptionGroup;
import plume.Options;

public class EmbeddedGwt {
    @OptionGroup("General Options")
    @Option(
            value = "-p Port on which to start the server (default IP is 127.0.0.1)",
            aliases = { "-port" })
    public static int port = 8080;

    /**
     * Print the current Synoptic version.
     */
    @Option(value = "-V Print Synoptic version", aliases = { "-version" })
    public static boolean version = false;

    /**
     * One line synopsis of usage
     */
    private static String usage_string = "embeddedgwt -p port";

    public static void main(String[] args) throws Throwable {
        // this directly sets the static member options of the Main class
        Options options = new Options(usage_string, EmbeddedGwt.class);
        options.parse_or_usage(args);

        if (EmbeddedGwt.version) {
            // TODO: need to print Synoptic/SynopticGWT version somehow
            // System.out.println("Synoptic version "
            // + synoptic.main.Main.versionString);
            return;
        }

        // Create an embedded Jetty server on the user-defined port
        Server server = new Server(EmbeddedGwt.port);

        // Create a handler for processing our GWT app
        WebAppContext handler = new WebAppContext();
        handler.setContextPath("/");
        handler.setWar("./apps/synopticgwt.war");

        // Add it to the server
        server.setHandler(handler);

        // Other misc. options
        // TODO: turn this into a command line option
        server.setThreadPool(new QueuedThreadPool(20));

        // And start it up
        try {
            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
