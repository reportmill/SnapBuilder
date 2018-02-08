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
    items.add(CheckBox.class);
    items.add(RadioButton.class);
    items.add(ComboBox.class);
    items.add(ThumbWheel.class);
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
    Object sitem = epane.getSelActionItem(); if(!(sitem instanceof Class)) return;
    
    // Create new view
    Class <View> cls = (Class)sitem;
    View view = null; try { view = cls.newInstance(); } catch(Exception e) { }
    ViewHpr.getHpr(view).configure(view);
    
    // Get selected view
    View sview = editor.getSelView();
    if(sview instanceof ChildView)
        ((ChildView)sview).addChild(view);
        
    // Select view
    editor.setSelView(view);
}

/**
 * Standard toString implementation.
 */
public String toString()  { return "Add Child"; }

/** Override to invoke action on single click. */
public boolean invokeOnClick()  { return false; }

/** Returns the category of action. */
public Type getType()  { return Type.Child; }

}