package snapbuild.actions;
import snapbuild.app.*;
import snap.view.*;

/**
 * A custom class.
 */
public class AddAfter extends AddChild {

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
    View par = sview.getParent();
    int ind = sview.indexInParent();
    ViewHpr.getHpr(par).addChild(par, view, ind+1);
        
    // Select view
    editor.setSelView(view);
}

}