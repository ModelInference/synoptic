package synoptic.tests.integration;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import synoptic.main.Main;
import synoptic.tests.SynopticTest;

/**
 * Tests main with a variety of command line arguments that require no
 * environment customization (e.g., no log file creation). The test oracle is
 * successful execution -- no crashes.
 */
@RunWith(value = Parameterized.class)
public class EndToEndSimpleMainTests extends SynopticTest {
    String[] args;

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { { new String[] { "" } },
                { new String[] { "--help" } }, { new String[] { "-h" } },
                { new String[] { "-H" } }, { new String[] { "--version" } },
                { new String[] { "-V" } },
                { new String[] { "FILE_THAT_DOES_NOT_EXIST" } } });
    }

    public EndToEndSimpleMainTests(String... argsParam) {
        this.args = argsParam;
    }

    @Test
    public void argsTest() throws Exception {
        Main.main(args);
    }
}
