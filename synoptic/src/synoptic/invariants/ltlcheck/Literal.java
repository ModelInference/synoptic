package synoptic.invariants.ltlcheck;

public class Literal {
    private boolean positive = true;
    private AtomicProposition atom = null;

    public Literal() {
        super();
    }

    public Literal(AtomicProposition atom) {
        super();
        positive = true;
        this.atom = atom;
    }

    public Literal(AtomicProposition atom, boolean positive) {
        super();
        this.positive = positive;
        this.atom = atom;
    }

    public boolean isPositive() {
        return positive;
    }

    public void setPositive(boolean positive) {
        this.positive = positive;
    }

    public AtomicProposition getAtom() {
        return atom;
    }

    public void setAtom(AtomicProposition atom) {
        this.atom = atom;
    }

    @Override
    public String toString() {
        String ret = "";
        if (!positive) {
            ret += "!";
        }
        ret += atom.toString();
        return ret;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (atom == null ? 0 : atom.hashCode());
        result = prime * result + (positive ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Literal other = (Literal) obj;
        if (atom == null) {
            if (other.atom != null) {
                return false;
            }
        } else if (!atom.equals(other.atom)) {
            return false;
        }
        if (positive != other.positive) {
            return false;
        }
        return true;
    }
}
