package synoptic.model.export.types;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class SynNode<T> {
    protected Set<T> elements;
    protected boolean isTerminal;

    public SynNode(Collection<T> elements) {
        this(elements, false);
    }

    public SynNode(Collection<T> elements, boolean isTerminal) {
        this.elements = new LinkedHashSet<>(elements);
        this.isTerminal = isTerminal;
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
}
