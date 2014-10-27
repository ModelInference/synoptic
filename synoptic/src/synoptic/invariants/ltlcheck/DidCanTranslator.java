package synoptic.invariants.ltlcheck;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import gov.nasa.ltl.graph.Edge;
import gov.nasa.ltl.graph.Graph;
import gov.nasa.ltl.graph.Node;

public class DidCanTranslator {
    private final static boolean debug = false;

    /**
     * Translates the graph into a did-can graph (see return).
     * 
     * @param lts
     *            original Graph
     * @return expanded graph with atomic propositions for the action did before
     *         and for the actions enabled in this state
     */
    public static Graph translate(Graph lts) {
        Graph g = new Graph();
        LinkedHashMap<Pair<String, Node>, Node> states_o2n = new LinkedHashMap<Pair<String, Node>, Node>();

        // for each node n in lts
        for (Node current : lts.getNodes()) {

            // and each incoming edge labeled l
            for (Edge edge : current.getIncomingEdges()) {

                // add a node <l, n> in did/can-extended graph g
                Node ln = new Node(g);
                ln.setAttributes(current.getAttributes());
                List<AtomicProposition> label = new ArrayList<AtomicProposition>();
                // with did set to l:
                label.add(new AtomicProposition(edge.getAction(),
                        AtomicProposition.PropositionType.Did));

                // and can set extracted from outgoing edges
                for (Iterator<Edge> lts_outgoing_edge_iterator = current
                        .getOutgoingEdges().iterator(); lts_outgoing_edge_iterator
                        .hasNext();) {
                    label.add(new AtomicProposition(lts_outgoing_edge_iterator
                            .next().getAction(),
                            AtomicProposition.PropositionType.Can));
                }

                ln.setAttribute("label", label);

                states_o2n.put(
                        new Pair<String, Node>(edge.getAction(), current), ln);
            }
        }
        // add start node for did/can-expanded graph
        Node start = new Node(g);
        List<AtomicProposition> start_label = new ArrayList<AtomicProposition>();
        start_label.add(new AtomicProposition("__init",
                AtomicProposition.PropositionType.Unknown));
        for (Edge edge : lts.getInit().getOutgoingEdges()) {
            Node to = edge.getNext();
            start_label.add(new AtomicProposition(edge.getAction(),
                    AtomicProposition.PropositionType.Can));
            // also add edges for start node
            Edge e = new Edge(start, states_o2n.get(new Pair<String, Node>(edge
                    .getAction(), to)), edge.getAction());
            e.setAttributes(edge.getAttributes());
        }
        start.setAttribute("label", start_label);

        // set initial start state for did/can-expanded graph
        g.setInit(start);

        // build edges for did/can-expanded graph (initial state already handled
        // !)
        // therefore iterate over all nodes in did/can-expanded graph
        for (Pair<String, Node> current : states_o2n.keySet()) {
            Node from = states_o2n.get(current);
            // iterate over all outgoing edges of the original node that belongs
            // to expanded node
            for (Iterator<Edge> lts_outgoing_edge_iterator = current
                    .getSecond().getOutgoingEdges().iterator(); lts_outgoing_edge_iterator
                    .hasNext();) {
                Edge edge = lts_outgoing_edge_iterator.next();
                String label = edge.getAction();
                Node to = edge.getNext();
                Edge e = new Edge(from, states_o2n.get(new Pair<String, Node>(
                        label, to)), label);
                e.setAttributes(edge.getAttributes());
            }
        }

        if (debug) {
            for (Node c : g.getNodes()) {
                System.out.println("Node [" + c.getId() + "] : "
                        + c.getAttribute("label"));
            }
        }

        return g;
    }
}
