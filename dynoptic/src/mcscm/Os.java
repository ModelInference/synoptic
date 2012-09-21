package mcscm;

/**
 * Utility class that is used to detect the OS on which we are executing.
 * Currently, we only care about Mac and Linux detection.
 */
public final class Os {

    public static String getOsName() {
        return System.getProperty("os.name", "unknown");
    }

    public static String getOsVersion() {
        return System.getProperty("os.version");
    }

    /**
     * Returns the version string, omitting the minor number. For example, if
     * the version string is "10.8.2" then this returns "10.8"
     */
    public static String getMajorOSXVersion() {
        String vs = getOsVersion();
        if (!vs.contains(".")) {
            return vs;
        }
        int lastDotIndex = vs.lastIndexOf(".");
        return vs.substring(0, lastDotIndex);
    }

    public static String getOsArch() {
        return System.getProperty("os.arch");
    }

    public static boolean isLinux() {
        return getOsName().toLowerCase().indexOf("linux") >= 0;
    }

    public static boolean isMac() {
        final String os = getOsName().toLowerCase();
        return os.startsWith("mac");
    }
}