package snapbuild.app;
import snapbuild.actions.*;
import java.util.*;

/**
 * A custom class.
 */
public abstract class Action {
    
    // The editor
    Editor           _editor;
    
    // The EditorPane
    EditorPane       _edPane;
    
    // Actions for specific kinds of views

/**
 * Returns the action name.
 */
public String getName()  { return getClass().getSimpleName(); }

/**
 * Returns the editor.
 */
public Editor getEditor()  { return _editor; }

/**
 * Returns the editor pane.
 */
public EditorPane getEditorPane()  { return _edPane; }

/**
 * Returns whether action has items.
 */
public boolean hasItems()  { return true; }

/**
 * Returns the item array.
 */
public List getItems()  { return Collections.EMPTY_LIST; }

/**
 * Returns the item as array.
 */
public Object[] getItemArray()  { return getItems().toArray(new Object[0]); }

/**
 * Invokes the action.
 */
public void invoke()  { }

/**
 * Returns actions for View.
 */
public static List <Action> getViewActions(EditorPane anEP)
{
    List <Action> list = new ArrayList();
    Collections.addAll(list, new AddPeer(), new SetGrowWidth(), new SetGrowHeight(), new SetLeanX(), new Delete(),
        new Duplicate());
    for(Action a : list) { a._edPane = anEP; a._editor = anEP.getEditor(); }
    return list;
}

/**
 * Returns actions for View.
 */
public static List <Action> getChildViewActions(EditorPane anEP)
{
    List <Action> list = new ArrayList();
    Collections.addAll(list, new AddChild(), new AddPeer(), new SetPadding(), new SetSpacing(), new SetBorder(),
     new SetGrowWidth(), new SetGrowHeight(), new Delete(), new Duplicate());
    for(Action a : list) { a._edPane = anEP; a._editor = anEP.getEditor(); }
    return list;
}

}