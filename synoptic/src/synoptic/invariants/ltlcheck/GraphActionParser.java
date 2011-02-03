package synoptic.invariants.ltlcheck;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import gov.nasa.ltl.graph.Edge;
import gov.nasa.ltl.graph.Graph;
import gov.nasa.ltl.graph.Node;

public final class GraphActionParser {

    private static boolean incomingOutgoingInvariant(Graph g) {
        Set<Edge> outgoing = new HashSet<Edge>();
        Set<Edge> incoming = new HashSet<Edge>();
        int outgoingCount = 0;
        int incomingCount = 0;

        for (Node n : g.getNodes()) {
            outgoingCount += n.getOutgoingEdgeCount();
            incomingCount += n.getIncomingEdgeCount();
            outgoing.addAll(n.getOutgoingEdges());
            incoming.addAll(n.getIncomingEdges());
        }

        assert incomingCount == outgoingCount;
        assert outgoing.equals(incoming);
        return true;
    }

    public static void parseTransitions(Graph g) {
        assert incomingOutgoingInvariant(g);

        for (Node n : g.getNodes()) {
            for (Edge e : n.getOutgoingEdges()) {
                Conjunction pa = parse(e.getGuard());
                e.setAttribute("parsedaction", pa);
            }
        }
    }

    // Parse the simple formulas on NBA transitions, e.g. !a&b&!c
    private static Conjunction parse(String formula) {
        Conjunction c = new Conjunction();

        if (!formula.equals("-")) {
            StringTokenizer tok = new StringTokenizer(formula, "&");
            while (tok.hasMoreTokens()) {
                String token = tok.nextToken();
                Literal l = new Literal();

                if (token.charAt(0) == '!') {
                    l.setPositive(false);
                    token = token.substring(1);
                }

                AtomicProposition atom = new AtomicProposition();
                if (token.toLowerCase().startsWith("can(")
                        && token.substring(token.length() - 1).equals(")")) {
                    atom.setType(AtomicProposition.PropositionType.Can);
                    atom.setAtom(token.substring(4, token.length() - 1));
                } else if (token.toLowerCase().startsWith("did(")
                        && token.substring(token.length() - 1).equals(")")) {
                    atom.setType(AtomicProposition.PropositionType.Did);
                    atom.setAtom(token.substring(4, token.length() - 1));
                } else if (token.toLowerCase().equals("true")) {
                    atom.setType(AtomicProposition.PropositionType.True);
                } else if (token.toLowerCase().equals("false")) {
                    atom.setType(AtomicProposition.PropositionType.False);
                } else {
                    atom.setAtom(token);
                }

                l.setAtom(atom);
                c.add(l);
            }
        }

        return c;
    }
}
