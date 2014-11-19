package synoptic.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import synoptic.main.SynopticMain;

/**
 * Contains useful functionality for dealing with synoptic.jar
 */
public class SynopticJar {

    /** Suppress default constructor for non-instantiability */
    private SynopticJar() {
        throw new AssertionError();
    }

    /**
     * Retrieve and return the ChangesetID attribute in the manifest of the jar
     * that contains the Synoptic Main class. Returns null if not running from a
     * jar.
     * 
     * @throws IOException
     */
    public static String getHgChangesetID() throws IOException {
        String changesetID = null;

        // Find the resource corresponding to the Synoptic Main class.
        URL res = SynopticMain.class.getResource(SynopticMain.class
                .getSimpleName() + ".class");

        URLConnection conn = res.openConnection();
        if (!(conn instanceof JarURLConnection)) {
            // We are not running from inside a jar. In this case, return null
            // for ChangesetID.
            return null;
        }
        JarURLConnection jarConn = (JarURLConnection) conn;

        // Grab attributes from the manifest of the jar (synoptic.jar)
        Manifest mf = jarConn.getManifest();
        Attributes atts = mf.getMainAttributes();

        // Extract ChangesetID from the attributes and return it.
        changesetID = atts.getValue("ChangesetID");

        return changesetID;
    }

    /**
     * Returns the set of all tests within a particular package (e.g.,
     * "synoptic.tests.units"). This method can be used with Synoptic in a jar
     * file (e.g., synoptic.jar), but also if Synoptic is not within a jar file. <br />
     * <br/>
     * TODO: Create a unit test for this method that would run Synoptic as a
     * jar.
     * 
     * @throws URISyntaxException
     *             if Synoptic Main.class can't be located
     * @throws IOException
     */
    public static List<String> getTestsInPackage(String packageName)
            throws URISyntaxException, IOException {

        // If we are running from within a jar, then jarName contains the path
        // to the jar. Otherwise, it contains the path to where Main.class is
        // located on the filesystem
        String jarName = SynopticMain.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI().getPath();
        System.out.println("Looking for tests in: " + jarName);

        // We assume that the tests we want to run are classes within
        // packageName, which can be found with the corresponding packagePath
        // filesystem offset
        String packagePath = packageName.replace('.', '/');

        ArrayList<String> testClasses = new ArrayList<String>();

        JarInputStream jarFile = null;
        try {
            // Case1: running from within a jar.
            // Open the jar file and locate the tests by their path.
            jarFile = new JarInputStream(new FileInputStream(jarName));
            JarEntry jarEntry;
            while (true) {
                jarEntry = jarFile.getNextJarEntry();
                if (jarEntry == null) {
                    break;
                }
                System.out.println("jarEntry : " + jarEntry.toString());

                String className = jarEntry.getName();
                if (className.startsWith(packagePath)
                        && className.endsWith(".class")) {
                    int endIndex = className.lastIndexOf(".class");
                    className = className.substring(0, endIndex);
                    testClasses.add(className.replace('/', '.'));
                }
            }
        } catch (java.io.FileNotFoundException e) {
            // Case2: not running from within a jar.
            // Find the tests by walking through the directory structure.
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
        } catch (Exception e) {
            throw InternalSynopticException.wrap(e);
        } finally {
            if (jarFile != null) {
                jarFile.close();
            }
        }

        // Remove anonymous inner classes from the list, these look like
        // 'TraceParserTests$1.class'
        ArrayList<String> anonClasses = new ArrayList<String>();
        for (String testClass : testClasses) {
            if (testClass.contains("$")) {
                anonClasses.add(testClass);
            }
        }
        testClasses.removeAll(anonClasses);

        return testClasses;
    }

}
