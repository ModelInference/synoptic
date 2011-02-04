package synoptic.tests;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import synoptic.algorithms.graph.GraphUtil;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.model.Action;
import synoptic.model.Graph;
import synoptic.model.LogEvent;
import synoptic.model.export.GraphVizExporter;
import synoptic.model.input.GraphBuilder;
import synoptic.model.input.NetBuilder;
import synoptic.model.input.PetersonReader;
import synoptic.model.nets.PetriEvent;
import synoptic.model.nets.Net;
import synoptic.model.nets.Place;
import synoptic.statistics.FrequencyMiner;
import synoptic.util.InternalSynopticException;

public class TraceCondenserTest {
    private static String igAPRel = "AP";

    public static void main(String[] args) throws Exception {
        GraphBuilder b = new GraphBuilder();
        PetersonReader<LogEvent> r = new PetersonReader<LogEvent>(b);
        GraphVizExporter exporter = new GraphVizExporter();
        r.readGraphSet(
                "traces/PetersonLeaderElection/generated_traces/no-rand.trace",
                /* 5process_trace-5-1 */
                1);
        Graph<LogEvent> g = b.getGraph();
        exporter.exportAsDotAndPng("output/traceCondenser/initial.dot", g);
        FrequencyMiner<LogEvent> miner = new FrequencyMiner<LogEvent>(g
                .getNodes());
        System.out.println(miner);

        TemporalInvariantSet s = TemporalInvariantSet.computeInvariants(g);
        TemporalInvariantSet s2 = TemporalInvariantSet.computeInvariantsSplt(g,
                "relay");
        System.out.println(s2);
        Graph<LogEvent> igAP = s.getInvariantGraph("AP");
        exportInvariants(exporter, s, "synoptic.invariants");
        exportInvariants(exporter, s2, "synoptic.invariants-splt");
        System.out.println(s);
        NetBuilder netBuilder = new NetBuilder();
        GraphUtil.copyTo(g, netBuilder);
        Net net = netBuilder.getNet();
        exporter.exportAsDotAndPng("output/traceCondenser/initial.dot", net);
        Net newNet = condense(igAP, net);
        exporter.exportAsDotAndPng(
                "output/traceCondenser/initial-condensed.dot", newNet);
    }

    private static Net condense(Graph<LogEvent> igAP, Net net) {
        HashMap<String, LogEvent> map = new HashMap<String, LogEvent>();
        for (LogEvent m : igAP.getNodes()) {
            map.put(m.getLabel(), m);
        }
        for (Place p : net.getInitalPlaces()) {
            Place current = p;
            while (current != null) {
                Set<Place> nexts = current.getPostPlaces();
                if (nexts.size() != 1) {
                    System.out.println("not linear");
                    break;
                }
                Place next = nexts.iterator().next();
                Set<PetriEvent> first = net.getPre(next);
                Set<PetriEvent> second = next.getPost();
                HashMap<PetriEvent, Set<PetriEvent>> related = new HashMap<PetriEvent, Set<PetriEvent>>();
                if (noneRelated(first, second, map, related)
                        && noneRelated(second, first, map, related)) {
                    System.out.println("contracting " + current + " " + next);
                    net.contract(current, next);
                } else {
                    System.out.println("related: " + related);
                }
                current = next;
            }
        }
        return net;
    }

    private static void exportInvariants(GraphVizExporter exporter,
            TemporalInvariantSet s, String fileName) throws Exception {
        exporter.exportAsDotAndPng(
                "output/traceCondenser/" + fileName + ".dot", s
                        .getInvariantGraph(null));
        exporter.exportAsDotAndPng("output/traceCondenser/" + fileName
                + "-AP.dot", s.getInvariantGraph("AP"));
        exporter.exportAsDotAndPng("output/traceCondenser/" + fileName
                + "-AFby.dot", s.getInvariantGraph("AFby"));
        exporter.exportAsDotAndPng("output/traceCondenser/" + fileName
                + "-NFby.dot", s.getInvariantGraph("NFby"));
    }

    private static boolean noneRelated(Set<PetriEvent> first, Set<PetriEvent> second,
            HashMap<String, LogEvent> map,
            HashMap<PetriEvent, Set<PetriEvent>> related) {
        boolean ret = true;
        for (PetriEvent e : first) {
            for (PetriEvent e2 : second) {
                if (map.get(e.getName()).getTransition(map.get(e2.getName()),
                        igAPRel) != null) {
                    ret = false;
                    if (!map.containsKey(e)) {
                        related.put(e, new HashSet<PetriEvent>());
                    }
                    related.get(e).add(e2);
                }
            }
        }
        return ret;
    }

    private static Net condense2(Graph<LogEvent> igAP, Net net) {
        HashMap<PetriEvent, PetriEvent> map2 = new HashMap<PetriEvent, PetriEvent>();
        HashMap<String, LogEvent> map = new HashMap<String, LogEvent>();
        for (LogEvent m : igAP.getNodes()) {
            map.put(m.getLabel(), m);
        }
        NetBuilder nb = new NetBuilder();
        for (PetriEvent e : net.getInitalEvents()) {
            PetriEvent first = nb.insert(new Action(e.getName()));
            PetriEvent e2 = e;
            map2.put(e, first);
            nb.tagInitial(first, "");

            while (e2.getPost().size() > 0) {
                Set<PetriEvent> post = e2.getPostEvents();
                if (post.size() == 0) {
                    break;
                }
                if (post.size() > 1) {
                    throw new InternalSynopticException("not linear");
                }
                e2 = post.iterator().next();
                Set<PetriEvent> related = getFirstRelatedPredecessor(net, e2, map);
                System.out.println(e2 + " related " + related);
                Set<PetriEvent> relatedTranslated = new HashSet<PetriEvent>();
                for (PetriEvent evt : related) {
                    relatedTranslated.add(map2.get(evt));
                }
                PetriEvent newEvent = nb.insert(new Action(e2.getName()));
                map2.put(e2, newEvent);
                for (PetriEvent rel : relatedTranslated) {
                    nb.connect(rel, newEvent, "");
                }
            }
        }
        return nb.getNet();
    }

    private static Set<PetriEvent> getFirstRelatedPredecessor(Net net, PetriEvent e2,
            HashMap<String, LogEvent> map) {
        HashSet<PetriEvent> relatedEvents = new HashSet<PetriEvent>();
        Set<PetriEvent> pre = net.getPreEvents(e2);
        if (pre.size() > 1) {
            throw new InternalSynopticException("not linear");
        }
        PetriEvent current = pre.iterator().next();
        HashMap<PetriEvent, Set<PetriEvent>> related = new HashMap<PetriEvent, Set<PetriEvent>>();
        while (current != null) {
            if (!noneRelated(Collections.singleton(current), Collections
                    .singleton(e2), map, related)
                    || !noneRelated(Collections.singleton(e2), Collections
                            .singleton(current), map, related)) {
                relatedEvents.add(current);
            }
            Set<PetriEvent> pre2 = net.getPreEvents(current);
            if (pre2.size() == 0) {
                return relatedEvents;
            }
            if (pre2.size() > 1) {
                throw new InternalSynopticException("not linear");
            }
            current = pre2.iterator().next();
        }
        return relatedEvents;
    }
}
