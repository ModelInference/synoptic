package mcscm;

/**
 * Used to detect the OS on which we are executing. Currently, we only care
 * about Mac and Linux detection.
 */
public final class Os {

    public static String getOsName() {
        return System.getProperty("os.name", "unknown");
    }

    public static boolean isLinux() {
        return getOsName().toLowerCase().indexOf("linux") >= 0;
    }

    public static boolean isMac() {
        final String os = getOsName().toLowerCase();
        return os.startsWith("mac");
    }
}