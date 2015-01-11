package synoptic.invariants.ltlcheck;

import java.util.ArrayList;
import java.util.List;

public class Conjunction {
    private final List<Literal> literals = new ArrayList<Literal>();

    public Conjunction() {
        super();
    }

    public boolean allows(List<AtomicProposition> atoms) {
        for (Literal l : literals) {
            if (l.getAtom().getType() == AtomicProposition.PropositionType.True) {
                continue;
            }
            if (l.getAtom().getType() == AtomicProposition.PropositionType.False
                    || l.isPositive()
                    && !atoms.contains(l.getAtom())
                    || !l.isPositive() && atoms.contains(l.getAtom())) {
                return false;
            }
        }
        return true;
    }

    public void add(Literal l) {
        literals.add(l);
    }

    public boolean isTrue() {
        return literals.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();

        if (!literals.isEmpty()) {
            for (Literal l : literals) {
                b.append(l.toString());
                b.append(" ");
            }
        } else {
            b.append("true");
        }

        return b.toString();
    }
}
