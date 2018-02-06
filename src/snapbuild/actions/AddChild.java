package snapbuild.actions;
import snapbuild.app.*;
import java.util.ArrayList;
import java.util.List;
import snap.view.*;

/**
 * A custom class.
 */
public class AddChild extends Action {

/**
 * Returns list of View classes to add.
 */
public List getItems()
{
    List items = new ArrayList();
    items.add(ColView.class);
    items.add(RowView.class);
    items.add(Label.class);
    items.add(Button.class);
    items.add(TextField.class);
    items.add(Slider.class);
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
    Object sitem = epane.getSelActionItem(); if(!(sitem instanceof Class)) return;
    
    // Create new view
    Class <View> cls = (Class)sitem;
    View view = null; try { view = cls.newInstance(); } catch(Exception e) { }
    configure(view);
    
    // Get selected view
    View sview = editor.getSelView();
    if(sview instanceof ChildView)
        ((ChildView)sview).addChild(view);
        
    // Select view
    editor.setSelView(view);
}

/**
 * Configure new view.
 */
void configure(View aView)
{
    // Handle Label
    if(aView instanceof Label) aView.setText("Label");
    
    // Handle Button
    else if(aView instanceof Button) aView.setText("Button");
    
    // Handle TextField
    else if(aView instanceof TextField) aView.setMinWidth(100);
    
    // Handle RowView
    else if(aView instanceof RowView) {
        aView.setPadding(4,4,4,4); ((RowView)aView).setSpacing(4); aView.setGrowWidth(true); }
    
    // Handle ColView
    else if(aView instanceof ColView) {
        aView.setPadding(4,4,4,4); ((ColView)aView).setSpacing(4); aView.setGrowHeight(true); }
}

/**
 * Standard toString implementation.
 */
public String toString()  { return "Add Child"; }

}