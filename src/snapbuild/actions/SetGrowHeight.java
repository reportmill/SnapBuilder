package snapbuild.actions;
import snapbuild.app.*;
import java.util.*;
import snap.util.SnapUtils;
import snap.view.View;

/**
 * A custom class.
 */
public class SetGrowHeight extends Action {

/**
 * Returns list of View classes to add.
 */
public List getItems()
{
    List items = new ArrayList();
    items.add("true");
    items.add("false");
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
    boolean val = SnapUtils.boolValue(sitem);
    
    // Get selected view
    View sview = editor.getSelView();
    sview.setGrowHeight(val);
}

}