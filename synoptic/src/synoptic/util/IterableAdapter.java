package synoptic.util;

import java.util.Iterator;

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
