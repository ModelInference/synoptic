package synoptic.util.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates a list of resource instances of the same type as identified by
 * the key that extend AbstractResource.
 */
public class ResourceSeries<ResourceType extends AbstractResource> implements
        Comparable<ResourceSeries<ResourceType>> {
    private List<ResourceType> resources;
    boolean isSorted;
    private final String key;

    public ResourceSeries() {
        resources = new ArrayList<ResourceType>();
        isSorted = true;
        key = "";
    }

    public ResourceSeries(String key) {
        resources = new ArrayList<ResourceType>();
        isSorted = true;
        this.key = key;
    }

    /**
     * If the resource list is not sorted, then sorts the list in-place.
     */
    private void sort() {
        if (!isSorted) {
            Collections.sort(resources);
            isSorted = true;
        }
    }

    /**
     * @return mode delta resource for transition, null if transition has zero
     *         delta resources.
     */
    public ResourceType computeMode() {

        if (this.resources.isEmpty()) {
            return null;
        }

        Map<ResourceType, Integer> counts = new HashMap<ResourceType, Integer>();
        ResourceType mostCommon = null;
        int max = 0;
        for (ResourceType delta : resources) {
            Integer count = counts.get(delta);

            if (count == null) {
                count = 1;
            } else {
                count++;
            }

            if (count > max) {
                mostCommon = delta;
                max = count;
            }

            counts.put(delta, count);
        }

        return mostCommon;
    }

    /**
     * @return median delta resource for transition, null if transition has zero
     *         delta resources.
     */
    @SuppressWarnings("unchecked")
    public ResourceType computeMedian() {
        if (this.resources.isEmpty()) {
            return null;
        }

        // Sort the list.
        sort();

        int middle = resources.size() / 2;
        if (resources.size() % 2 == 1) {
            // Odd length.
            return resources.get(middle);
        }
        // Event length.
        // TODO: make this a safe cast by having incrBy return a more concrete
        // time than AbstractResource.
        return (ResourceType) resources.get(middle - 1)
                .incrBy(resources.get(middle)).divBy(2);
    }

    /**
     * @return mean delta resource for transition, null if transition has zero
     *         delta resources.
     */
    @SuppressWarnings("unchecked")
    public ResourceType computeMean() {
        if (resources.isEmpty()) {
            return null;
        }

        // Create a zero valued starting point.
        ResourceType initial = (ResourceType) resources.get(0)
                .getZeroResource();

        for (ResourceType r : resources) {
            initial = (ResourceType) initial.incrBy(r);
        }

        return (ResourceType) initial.divBy(resources.size());
    }

    /**
     * @return Minimum resource delta for transition, or null if transition has
     *         no resource deltas
     */
    public ResourceType computeMin() {
        return computeMinMax(false);
    }

    /**
     * @return Maximum resource delta for transition, or null if transition has
     *         no resource deltas
     */
    public ResourceType computeMax() {
        return computeMinMax(true);
    }

    /**
     * @return Median resource delta for transition, or null if transition has
     *         no resource deltas
     */
    @SuppressWarnings("unchecked")
    public ResourceType computeMed() {
        // Check for empty or size-one resource series
        if (resources.isEmpty()) {
            return null;
        } else if (resources.size() == 1) {
            return resources.get(0);
        }

        // Median position if odd, or lower median position if even
        int medianPos = (resources.size() - 1) / 2;

        // Resources size is even, so calculate and return median
        if (resources.size() % 2 == 0) {
            ResourceType lowMedian = resources.get(medianPos);
            ResourceType highMedian = resources.get(medianPos + 1);
            return (ResourceType) lowMedian.incrBy(highMedian).divBy(2);
        }

        // Resources size is odd, so just return median
        {
            return resources.get(medianPos);
        }
    }

    /**
     * @param findMax
     *            If true, find max. If false, find min.
     * @return Minimum or maximum resource delta
     */
    private ResourceType computeMinMax(boolean findMax) {
        // Check for empty resource series
        if (resources.isEmpty()) {
            return null;
        }

        // Start the running min/max resource with the first resource delta
        ResourceType minMaxResource = resources.get(0);

        // Find max resource
        if (findMax) {
            for (ResourceType r : resources) {
                if (minMaxResource.lessThan(r)) {
                    minMaxResource = r;
                }
            }
        }

        // Find min resource
        else {
            for (ResourceType r : resources) {
                if (r.lessThan(minMaxResource)) {
                    minMaxResource = r;
                }
            }
        }

        return minMaxResource;
    }

    /**
     * Adds a resource for the transition between the source and target nodes.
     * 
     * @param delta
     *            The resource between nodes.
     */
    public void addDelta(ResourceType r) {
        assert r != null;
        if (!r.key.equals(key)) {
            throw new WrongResourceTypeException(key, r);
        }
        resources.add(r);
        isSorted = false;
    }

    /**
     * Adds a collection of resources for transition between source and target
     * nodes.
     * 
     * @param deltas
     */
    public void addAllDeltas(Collection<ResourceType> deltas) {
        assert deltas != null;
        for (ResourceType delta : deltas) {
            if (!delta.key.equals(key)) {
                throw new WrongResourceTypeException(key, delta);
            }
        }
        resources.addAll(deltas);
    }

    public List<ResourceType> getAllDeltas() {
        return resources;
    }

    public String getKey() {
        return key;
    }

    @Override
    public int compareTo(ResourceSeries<ResourceType> o) {
        int cmp;

        sort();
        o.sort();
        cmp = ((Integer) resources.size()).compareTo(o.resources.size());
        if (cmp != 0) {
            return cmp;
        }

        int i = 0;
        for (AbstractResource r : resources) {
            cmp = r.compareTo(o.resources.get(i));
            if (cmp != 0) {
                return cmp;
            }
            i += 1;
        }
        return 0;
    }
}
