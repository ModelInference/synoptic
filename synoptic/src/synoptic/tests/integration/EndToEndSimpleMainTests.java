package synoptic.tests.integration;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import synoptic.main.SynopticMain;
import synoptic.main.parser.ParseException;
import synoptic.tests.SynopticTest;

/**
 * Tests main with a variety of command line arguments that require no
 * environment customization (e.g., no log file creation). The test oracle is
 * successful execution -- no crashes.
 */
@RunWith(value = Parameterized.class)
public class EndToEndSimpleMainTests extends SynopticTest {
    String[] args;

    @Override
    public void setUp() throws ParseException {
        // Do not call to super, so that a new Synoptic Main is not allocated.
        return;
    }

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
        SynopticMain.main(args);
    }
}
