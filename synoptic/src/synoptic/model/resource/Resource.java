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
    public int compareTo(IResource<E> o) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getResourceType() {
        return resourceType;
    }

    @Override
    public E getResourceVal() {
        return resourceVal;
    }
    //

}
