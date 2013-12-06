package daikonizer;

/**
 * Represents a Daikon variable, which has a name and a type.
 * 
 * @author rsukkerd
 *
 */
public class DaikonVar {
	public String vname;
	public DaikonVarType vtype;
	
	public DaikonVar(String vname, DaikonVarType vtype) {
		this.vname = vname;
		this.vtype = vtype;
	}
	
	@Override
	public int hashCode() {
	    int ret = 31;
        ret = ret * 31 + vname.hashCode();
        ret = ret * 31 + vtype.hashCode();
        return ret;
	}
	
	@Override
	public boolean equals(Object obj) {
	    if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DaikonVar other = (DaikonVar) obj;
        return vname.equals(other.vname) && vtype.equals(other.vtype);
	}
	
	@Override
	public String toString() {
	    return vtype + " " + vname;
	}
}
