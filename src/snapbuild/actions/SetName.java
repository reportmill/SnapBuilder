package snapbuild.actions;
import snapbuild.app.*;
import snap.view.*;

/**
 * A custom class.
 */
public class SetName extends Action {

/**
 * Returns whether action has items.
 */
public boolean hasItems()  { return false; }

/**
 * Adds a child view to current view.
 */
public void invoke(EditorPane epane)
{
    // Get editor, editorpane
    Editor editor = epane.getEditor();
    
    // Get selected view and default name
    View sview = editor.getSelView();
    String def = sview.getClass().getSimpleName();
    
    // Get amount and fix
    String name = DialogBox.showInputDialog(epane.getUI(), "Set Name Panel", "Enter name:", def);
    if(name==null) return;
    
    // Get selected view
    sview.setName(name);
}

/** Returns the category of action. */
public Type getType()  { return Type.Misc; }

}