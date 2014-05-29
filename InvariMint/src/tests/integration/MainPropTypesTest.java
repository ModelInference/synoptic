package tests.integration;

import static org.junit.Assert.assertTrue;

import java.io.File;

import main.OptionException;
import model.EncodedAutomaton;

import org.junit.Test;

import synoptic.model.event.EventType;
import synoptic.model.event.StringEventType;

import tests.InvariMintTest;
import tests.units.InvMintPropTypesTest;

/**
 * Tests the addition of InvariMintPropTypes Algorithm to main, mainly testing
 * option exceptions. Uses the simple-model log.
 */
public class MainPropTypesTest extends InvariMintTest {

    private EventType a = new StringEventType("a");
    private EventType b = new StringEventType("b");

    /**
     * Tests InvariMint with property types and an option which cannot be
     * applied to a non PGraph-based algorithm. Should cause OptionException.
     * 
     * @throws Exception
     */
    @Test(expected = OptionException.class)
    public void cannotSpecifyPGraphOptionsTest() throws Exception {
        String tPath = ".." + File.separator + "traces" + File.separator;
        String simpleModelPath = tPath + "abstract" + File.separator
                + "simple-model" + File.separator;

        String[] args = new String[] { "-r",
                "^(?<DTYPE>.+)(?<nodename>)(?<TYPE>)$", "-m", "\\k<nodename>",
                "-o", testOutputDir + "ktail-simple-model-example",
                "--removeSpuriousEdges=true", "--AP",
                simpleModelPath + "trace.txt" };
        EncodedAutomaton dfa = runInvariMintWithArgs(args);

    }

    /**
     * Tests whether specifying another InvariMintAlgorithm with individual
     * property types causes an OptionException.
     * 
     * @throws Exception
     */
    @Test(expected = OptionException.class)
    public void cannotSpecifyTwoAlgsTest() throws Exception {
        String tPath = ".." + File.separator + "traces" + File.separator;
        String simpleModelPath = tPath + "abstract" + File.separator
                + "simple-model" + File.separator;

        String[] args = new String[] { "-r",
                "^(?<DTYPE>.+)(?<nodename>)(?<TYPE>)$", "-m", "\\k<nodename>",
                "-o", testOutputDir + "ktail-simple-model-example",
                "--invMintKTails", "--AP", simpleModelPath + "trace.txt" };
        EncodedAutomaton dfa = runInvariMintWithArgs(args);

    }

    /**
     * Tests whether --AP DFA is correctly constructed by comparing output to
     * created AP DFA.
     * 
     * @throws Exception
     */
    @Test
    public void simpleModelMainTest() throws Exception {
        String tPath = ".." + File.separator + "traces" + File.separator;
        String simpleModelPath = tPath + "abstract" + File.separator
                + "simple-model" + File.separator;

        String[] args = new String[] { "-r",
                "^(?<DTYPE>.+)(?<nodename>)(?<TYPE>)$", "-m", "\\k<nodename>",
                "-o", testOutputDir + "ktail-simple-model-example", "--AP",
                simpleModelPath + "trace.txt" };
        EncodedAutomaton dfa = runInvariMintWithArgs(args);
        InvMintPropTypesTest dfaCreator = new InvMintPropTypesTest();
        dfaCreator.fillAlphabet();
        EncodedAutomaton expectedDfa = dfaCreator.createAPdfa(a, b);

        assertTrue(dfa.subsetOf(expectedDfa));
        assertTrue(expectedDfa.subsetOf(dfa));

    }

}
