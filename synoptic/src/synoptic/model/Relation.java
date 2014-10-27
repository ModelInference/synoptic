package synoptic.model;

/**
 * Represents named and anonymous relations
 * 
 */
public class Relation {
    // The name used to describe an anonymous relation.
    public static final String anonName = "anon-relation";

    // A short descriptive and unique string representing this relation.
    private String relation;
    // Used to document the purpose of the relation.
    private String name;
    // Whether or not this relation is a closure relation (?)
    private boolean isClosure;

    public Relation(String name, String relation, boolean isClosure) {
        this.name = name;
        this.relation = relation;
        this.isClosure = isClosure;
    }

    public boolean isAnonymous() {
        return name.equals(anonName);
    }

    public boolean isClosure() {
        return isClosure;
    }

    public String getName() {
        return name;
    }

    public String getRelation() {
        return relation;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Relation)) {
            return false;
        }
        Relation oRelation = (Relation) o;
        return name.equals(oRelation.getName())
                && relation.equals(oRelation.getRelation())
                && isClosure == oRelation.isClosure();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + relation.hashCode();
        result = prime * result + name.hashCode();
        if (isClosure) {
            result = prime * result + prime;
        }
        return result;
    }

    @Override
    public String toString() {
        String result = name + " : " + relation.toString();

        if (isClosure()) {
            result += "*";
        }

        return result;
    }
}
