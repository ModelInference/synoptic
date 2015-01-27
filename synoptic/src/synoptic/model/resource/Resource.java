package synoptic.model.resource;

/**
 * A numerical resource with its type and value
 */
public class Resource<E extends Number & Comparable<E>> implements IResource<E> {

    /**
     * The string identifying the resource type
     */
    private String resourceType;

    /**
     * The numberical value of the resource
     */
    private E resourceVal;

    public Resource(String type, E value) {
        resourceType = type;
        resourceVal = value;
    }

    /**
     * Compares this resource to another resource of the same resourceType
     * Returns a value smaller than 0 if this resource is smaller than the
     * other, larger than 0 if this resource is larger than the other, or 0 if
     * this resource is equal to the other.
     * 
     * @return
     */
    @Override
    public int compareTo(IResource<E> res) {
        if (!(res instanceof Resource<?>)) {
            throw new NonComparableResourceException(this, res);
        }
        Resource<E> resource = (Resource<E>) res;
        if (!(resourceType.equals(resource.resourceType))) {
            throw new NonComparableResourceException(this, res);
        }
        return resourceVal.compareTo(resource.resourceVal);
    }

    @Override
    public String getResourceType() {
        return resourceType;
    }

    @Override
    public E getResourceVal() {
        return resourceVal;
    }

}
