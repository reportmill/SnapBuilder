package snapbuild.app;
import java.util.*;

/**
 * A custom class.
 */
public abstract class Action {
    
    // The category of action
    public enum Type { Bounds, Child, Style, Misc, Prop };
    
/**
 * Returns the action name.
 */
public String getName()  { return getClass().getSimpleName(); }

/**
 * Returns whether action has items.
 */
public boolean hasItems()  { return true; }

/**
 * Returns the item array.
 */
public List getItems()  { return Collections.EMPTY_LIST; }

/**
 * Returns the item as array.
 */
public Object[] getItemArray()  { return getItems().toArray(new Object[0]); }

/**
 * Invokes the action.
 */
public void invoke(EditorPane epane)  { }

/**
 * Whether to invoke action on single click.
 */
public boolean invokeOnClick()  { return true; }

/**
 * Returns the category of action.
 */
public Type getType()  { return Type.Misc; }

}