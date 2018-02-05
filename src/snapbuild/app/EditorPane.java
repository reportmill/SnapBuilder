package snapbuild.app;
import java.util.List;
import snap.gfx.Color;
import snap.view.*;

/**
 * A class to manage the Editor and controls.
 */
public class EditorPane extends ViewOwner {
    
    // The Editor
    Editor           _editor;
    
    // A box to hold selection path
    RowView          _selPathBox;
    
    // The deepest view to show in the SelPathBox
    View             _selPathDeep;

    // The ViewTree
    TreeView <View>  _viewTree;
    
    // The ActionBrowser
    BrowserView      _actBrwsr;
    
    // The Top level actions
    List <Action>    _viewActions;
    List <Action>    _childViewActions;

/**
 * Returns the editor.
 */
public Editor getEditor()  { return _editor; }

/**
 * Returns the top selected action.
 */
public Action getSelAction()
{
    int cind = _actBrwsr.getSelColIndex();
    for(int i=cind;i>=0;i--) { Object sitem = _actBrwsr.getCol(i).getSelectedItem();
        if(sitem instanceof Action)
            return (Action)sitem; }
    return null;
}

/**
 * Returns the top selected action.
 */
public Object getSelActionItem()  { return _actBrwsr.getSelectedItem(); }

/**
 * Initialize UI.
 */
protected void initUI()
{
    // Get editor
    _editor = getView("Editor", Editor.class);
    _editor.addPropChangeListener(pce -> editorSelViewChange(), Editor.SelView_Prop);
    
    // Get SelPathBox
    _selPathBox = getView("SelPathBox", RowView.class);
    updateSelPathBox();
    
    // Get/configure ViewTree
    _viewTree = getView("ViewTree", TreeView.class);
    _viewTree.setResolver(new ViewTreeResolver());
    
    // Get ActionBrowser
    _actBrwsr = getView("ActionBrowser", BrowserView.class); _actBrwsr.setPrefColCount(3);
    _actBrwsr.setFocusWhenPressed(false);
    _actBrwsr.setFireActionOnRelease(true);
    _actBrwsr.setResolver(new ActionResolver());
    
    // Set default actions
    _viewActions = snapbuild.app.Action.getViewActions(this);
    _childViewActions = snapbuild.app.Action.getChildViewActions(this);
    updateActionBrowser();
    
    // Set FirstFocus
    setFirstFocus("CommandText");
}

/**
 * Reset UI.
 */
protected void resetUI()
{
    // Update SelPathBox
    updateSelPathBox();
    
    // Update ViewTree
    _viewTree.setItems(getEditor().getContent());
    _viewTree.expandAll();
    _viewTree.setSelectedItem(getEditor().getSelView());
}

/**
 * Respond to UI.
 */
protected void respondUI(ViewEvent anEvent)
{
    // Handle ViewTree
    if(anEvent.equals(_viewTree)) {
        View view = _viewTree.getSelectedItem();
        getEditor().setSelView(view);
    }
    
    // Handle ActionBrowser
    if(anEvent.equals(_actBrwsr)) {
        Action act = getSelAction();
        if(act!=null)
            act.invoke();
        getEditor().repaint();
    }
}

/**
 * Called to update SelPathBox.
 */
protected void updateSelPathBox()
{
    _selPathBox.removeChildren(); if(_selPathDeep==null) _selPathDeep = getEditor().getSelView();
    View sview = getEditor().getSelView(), cview = getEditor().getContent(), view = _selPathDeep;
    while(view!=null) { View view2 = view;
        Label label = new Label(view.getClass().getSimpleName()); label.setPadding(2,2,2,2);
        if(view==sview) label.setFill(Color.LIGHTGRAY);
        label.addEventHandler(e -> selPathItemClicked(view2), MouseRelease);
        _selPathBox.addChild(label, 0);
        if(_selPathBox.getChildCount()>1) _selPathBox.addChild(new Label(" \u2022 "),1);
        if(view==cview) break; view = view.getParent();
    }
}

/**
 * Called to update ActionBrowser.
 */
protected void updateActionBrowser()
{
    View sview = _editor.getSelView();
    List <Action> items = getActions(sview);
    _actBrwsr.setItems(items);
    _actBrwsr.setSelectedItem(items.get(0));
}

/**
 * Called when Editor.SelView changes.
 */
protected void editorSelViewChange()
{
    updateActionBrowser();
    _selPathDeep = getEditor().getSelView();
    resetLater();
}

/**
 * Called when SelPath is clicked.
 */
protected void selPathItemClicked(View aView)
{
    View deep = _selPathDeep;
    getEditor().setSelView(aView);
    _selPathDeep = deep;
}
    
/**
 * Returns the actions for given view.
 */
protected List <Action> getActions(View aView)
{
    if(aView instanceof ChildView) return _childViewActions;
    return _viewActions;
}

/**
 * ActionResolver.
 */
public class ActionResolver extends TreeResolver {
    
    /** Returns the parent of given item. */
    public Object getParent(Object anItem)  { return null; }
    
    /** Whether given object is a parent (has children). */
    public boolean isParent(Object anItem)
    {
        if(anItem instanceof Action) return ((Action)anItem).hasItems();
        return false;
    }
    
    /** Returns the children. */
    public Object[] getChildren(Object aPar)
    {
        if(aPar instanceof Action)
            return ((Action)aPar).getItemArray();
        return null;
    }
    
    /**
     * Returns the text to be used for given item.
     */
    public String getText(Object anItem)
    {
        if(anItem instanceof Action) return ((Action)anItem).getName();
        if(anItem instanceof Class) return ((Class)anItem).getSimpleName();
        return anItem.toString();
    }
}

/**
 * A resolver for Views.
 */
public class ViewTreeResolver extends TreeResolver <View> {
    
    /** Returns the parent of given item. */
    public View getParent(View anItem)  { return anItem!=getEditor().getContent()? anItem.getParent() : null; }

    /** Whether given object is a parent (has children). */
    public boolean isParent(View anItem)
    {
        if(!(anItem instanceof ParentView)) return false;
        if(anItem instanceof Label || anItem instanceof ButtonBase || anItem instanceof Spinner) return false;
        if(anItem instanceof ComboBox || anItem instanceof ListView) return false;
        return ((ParentView)anItem).getChildCount()>0;
    }

    /** Returns the children. */
    public View[] getChildren(View aParent)
    {
        ParentView par = (ParentView)aParent;
        if(par instanceof ScrollView) { ScrollView sp = (ScrollView)par;
            return sp.getContent()!=null? new View[] { sp.getContent() } : new View[0]; }
        return par.getChildren();
    }

    /** Returns the text to be used for given item. */
    public String getText(View anItem)
    {
        String str = anItem.getClass().getSimpleName();
        String name = anItem.getName(); if(name!=null) str += " - " + name;
        String text = anItem.getText(); if(text!=null) str += " \"" + text + "\" ";
        return str;
    }

    /** Return the image to be used for given item. */
    public View getGraphic(View anItem)  { return null; }
}

}