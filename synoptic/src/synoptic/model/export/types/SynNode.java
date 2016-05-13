package synoptic.model.export.types;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class SynNode<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    protected Set<T> elements;
    protected boolean isTerminal;
    
    protected SynNode() {
        this(new LinkedHashSet<T>(), false);
    }

    public SynNode(Collection<T> elements) {
        this(elements, false);
    }

    public SynNode(Collection<T> elements, boolean isTerminal) {
        this.elements = new LinkedHashSet<>(elements);
        this.isTerminal = isTerminal;
    }

    protected void addElement(T elem) {
        elements.add(elem);
    }

    public Set<T> getElements() {
        return elements;
    }

    public void makeTerminal() {
        isTerminal = true;
    }

    public boolean isTerminal() {
        return isTerminal;
    }

    public boolean contains(T element) {
        return elements.contains(element);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(elements.toString());
        if (isTerminal) {
            sb.append(" (TERMINAL)");
        }
        return sb.toString();
    }
}
