package synoptic.util;

import java.util.Iterator;

/**
 * Makes iterators easily usable in for each loops. From
 * http://www.rittau.org/blog/20061122-00. This interface provides a name for
 * the union of Iterator and Iterable interfaces and can be used in anonymous
 * class definitions
 * 
 * @param <T>
 *            The Type being of the collection which is iterated over.
 */
public interface IIterableIterator<T> extends Iterator<T>, Iterable<T> {
    // This empty interface acts as shorthand for Iterator/Iterable only.
}
