package snapbuild.actions;
import snapbuild.app.*;

/**
 * A custom class.
 */
public class Duplicate extends Action {

/**
 * Returns whether action has items.
 */
public boolean hasItems()  { return false; }

/**
 * Adds a child view to current view.
 */
public void invoke(EditorPane epane)
{
    Editor editor = epane.getEditor();
    editor.copy();
    editor.paste();
}

/** Returns the category of action. */
public Type getType()  { return Type.Child; }

}