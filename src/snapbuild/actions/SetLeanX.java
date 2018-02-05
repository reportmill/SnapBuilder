package snapbuild.actions;
import snapbuild.app.*;
import java.util.*;
import snap.gfx.HPos;
import snap.view.View;

/**
 * A custom class.
 */
public class SetLeanX extends Action {

/**
 * Returns list of View classes to add.
 */
public List getItems()
{
    List items = new ArrayList();
    items.add("LEFT");
    items.add("CENTER");
    items.add("RIGHT");
    items.add("None");
    return items;
}

/**
 * Adds a child view to current view.
 */
public void invoke()
{
    // Get editor, editorpane
    Editor editor = getEditor();
    EditorPane epane = getEditorPane();
    
    // Get selected item
    Object sitem = epane.getSelActionItem(); if(!(sitem instanceof String)) return;
    String str = (String)sitem;
    HPos lean = !str.equals("None")? HPos.valueOf(str) : null;
    
    // Get selected view
    View sview = editor.getSelView();
    sview.setLeanX(lean);
}

}