package synoptic.tests.units;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;

import org.junit.Test;

import synoptic.main.options.ListedProperties;
import synoptic.tests.SynopticTest;

/**
 * Tests for synoptic.main.ListedProperties class
 * 
 * @author ivan
 */
public class ListedPropertiesTests extends SynopticTest {

    /**
     * Uses ListedProperties to convert an options string to a cmd args array
     * 
     * @param optionsStr
     *            options string to process
     * @return command line args array
     */
    private String[] optionsStringToCmdArgs(String optionsStr) {
        StringReader reader = new StringReader(optionsStr);
        ListedProperties props = new ListedProperties();
        try {
            props.load(reader);
        } catch (IOException e) {
            fail("Unexpected IOException");
        }
        String[] cmdArgs = props.getCmdArgsLine();
        return cmdArgs;
    }

    /**
     * Make sure we can add an option with one value
     */
    @Test
    public void onePropOneValTest() {
        // -option some-value
        String[] cmdArgs = optionsStringToCmdArgs("-option some-value");
        assertArrayEquals(cmdArgs, new String[] { "-option", "some-value" });

        // -option=some-value
        cmdArgs = optionsStringToCmdArgs("-option=some-value");
        assertArrayEquals(cmdArgs, new String[] { "-option", "some-value" });

        // --option=some-value
        cmdArgs = optionsStringToCmdArgs("--option=some-value");
        assertArrayEquals(cmdArgs, new String[] { "--option=some-value" });
    }

    /**
     * Make sure we can add an option with multiple values
     */
    @Test
    public void onePropMultValsTest() {
        String[] cmdArgs = optionsStringToCmdArgs("-option some-value\n-option some-value2");
        assertArrayEquals(cmdArgs, new String[] { "-option", "some-value",
                "-option", "some-value2" });
    }

    /**
     * Test setting multiple options with multiple values
     */
    @Test
    public void multPropsMultValsTest() {
        String[] cmdArgs = optionsStringToCmdArgs("-opt1 val1\n-opt1 val2\n-opt2 val3\n-opt2 val4");
        // there is no ordering constraint on whether opt1 set or opt2 set of
        // options appears first, so we test for both orderings, and assert that
        // cmdArgs follows one of them.
        String[] order1 = { "-opt1", "val1", "-opt1", "val2", "-opt2", "val3",
                "-opt2", "val4" };
        String[] order2 = { "-opt2", "val3", "-opt2", "val4", "-opt1", "val1",
                "-opt1", "val2" };
        assertTrue(Arrays.equals(order1, cmdArgs)
                || Arrays.equals(order2, cmdArgs));
    }
}
