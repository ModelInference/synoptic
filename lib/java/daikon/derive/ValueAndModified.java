package daikon.derive;

import daikon.*;

import utilMDE.*;

/**
 * This is a temporary structure for grouping elements to be returned
 * from computeValueAndModified, not for permanent storage.
 **/

public final class ValueAndModified {
  // The constructor checks that it is interned, contradicting this comment.
  public /*@Nullable*/ Object value;     // not necessarily an interned value
  public int modified;

  public static final ValueAndModified MISSING_NONSENSICAL
    = new ValueAndModified(null, ValueTuple.MISSING_NONSENSICAL);

  public static final ValueAndModified MISSING_FLOW
    = new ValueAndModified(null, ValueTuple.MISSING_FLOW);

  public ValueAndModified(/*@Nullable*/ Object val, int mod) {
    assert Intern.isInterned(val);
    // Type should be Long, not Integer
    assert ! (val instanceof Integer);
    value = val;
    modified = mod;
  }
}
