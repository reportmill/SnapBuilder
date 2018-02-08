package snapbuild.actions;
import snapbuild.app.*;
import java.util.*;
import snap.gfx.Insets;
import snap.util.SnapUtils;
import snap.view.View;

/**
 * A custom class.
 */
public class SetPadding extends Action {

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
    Insets ins = sview.getPadding();
    Insets ins2 = Insets.add(ins, amt, amt, amt, amt);
    sview.setPadding(ins2);
}

/** Returns the category of action. */
public Type getType()  { return Type.Bounds; }

}