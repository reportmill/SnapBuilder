package snapbuild.actions;
import snapbuild.app.*;
import java.util.*;
import snap.gfx.VPos;
import snap.view.View;

/**
 * A custom class.
 */
public class SetLeanY extends Action {

/**
 * Returns list of View classes to add.
 */
public List getItems()
{
    List items = new ArrayList();
    items.add("TOP");
    items.add("CENTER");
    items.add("BOTTOM");
    items.add("None");
    return items;
}

/**
 * Adds a child view to current view.
 */
public void invoke(EditorPane epane)
{
    // Get editor, editorpane
    Editor editor = epane.getEditor();
    
    // Get selected item
    Object sitem = epane.getSelActionItem(); if(!(sitem instanceof String)) return;
    String str = (String)sitem;
    VPos lean = !str.equals("None")? VPos.valueOf(str) : null;
    
    // Get selected view
    View sview = editor.getSelView();
    sview.setLeanY(lean);
}

/** Returns the category of action. */
public Type getType()  { return Type.Bounds; }

}