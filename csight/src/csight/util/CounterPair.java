package csight.util;

public class CounterPair<K> implements Comparable<CounterPair<K>> {

    private K key;
    private Integer value;

    public CounterPair(K key, Integer value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public int compareTo(CounterPair<K> o) {
        return o.getValue() - this.value;
    }

    public Integer getValue() {
        return value;
    }

    public K getKey() {
        return key;
    }

    @Override
    public String toString() {
        return "[ " + key + "," + value + "]";
    }
}
