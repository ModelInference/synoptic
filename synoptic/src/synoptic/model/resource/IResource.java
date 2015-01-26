package synoptic.model.resource;

/**
 * Represents a mined resource with the resource type and resource value for
 * Perfume. A resource is a key identifying the resource, and a number (any of
 * int, float, double etc)
 */
public interface IResource<E extends Comparable<E>> extends
        Comparable<IResource<E>> {

    /**
     * Compares two resources of the same type based on the resource value.
     * 
     * @return
     */
    @Override
    int compareTo(IResource<E> o);

    String getResourceType();

    E getResourceVal();
}
