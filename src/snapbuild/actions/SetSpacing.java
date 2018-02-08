package snapbuild.actions;
import snapbuild.app.*;
import java.util.*;
import snap.util.SnapUtils;
import snap.view.*;

/**
 * A custom class.
 */
public class SetSpacing extends Action {

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
    
    // Get amount and fix
    int amt = SnapUtils.intValue(sitem);
    
    // Get selected view
    View sview = editor.getSelView();
    if(sview instanceof Label) ((Label)sview).setSpacing(((Label)sview).getSpacing()+amt);
    if(sview instanceof RowView) ((RowView)sview).setSpacing(((RowView)sview).getSpacing()+amt);
    if(sview instanceof ColView) ((ColView)sview).setSpacing(((ColView)sview).getSpacing()+amt);
}

/** Returns the category of action. */
public Type getType()  { return Type.Bounds; }

}