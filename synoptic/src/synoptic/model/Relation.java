package synoptic.model;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Represents named and anonymous relations
 * 
 * @author timjv
 */
public class Relation {

    public static final String anonName = "anon-relation";

    private Set<String> relations;
    private String name;
    private boolean isClosure;

    public Relation(String name, String relation, boolean isClosure) {
        this.name = name;
        this.relations = new LinkedHashSet<String>();
        this.relations.add(relation);
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

    public Set<String> getRelations() {
        return relations;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Relation)) {
            return false;
        }
        Relation oRelation = (Relation) o;
        return name.equals(oRelation.getName())
                && relations.equals(oRelation.getRelations())
                && isClosure == oRelation.isClosure();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + relations.hashCode();
        result = prime * result + name.hashCode();
        if (isClosure) {
            result = prime * result + prime;
        }
        return result;
    }

    @Override
    public String toString() {
        String result = name + " : " + relations.toString();

        if (isClosure()) {
            result += "*";
        }

        return result;
    }
}
