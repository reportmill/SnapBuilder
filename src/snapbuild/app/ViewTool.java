package snapbuild.app;
import snap.geom.Pos;
import snap.gfx.*;
import snap.view.*;

/**
 * A class to manage UI editing of a View.
 */
public class ViewTool<T extends View> extends ViewOwner {

    // The EditorPane
    protected EditorPane  _editorPane;

    /**
     * Returns the name.
     */
    public String getName()
    {
        Class cls = getClass();
        String clsName = cls.getSimpleName();
        return clsName.replace("Tool", "") + " Props";
    }

    /**
     * Returns the editor pane.
     */
    public EditorPane getEditorPane()  { return _editorPane; }

    /**
     * Returns the editor.
     */
    public Editor getEditor()  { return _editorPane.getEditor(); }

    /**
     * Returns the selected view.
     */
    public T getSelView()
    {
        return (T) getEditor().getSelView();
    }

    /**
     * Create UI.
     */
    protected View createUI()
    {
        // Subclasses should have UI file
        if (getClass() != ViewTool.class)
            return super.createUI();

        // This root class just returns a label
        Label label = new Label(getName());
        label.setAlign(Pos.CENTER);
        label.setFont(Font.Arial14);
        label.setTextColor(Color.LIGHTGRAY);
        return label;
    }
}