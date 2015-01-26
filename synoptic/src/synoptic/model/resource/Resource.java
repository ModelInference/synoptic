package synoptic.model.resource;

/**
 * Represents a mined resource with the resource type and resource value for
 * Perfume. A resource is a key identifying the resource, and a number (any of
 * int, float, double etc)
 */
public class Resource<E extends Number & Comparable<E>> implements
        Comparable<Resource<E>> {

    /**
     * The resource type used to identify this resource
     */
    private String resourceType;

    /**
     * The value of this resource
     */
    private E resourceVal;

    public Resource(String type, E val) {
        resourceType = type;
        resourceVal = val;
    }

    /**
     * Compares two resources of the same type based on the resource value.
     */
    @Override
    public int compareTo(Resource<E> o) {
        assert (resourceType.equals(o.resourceType));
        return resourceVal.compareTo(o.resourceVal);
    }

    public String getResourceType() {
        return resourceType;
    }

    public E getResourceVal() {
        return resourceVal;
    }
}
