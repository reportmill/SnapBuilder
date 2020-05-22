package snapbuild.app;
import snap.geom.Pos;
import snap.gfx.*;
import snap.view.*;

/**
 * An class to manage UI editing of a View.
 */
public class ViewTool <T extends View> extends ViewOwner {

    // The EditorPane
    EditorPane         _epane;
    
/**
 * Returns the name.
 */
public String getName()  { return getClass().getSimpleName().replace("Tool","") + " Props"; }

/**
 * Returns the editor pane.
 */
public EditorPane getEditorPane()  { return _epane; }

/**
 * Returns the editor.
 */
public Editor getEditor()  { return _epane.getEditor(); }

/**
 * Returns the selected view.
 */
public T getSelView()  { return (T)getEditor().getSelOrSuperSelView(); }

/**
 * Create UI.
 */
protected View createUI()
{
    View ui = super.createUI();
    if(ui!=null) return ui;
    
    Label label = new Label(getName());
    label.setAlign(Pos.CENTER);
    label.setFont(Font.Arial14);
    label.setTextFill(Color.LIGHTGRAY);
    return label;
}

}