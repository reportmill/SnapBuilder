package snapbuild.actions;
import snapbuild.app.*;
import snap.view.*;

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
    // Get editor, editorpane
    Editor editor = epane.getEditor();
    
    // Get selected view
    View sview = editor.getSelView();
    ParentView par = sview.getParent();
    int ind = sview.indexInParent();
    
    View dup = new ViewArchiver().copy(sview);
    if(dup==null)
        dup = new ViewArchiver().copy(sview);
    if(par instanceof ChildView)
        ((ChildView)par).addChild(dup);
    editor.setSelView(dup);
}

/** Returns the category of action. */
public Type getType()  { return Type.Child; }

}