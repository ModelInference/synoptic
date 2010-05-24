package daikon.inv.unary.stringsequence;

import daikon.*;
import daikon.inv.*;
import utilMDE.*;


/**
 * Represents string sequences that contain a common subset.  Prints as
 * "{s1, s2, s3, ...} subset of x[]".
 **/
public class CommonStringSequence
  extends SingleStringSequence
{
  // We are Serializable, so we specify a version to allow changes to
  // method signatures without breaking serialization.  If you add or
  // remove fields, you should change this number to the current date.
  static final long serialVersionUID = 20030822L;

  // Variables starting with dkconfig_ should only be set via the
  // daikon.config.Configuration interface.
  /**
   * Boolean.  True iff CommonStringSequence invariants should be considered.
   **/
  public static boolean dkconfig_enabled = false;

  private int elts;
  /**
   * Null means no samples have been seen yet.
   * Empty array means intersection is empty.
   */
  private String /*@Nullable*/ [] intersect = null;

  protected CommonStringSequence(PptSlice ppt) {
    super(ppt);
  }

  protected /*@Prototype*/ CommonStringSequence() {
    super();
  }

  private static /*@Prototype*/ CommonStringSequence proto;

  /** Returns the prototype invariant for CommonStringSequence **/
  public static /*@Prototype*/ CommonStringSequence get_proto() {
    if (proto == null)
      proto = new /*@Prototype*/ CommonStringSequence ();
    return (proto);
  }

  /** returns whether or not this invariant is enabled **/
  public boolean enabled() {
    return dkconfig_enabled;
  }

  /** instantiate an invariant on the specified slice **/
  protected CommonStringSequence instantiate_dyn (PptSlice slice) /*@Prototype*/ {
    return new CommonStringSequence (slice);
  }

  // Don't write clone, because this.intersect is read-only
  // protected Object clone();

  public String repr() {
    return "CommonStringSequence " + varNames() + ": "
      + "elts=\"" + elts;
  }

  private String printIntersect() {
    if (intersect==null)
      return "{}";

    String result = "{";
    for (int i=0; i<intersect.length; i++) {
      result += intersect[i];
      if (i!=intersect.length-1)
        result += ", ";
    }
    result += "}";
    return result;
  }

  public String format_using(OutputFormat format) {
    if (format == OutputFormat.DAIKON) return format_daikon();

    return format_unimplemented(format);
  }

  public String format_daikon() {
    return (printIntersect() + " subset of " + var().name());
  }

  public InvariantStatus check_modified(String[] a, int count) {
    if (a == null) {
      return InvariantStatus.FALSIFIED;
    } else if (intersect==null) {
      return InvariantStatus.NO_CHANGE;
    } else {
      String[] tmp = new String[intersect.length];
      int    size = 0;
      for (int i=1; i<a.length; i++) {
        if ((ArraysMDE.indexOf(intersect, a[i])!=-1) &&
            ((size==0) ||
             (ArraysMDE.indexOf(ArraysMDE.subarray(tmp,0,size), a[i])==-1)))
          tmp[size++] = a[i];
      }

      if (size==0) {
        return InvariantStatus.FALSIFIED;
      }
    }
    return InvariantStatus.NO_CHANGE;
  }


  public InvariantStatus add_modified(String[] a, int count) {
    if (a == null) {
      return InvariantStatus.FALSIFIED;
    } else if (intersect==null) {
      intersect = Intern.intern(a);
      return InvariantStatus.NO_CHANGE;
    } else {
      String[] tmp = new String[intersect.length];
      int    size = 0;
      for (int i=1; i<a.length; i++) {
        if ((ArraysMDE.indexOf(intersect, a[i])!=-1) &&
            ((size==0) ||
             (ArraysMDE.indexOf(ArraysMDE.subarray(tmp,0,size), a[i])==-1)))
          tmp[size++] = a[i];
      }

      if (size==0) {
        return InvariantStatus.FALSIFIED;
      }
      intersect = Intern.intern(ArraysMDE.subarray(tmp, 0, size));
    }
    elts++;
    return InvariantStatus.NO_CHANGE;
  }

  protected double computeConfidence() {
    throw new Error("Not yet implemented");
  }

  public /*@Nullable*/ DiscardInfo isObviousImplied() {
    return null;
  }

  public boolean isSameFormula(Invariant other) {
    assert other instanceof CommonStringSequence;
    return true;
  }
}
