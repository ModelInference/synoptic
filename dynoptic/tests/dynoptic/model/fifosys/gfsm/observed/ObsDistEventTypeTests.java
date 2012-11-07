package dynoptic.model.fifosys.gfsm.observed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import synoptic.model.event.DistEventType;

public class ObsDistEventTypeTests {

    @Test
    public void buildInstance() {
        DistEventType etype = DistEventType.LocalEvent("e", 0);
        ObsDistEventType e = new ObsDistEventType(etype, 1);
        assertEquals(e.getDistEType(), etype);
        assertTrue(e.getTraceIds().contains(1));
    }

    @Test
    public void equality() {
        DistEventType etype = DistEventType.LocalEvent("e", 0);
        DistEventType etype2 = DistEventType.LocalEvent("e", 0);
        ObsDistEventType e1 = new ObsDistEventType(etype, 1);
        ObsDistEventType e2 = new ObsDistEventType(etype2, 2);

        assertFalse(e1.equals(e2));
        assertTrue(e1.equalsIgnoringTraceIds(e2));
        assertTrue(e1.equalsIgnoringTraceIds(e1));

        DistEventType etype3 = DistEventType.LocalEvent("f", 0);
        ObsDistEventType e3 = new ObsDistEventType(etype3, 2);
        assertFalse(e3.equals(e2));
        assertFalse(e3.equalsIgnoringTraceIds(e2));
    }

    @Test
    public void traceIds() {
        DistEventType etype = DistEventType.LocalEvent("e", 0);
        ObsDistEventType e = new ObsDistEventType(etype, 1);
        assertTrue(e.getTraceIds().contains(1));
        assertFalse(e.getTraceIds().contains(2));

        e.addTraceId(2);
        assertTrue(e.getTraceIds().contains(1));
        assertTrue(e.getTraceIds().contains(2));
    }
}
