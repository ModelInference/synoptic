package synoptic.invariants.ltlcheck;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import gov.nasa.ltl.graph.Edge;
import gov.nasa.ltl.graph.Graph;
import gov.nasa.ltl.graph.Node;

public class ProductTranslator {
    public static final boolean debug = false;

    public static GeneralGraph translate(Graph dcts, Graph ba) {
        GeneralGraph p = new GeneralGraph();
        LinkedHashMap<Pair<Node, Node>, Node> p_states = new LinkedHashMap<Pair<Node, Node>, Node>();

        // GENERATE NODES AND EDGES:
        // iterate over all did/can-expanded graph nodes
        for (Node dcts_from : dcts.getNodes()) {
            // iterate over all buchi automata nodes
            for (Node ba_from : ba.getNodes()) {
                // iterate over all outgoing edges of did/can-expanded graph
                // node
                for (Edge dcts_edge : dcts_from.getOutgoingEdges()) {
                    Node dcts_to = dcts_edge.getNext();
                    String alpha = dcts_edge.getGuard();
                    List<AtomicProposition> dcts_to_label = getLabel(dcts_to,
                            dcts_edge);

                    // iterate over all outgoing edges of buchi automata node
                    for (Edge ba_edge : ba_from.getOutgoingEdges()) {
                        Node ba_to = ba_edge.getNext();
                        Conjunction ba_edge_label = (Conjunction) ba_edge
                                .getAttribute("parsedaction");

                        if (debug) {
                            System.out.print("considering <"
                                    + dcts_from.getId() + "," + ba_from.getId()
                                    + "> ");
                            System.out.print("<" + dcts_to.getId() + ","
                                    + ba_to.getId() + "> : " + ba_edge_label
                                    + " vs." + dcts_to_label);
                        }

                        if (ba_edge_label.allows(dcts_to_label)) {
                            if (debug) {
                                System.out.println(" accept");
                            }
                            Node p_from = null;
                            Node p_to = null;

                            Pair<Node, Node> key_from = new Pair<Node, Node>(
                                    dcts_from, ba_from);
                            Pair<Node, Node> key_to = new Pair<Node, Node>(
                                    dcts_to, ba_to);

                            if (!p_states.containsKey(key_from)) {
                                p_from = new Node(p);
                                p_from.setAttributes(dcts_from.getAttributes());
                                p_from.setStringAttribute("name", "<"
                                        + dcts_from.getId() + ","
                                        + ba_from.getId() + ">");
                                // set accpeting if ba node is accepting
                                if (ba_from.getBooleanAttribute("accepting")) {
                                    p_from.setBooleanAttribute("accepting",
                                            true);
                                }
                                p_states.put(key_from, p_from);
                            } else {
                                p_from = p_states.get(key_from);
                            }
                            if (!p_states.containsKey(key_to)) {
                                p_to = new Node(p);
                                p_to.setAttributes(dcts_to.getAttributes());
                                p_to.setStringAttribute("name", "<"
                                        + dcts_to.getId() + "," + ba_to.getId()
                                        + ">");
                                // set accepting if ba node is accepting
                                if (ba_to.getBooleanAttribute("accepting")) {
                                    p_to.setBooleanAttribute("accepting", true);
                                }
                                p_states.put(key_to, p_to);
                            } else {
                                p_to = p_states.get(key_to);
                            }

                            Edge e = new Edge(p_from, p_to, alpha);
                            e.setAttributes(dcts_edge.getAttributes());
                        } else {
                            if (debug) {
                                System.out.println(" reject");
                            }
                        }
                    }
                }
            }
        }

        // COMPUTE INITIAL NODES
        for (Pair<Node, Node> key : p_states.keySet()) {
            Node dcts_node = key.getFirst();
            Node ba_node = key.getSecond();
            if (dcts_node == dcts.getInit()) {
                for (Edge edge : ba_node.getIncomingEdges()) {
                    // BA from node must be in Q0 !!!
                    if (edge.getSource() == ba.getInit()) {
                        Conjunction conj = (Conjunction) edge
                                .getAttribute("parsedaction");
                        List<AtomicProposition> label = getLabel(dcts_node,
                                null);

                        if (conj.allows(label)) {
                            p.getInitialNodes().add(p_states.get(key));
                            continue;
                        }
                    }
                }
            }
        }

        return p;
    }

    @SuppressWarnings("unchecked")
    private static List<AtomicProposition> getLabel(Node dcts_to, Edge dcts_edge) {
        List<AtomicProposition> dcts_to_label = (List<AtomicProposition>) dcts_to
                .getAttribute("label");
        if (dcts_to_label == null) {
            dcts_to_label = Collections.singletonList(new AtomicProposition(
                    dcts_edge.getAction(),
                    AtomicProposition.PropositionType.Unknown));
        }
        return dcts_to_label;
    }
}
