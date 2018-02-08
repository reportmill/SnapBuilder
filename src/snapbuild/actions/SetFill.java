package snapbuild.actions;
import snapbuild.app.*;
import java.util.*;
import snap.gfx.*;
import snap.view.*;

/**
 * A custom class.
 */
public class SetFill extends Action {

/**
 * Returns list of View classes to add.
 */
public List getItems()
{
    List items = new ArrayList();
    items.add("White");
    items.add("Gray");
    items.add("Black");
    items.add("Red");
    items.add("Green");
    items.add("Blue");
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
    
    // Get paint
    Paint paint = getFill(str);
    
    // Set border
    sview.setFill(paint);
}

/**
 * Returns border for string.
 */
Paint getFill(String aStr)
{
    switch(aStr) {
        case "White": return Color.WHITE;
        case "Gray": return Color.GRAY;
        case "Black": return Color.BLACK;
        case "Red": return new Color(1,.6,.6);
        case "Green": return new Color(.6,1,.6);
        case "Blue": return new Color(.6,.6,1);
        default: return null;
    }
}

/** Returns the category of action. */
public Type getType()  { return Type.Style; }

}