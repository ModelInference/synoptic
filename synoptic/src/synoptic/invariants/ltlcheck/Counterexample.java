package synoptic.invariants.ltlcheck;

import java.util.List;

import gov.nasa.ltl.graph.Edge;

public class Counterexample {
    private final List<Edge> prefix;
    private final List<Edge> cycle;

    public Counterexample(List<Edge> prefix, List<Edge> cycle) {
        this.prefix = prefix;
        this.cycle = cycle;
    }

    @Override
    public String toString() {
        boolean first = true;
        StringBuilder sb = new StringBuilder();

        sb.append("Prefix: ");
        for (Edge e : prefix) {
            sb.append((first ? "" : ", ") + e.getGuard());
            first = false;
        }
        sb.append('\n');

        first = true;
        sb.append("Cycle: ");
        for (Edge e : cycle) {
            sb.append((first ? "" : ", ") + e.getGuard());
            first = false;
        }
        sb.append('\n');

        return sb.toString();
    }

    public List<Edge> getPrefix() {
        return prefix;
    }

    public List<Edge> getCycle() {
        return cycle;
    }

    public String[] getTrace() {
        int i = 0;
        String str[] = new String[prefix.size() + cycle.size()];

        for (Edge e : prefix) {
            str[i] = e.getGuard();
            i++;
        }

        for (Edge e : cycle) {
            str[i] = e.getGuard();
            i++;
        }

        return str;
    }
}
