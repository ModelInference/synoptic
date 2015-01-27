package synoptic.model.resource;

/**
 * A non-time resource with its type and value
 */
public class Resource<E extends Number & Comparable<E>> implements IResource<E> {

    private String resourceType;
    private E resourceVal;

    public Resource(String type, E value) {
        resourceType = type;
        resourceVal = value;
    }

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
