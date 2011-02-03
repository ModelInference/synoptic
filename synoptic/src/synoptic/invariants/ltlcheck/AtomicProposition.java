package synoptic.invariants.ltlcheck;

public class AtomicProposition {
    public enum PropositionType {
        Can, Did, True, False, Unknown
    }

    private PropositionType type = PropositionType.Unknown;
    private String atom = "";

    public AtomicProposition() {
        super();
    }

    public AtomicProposition(String atom, PropositionType type) {
        super();
        this.atom = atom;
        this.type = type;
    }

    public PropositionType getType() {
        return type;
    }

    public void setType(PropositionType type) {
        this.type = type;
    }

    public String getAtom() {
        return atom;
    }

    public void setAtom(String atom) {
        this.atom = atom;
    }

    @Override
    public String toString() {
        String ret = "";
        if (type == PropositionType.Can) {
            ret += "can(";
        } else if (type == PropositionType.Did) {
            ret += "did(";
        }
        ret += atom;
        if (type != PropositionType.Unknown) {
            ret += ")";
        }
        return ret;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (atom == null ? 0 : atom.hashCode());
        result = prime * result + (type == null ? 0 : type.hashCode());
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
        final AtomicProposition other = (AtomicProposition) obj;
        if (atom == null) {
            if (other.atom != null) {
                return false;
            }
        } else if (!atom.equals(other.atom)) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        return true;
    }
}
