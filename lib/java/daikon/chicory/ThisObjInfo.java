package daikon.chicory;

import java.util.*;

/**
 * The ThisObjInfo class is a subtype of DaikonVariableInfo used for
 * variable types which represent the "this" object.s
 */
public class ThisObjInfo extends DaikonVariableInfo
{
    public Class<?> type;

    public ThisObjInfo()
    {
        super("this");
    }

    public ThisObjInfo (Class<?> type)
    {
        super ("this");
        this.type = type;
    }

    /* (non-Javadoc)
     * @see daikon.chicory.DaikonVariableInfo#getChildValue(java.lang.Object)
     */
    @Override
    public /*@Nullable*/ Object getMyValFromParentVal(Object val)
    {
        throw new Error("shouldn't be called");
    }

    /** 'this' is a top level variable **/
    public VarKind get_var_kind() {
        return VarKind.VARIABLE;
    }

    /** Add IS_PARM to list of variable flags **/
    public EnumSet<VarFlags> get_var_flags() {
      // System.out.printf ("%s is a parameter%n", this);
      EnumSet<VarFlags> var_flags = super.get_var_flags().clone();
      var_flags.add (VarFlags.IS_PARAM);
      return (var_flags);
    }

}
