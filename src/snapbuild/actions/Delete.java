package snapbuild.actions;
import snapbuild.app.*;
import snap.view.*;

/**
 * A custom class.
 */
public class Delete extends Action {

/**
 * Returns whether action has items.
 */
public boolean hasItems()  { return false; }

/**
 * Adds a child view to current view.
 */
public void invoke(EditorPane epane)
{
    // Get editor, editorpane
    Editor editor = epane.getEditor();
    
    // Get selected view
    View sview = editor.getSelView();
    ParentView par = sview.getParent();
    int ind = sview.indexInParent();
    if(par instanceof ChildView) { ChildView cview = (ChildView)par;
        cview.removeChild(sview);
        if(cview.getChildCount()>0)
            editor.setSelView(ind<cview.getChildCount()? cview.getChild(ind) : cview.getChild(ind-1));
        else editor.setSelView(cview);
    }
}

}