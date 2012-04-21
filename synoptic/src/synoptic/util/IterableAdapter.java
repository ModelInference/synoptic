package synoptic.util;

import java.util.Iterator;

/**
 * Makes iterators easily usable in for each loops. From
 * http://www.rittau.org/blog/20061122-00. This interface provides a name for
 * the union of Iterator and Iterable interfaces and can be used in anonymous
 * class definitions
 * 
 * @author sigurd
 * @param <T>
 *            The Type being of the collection which is iterated over.
 */
public class IterableAdapter<T> implements IIterableIterator<T> {
    private final Iterator<T> iterator;

    public IterableAdapter(Iterator<T> iterator) {
        this.iterator = iterator;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public T next() {
        return iterator.next();
    }

    @Override
    public void remove() {
        iterator.remove();
    }

    @Override
    public Iterator<T> iterator() {
        return iterator;
    }

    public static <T> IIterableIterator<T> make(Iterator<T> iterator) {
        return new IterableAdapter<T>(iterator);
    }
}
