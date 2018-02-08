package snapbuild.actions;
import snapbuild.app.*;
import java.util.*;
import snap.view.*;

/**
 * A custom class.
 */
public class SetText extends Action {

/**
 * Returns list of View classes to add.
 */
public List getItems()
{
    List items = new ArrayList();
    items.add("Okay");
    items.add("Cancel");
    items.add("Custom...");
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
    
    // Get amount and fix
    if(str.equals("Custom...")) {
        str = DialogBox.showInputDialog(epane.getUI(), "Set Text Panel", "Enter text:", null);
        if(str==null) return;
    }
    else if(str.equals("None"))
        str = null;
    
    // Get selected view
    View sview = editor.getSelView();
    sview.setText(str);
}

/** Returns the category of action. */
public Type getType()  { return Type.Style; }

}