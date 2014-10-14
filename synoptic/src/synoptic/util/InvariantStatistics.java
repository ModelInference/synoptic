package synoptic.util;

public class InvariantStatistics {
    public int supportCount;

    public InvariantStatistics(int supportCount) {
        this.supportCount = supportCount;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + supportCount;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        InvariantStatistics other = (InvariantStatistics) obj;
        if (supportCount != other.supportCount)
            return false;
        return true;
    }

}
