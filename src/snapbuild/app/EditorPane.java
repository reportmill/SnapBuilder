package snapbuild.app;
import java.util.List;
import snap.gfx.Color;
import snap.gfx.Image;
import snap.util.SnapUtils;
import snap.view.*;
import snap.viewx.TextPane;

/**
 * A class to manage the Editor and controls.
 */
public class EditorPane extends ViewOwner {
    
    // The Editor
    Editor           _editor;
    
    // The XML TextView
    TextView         _xmlText;
    
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
 * Returns the Editor.Content.
 */
public View getContent()  { return _editor.getContent(); }

/**
 * Returns the Editor.SelView.
 */
public View getSelView()  { return _editor.getSelView(); }

/**
 * Sets the Editor.SelView.
 */
public void setSelView(View aView)  { _editor.setSelView(aView); }

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
    
    // Get XMLText TextView
    _xmlText = getView("XMLText", TextView.class);
    getView("SplitView", SplitView.class).removeItem(_xmlText);
    
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
    
    // Add action for Escape key to pop selection
    addKeyActionFilter("EscapeAction", "ESCAPE");
    
    // Set Toolbar images
    getView("SaveButton", ButtonBase.class).setImage(Image.get(TextPane.class, "pkg.images/File_Save.png"));
    getView("CutButton", ButtonBase.class).setImage(Image.get(TextPane.class, "pkg.images/Edit_Cut.png"));
    getView("CopyButton", ButtonBase.class).setImage(Image.get(TextPane.class, "pkg.images/Edit_Copy.png"));
    getView("PasteButton", ButtonBase.class).setImage(Image.get(TextPane.class, "pkg.images/Edit_Paste.png"));
    getView("DeleteButton", ButtonBase.class).setImage(Image.get(TextPane.class, "pkg.images/Edit_Delete.png"));
    getView("IncreaseFontButton", ButtonBase.class).setImage(Image.get(TextPane.class, "pkg.images/Font_Increase.png"));
    getView("DecreaseFontButton", ButtonBase.class).setImage(Image.get(TextPane.class, "pkg.images/Font_Decrease.png"));
    getView("UndoButton", ButtonBase.class).setImage(Image.get(TextPane.class, "pkg.images/Edit_Undo.png"));
    getView("RedoButton", ButtonBase.class).setImage(Image.get(TextPane.class, "pkg.images/Edit_Redo.png"));
}

/**
 * Reset UI.
 */
protected void resetUI()
{
    // Update SelPathBox
    updateSelPathBox();
    
    // Update ViewTree
    _viewTree.setItems(getContent());
    _viewTree.expandItem(getContent());
    _viewTree.setSelectedItem(getSelView());
    for(View v=getSelView();v!=getContent();v=v.getParent())
        _viewTree.expandItem(v);
}

/**
 * Respond to UI.
 */
protected void respondUI(ViewEvent anEvent)
{
    // Handle ShowXMLButton
    if(anEvent.equals("ShowXMLButton"))
        toggleShowXML();

    // Handle ViewTree
    if(anEvent.equals(_viewTree)) {
        View view = _viewTree.getSelectedItem();
        getEditor().setSelView(view);
    }
    
    // Handle ActionBrowser
    if(anEvent.equals(_actBrwsr)) {
        Action act = getSelAction();
        if(act!=null) invokeAction(act);
    }
    
    // Handle EscapeAction
    if(anEvent.equals("EscapeAction")) {
        View sview = getSelView(), par = sview.getParent();
        if(sview!=getContent())
            setSelView(par);
        else beep();
    }
}

/**
 * Invokes the given action.
 */
public void invokeAction(Action anAct)
{
    anAct.invoke();
    getEditor().repaint();
    updateXMLText();
}

/**
 * Called to update SelPathBox.
 */
protected void updateSelPathBox()
{
    _selPathBox.removeChildren(); if(_selPathDeep==null) _selPathDeep = getSelView();
    View sview = getSelView(), cview = getContent(), view = _selPathDeep;
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
    View sview = getSelView();
    List <Action> items = getActions(sview);
    _actBrwsr.setItems(items);
    _actBrwsr.setSelectedItem(items.get(0));
}

/**
 * Updates the XMLText TextView.
 */
protected void updateXMLText()
{
    // If not showing, just return
    if(!_xmlText.isShowing()) return;
    
    // Get View
    View view = getContent();
    String text = SnapUtils.getText(new ViewArchiver().toXML(view).getBytes());
    _xmlText.setText(text);
}

/**
 * Updates the XMLText TextView.
 */
protected void updateXMLTextSel()
{
    // If not showing, just return
    if(!_xmlText.isShowing()) return;
    
    // Get View
    View sview = getSelView();
}

/**
 * Called when Editor.SelView changes.
 */
protected void editorSelViewChange()
{
    updateActionBrowser();
    _selPathDeep = getSelView();
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
 * Shows/Hides XMLText TextView.
 */
protected void toggleShowXML()
{
    SplitView split = getView("SplitView", SplitView.class);

    if(_xmlText.getParent()==null) {
        split.addItemWithAnim(_xmlText, 160);
        updateXMLText();
    }
    else {
        split.removeItemWithAnim(_xmlText);
    }
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
    public View getParent(View anItem)  { return anItem!=getContent()? anItem.getParent() : null; }

    /** Whether given object is a parent (has children). */
    public boolean isParent(View anItem)
    {
        if(!(anItem instanceof ParentView)) return false;
        if(anItem instanceof Label || anItem instanceof ButtonBase || anItem instanceof Spinner ||
            anItem instanceof TextField) return false;
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