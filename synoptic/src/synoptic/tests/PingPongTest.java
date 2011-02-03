package synoptic.tests;

import java.util.ArrayList;
import java.util.Random;

import synoptic.algorithms.bisim.Bisimulation;
import synoptic.model.Action;
import synoptic.model.Graph;
import synoptic.model.MessageEvent;
import synoptic.model.PartitionGraph;
import synoptic.model.export.GraphVizExporter;
import synoptic.model.input.GraphBuilder;

/**
 * Main Class. To create you own examples construct a transition system, as done
 * in pingExample. The program will export the graphs before and after
 * minimization.
 * 
 * @author sigurd
 */

public class PingPongTest {
    static public void main(String[] args) throws Exception {
        Graph<MessageEvent> t = pingExample();
        GraphVizExporter exporter = new GraphVizExporter();
        exporter.exportAsDotAndPng("output/ping-pong/ping.dot", t);
        System.out.println("Wrote ping.dot.");
        PartitionGraph g = Bisimulation.getSplitGraph(t);
        System.out.println("Done minimizing.");
        exporter.exportAsDotAndPng("output/ping-pong/ping-minimized.dot", g);
        System.out.println("done.");
    }

    public static Graph<MessageEvent> pingExample() throws Exception {
        GraphBuilder gb = new GraphBuilder();
        Random r = new Random(1001);
        for (int i = 0; i < 10; i++) {
            gb.append(new Action("ping"));
            if (r.nextBoolean()) {
                Action a = new Action("pong");
                a.setStringArgument("payload", "0");
                gb.append(a);
                if (r.nextDouble() > 0.7) {
                    a.setStringArgument("payload", "1");
                    gb.append(new Action("status"));
                }
            }
            // gb.split();
        }
        return gb.getRawGraph();
    }

    public static PartitionGraph fragExample() throws Exception {
        GraphBuilder gb = new GraphBuilder();
        Random r = new Random();
        int LOOPS = 10;
        for (int i = 0; i < LOOPS; i++) {
            ArrayList<String> states = new ArrayList<String>();
            int NUM_FRAG = 3;
            for (int numFrag = 0; numFrag < NUM_FRAG; ++numFrag) {
                states.add("frag");
            }
            for (int j = 0; j < NUM_FRAG; j++) {
                int isel = r.nextInt(states.size());
                String selected = states.get(isel);
                states.remove(selected);
                gb.append(new Action(selected));
            }

            gb.append(new Action("ack"));
        }
        return gb.getGraph(true);
    }
}
