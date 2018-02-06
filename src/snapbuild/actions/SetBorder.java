package snapbuild.actions;
import snapbuild.app.*;
import java.util.*;
import snap.gfx.*;
import snap.gfx.Border.*;
import snap.view.*;

/**
 * A custom class.
 */
public class SetBorder extends Action {

/**
 * Returns list of View classes to add.
 */
public List getItems()
{
    List items = new ArrayList();
    items.add("White");
    items.add("Gray");
    items.add("Black");
    items.add("Bevel Raised");
    items.add("Bevel Lowered");
    items.add("Etched");
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
    
    // Get selected view
    View sview = editor.getSelView();
    Border bdr = sview.getBorder();
    
    // Get border
    Border border = getBorder(str);
    if(border instanceof LineBorder && bdr instanceof LineBorder)
        border = new LineBorder(((LineBorder)border).getColor(), ((LineBorder)bdr).getWidth()+1);
    
    // Set border
    sview.setBorder(border);
}

/**
 * Returns border for string.
 */
Border getBorder(String aStr)
{
    switch(aStr) {
        case "White": return new LineBorder(Color.WHITE, 1);
        case "Gray": return new LineBorder(Color.GRAY, 1);
        case "Black": return new LineBorder(Color.BLACK, 1);
        case "Bevel Raised": return new BevelBorder(1);
        case "Bevel Lowered": return new BevelBorder(0);
        case "Etched": return new EtchBorder();
        default: return null;
    }
}

}