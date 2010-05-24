package daikon.chicory;


/**
 * A subtype of DaikonVariableInfo used for variables that are
 * returned from procedures.
 */
public class ReturnInfo extends DaikonVariableInfo
{
    // Under what circumstances is this null?  Maybe it's unused. -MDE
//    Class<?> return_type = null;

    public ReturnInfo()
    {
        super("return");
    }

    public ReturnInfo (Class<?> return_type)
    {
        super("return");
//        this.return_type = return_type;
    }

    @Override
    public Object getMyValFromParentVal(Object value)
    {
        throw new RuntimeException("Don't call getMyValFromParentVal on ReturnInfo objects");
    }

    public VarKind get_var_kind() {
        return VarKind.RETURN;
    }
}
