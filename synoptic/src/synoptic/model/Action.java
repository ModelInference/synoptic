package synoptic.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import synoptic.main.Main;
import synoptic.util.VectorTime;

/**
 * The action class abstracts an event. Each event needs at least a name, called
 * a label. Optionally, a vector time and data fields can set. If data fields
 * will be used, {@code useDatafields} must be set before compilation.
 * 
 * @author Sigurd Schneider
 */
public class Action {
    /**
     * The action's label.
     */
    String label;
    /**
     * Cache for the hash code of this object.
     */
    private Integer cachedHashCode = null;
    /**
     * The time this action occurred.
     */
    private VectorTime vectorTime;
    /**
     * A map to ensure each Action object is unique.
     */
    private static LinkedHashMap<Action, Action> internMap = new LinkedHashMap<Action, Action>();
    /**
     * Set this to true if you want equals and hash-code to respect the contents
     * of stringArgumens.
     */
    private final static boolean useDatafields = true;

    /**
     * The map that stores the arguments and their values. Arguments are named
     * properties.
     */
    Map<String, String> stringArguments = new LinkedHashMap<String, String>();

    /**
     * Create an action with a label. Do not check for collisions with
     * internally used labels.
     * 
     * @param label
     *            the label for the action
     * @param dummy
     *            unused
     */
    public Action(String label, boolean dummy) {
        this.label = label;
        computeHashCode();
    }

    /**
     * Create an action with a label.
     * 
     * @param label
     *            the label for the action
     */
    public Action(String label) {
        this(label, true);
        // TODO: translate labels so that collisions such as this do not occur.
        if (label.equals(Main.initialNodeLabel)
                || label.equals(Main.terminalNodeLabel)) {
            throw new IllegalArgumentException(
                    "Cannot create a node with label '"
                            + label
                            + "' because it conflicts with internal INITIAL/TERMINAL Synoptic labels.");
        }
    }

    /**
     * Returns the special initial action.
     */
    public static Action NewInitialAction() {
        return new Action(Main.initialNodeLabel, true);
    }

    /**
     * Returns the special terminal action.
     */
    public static Action NewTerminalAction() {
        return new Action(Main.terminalNodeLabel, true);
    }

    @Override
    public String toString() {
        return label + "-" + vectorTime.toString() + "-"
                + stringArguments.toString();
    }

    /**
     * Compute the hash code. This method should be called whenever the internal
     * representation changes.
     * 
     * @return the new hash code.
     */
    private int computeHashCode() {
        final int prime = 31;
        cachedHashCode = prime + (label == null ? 0 : label.hashCode());
        if (useDatafields) {
            if (vectorTime != null) {
                cachedHashCode += prime * vectorTime.hashCode();
            }
            cachedHashCode += 7 * prime * stringArguments.hashCode();
        }
        return cachedHashCode;
    }

    @Override
    public int hashCode() {
        return cachedHashCode;
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
        Action other = (Action) obj;
        if (label == null) {
            if (other.label != null) {
                return false;
            }
        } else if (!label.equals(other.label)) {
            return false;
        }
        if (useDatafields) {
            if (vectorTime == null && other.vectorTime != null) {
                return false;
            }
            if (vectorTime != null && !vectorTime.equals(other.vectorTime)) {
                return false;
            }
            if (!stringArguments.equals(other.stringArguments)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get the label of the action.
     * 
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Set the time when this action occurred.
     * 
     * @param vectorTime
     *            the time
     */

    public void setTime(VectorTime vectorTime) {
        this.vectorTime = vectorTime;
        computeHashCode();
    }

    /**
     * Get the vector time of this action.
     * 
     * @return the vector time when this action occurred.
     */
    public VectorTime getTime() {
        return vectorTime;
    }

    /**
     * Return the internal map that stores the arguments.
     * 
     * @return the internal map
     */
    public Map<String, String> getStringArguments() {
        return stringArguments;
    }

    /**
     * Get all names for which we have argument values set.
     * 
     * @return the names
     */
    public Set<String> getStringArgumentNames() {
        return stringArguments.keySet();
    }

    /**
     * Intern this object. Depending on whether {@code useDatafields} is set,
     * the action's time and arguments will be taken into account.
     * 
     * @return the interned action
     */
    public Action intern() {
        if (internMap.containsKey(this)) {
            return internMap.get(this);
        }
        internMap.put(this, this);
        return this;
    }

    /**
     * Set a string argument.
     * 
     * @param name
     *            name of the argument
     * @param value
     *            value of the argument
     */
    public void setStringArgument(String name, String value) {
        stringArguments.put(name, value);
        computeHashCode();
    }

    /**
     * Retrieve an argument value.
     * 
     * @param name
     *            the name of the argument
     * @return its value
     */
    public String getStringArgument(String name) {
        return stringArguments.get(name);
    }

    /**
     * Add all arguments from {@code action} to this action's arguments,
     * possibly overwriting arguments of this action.
     * 
     * @param action
     *            the action to read the additional arguments form.
     */
    public void mergeFromAction(Action action) {
        stringArguments.putAll(action.stringArguments);
        computeHashCode();
    }

}
