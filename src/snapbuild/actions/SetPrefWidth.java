package snapbuild.actions;
import snapbuild.app.*;
import java.util.*;
import snap.util.SnapUtils;
import snap.view.View;

/**
 * A custom class.
 */
public class SetPrefWidth extends Action {

/**
 * Returns list of View classes to add.
 */
public List getItems()
{
    List items = new ArrayList();
    items.add(" +1");
    items.add(" +2");
    items.add(" +5");
    items.add("  ");
    items.add(" -1");
    items.add(" -2");
    items.add(" -5");
    items.add("  ");
    items.add("Clear");
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
    int amt = SnapUtils.intValue(str);
    if(str.equals("Clear")) amt = -9999;
    
    // Get selected view
    View sview = editor.getSelView();
    double pw = sview.getPrefWidth() + amt;
    sview.setPrefWidth(pw>=0? pw : -1);
}

/** Returns the category of action. */
public Type getType()  { return Type.Bounds; }

}