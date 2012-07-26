package dynoptic.main;

import org.junit.Test;

import dynoptic.DynopticTest;

public class DynopticMainTests extends DynopticTest {

    @Test(expected = OptionException.class)
    public void dynopticMainBadOptions() throws Exception {
        DynopticMain d = new DynopticMain(new DynopticOptions());
    }
}
