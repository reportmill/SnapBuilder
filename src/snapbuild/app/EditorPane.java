package snapbuild.app;
import java.util.*;
import snap.gfx.Color;
import snap.gfx.Image;
import snap.util.*;
import snap.view.*;
import snap.viewx.*;
import snap.web.WebURL;

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
    
    // The command textfield
    TextField        _cmdText;

    // The ViewTree
    TreeView <View>  _viewTree;
    
    // The ActionBrowser
    BrowserView      _actBrwsr;

/**
 * Creates a new EditorPane.
 */
public EditorPane()
{
    getUI();
}

/**
 * Returns the editor.
 */
public Editor getEditor()  { return _editor; }

/**
 * Returns the document source.
 */
protected WebURL getSourceURL()  { return getEditor().getSourceURL(); }

/**
 * Sets the source URL.
 */
public void setSourceURL(WebURL aURL)  { getEditor().setSourceURL(aURL); }

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
 * Creates a new default editor pane.
 */
public EditorPane newDocument()
{
    RowView row = new RowView(); row.setPadding(4,4,4,4); row.setSpacing(4); row.setGrowWidth(true);
    ColView col = new ColView(); col.setPadding(4,4,4,4); col.setSpacing(4); col.addChild(row);
    getEditor().setContent(col);
    setSelView(row);
    return this;
}

/**
 * Creates a new editor window from an open panel.
 */
public EditorPane showOpenPanel(View aView)
{
    // Get path from open panel for supported file extensions
    String path = FileChooser.showOpenPanel(aView, "Snap UI File", "snp");
    return open(path);
}

/**
 * Creates a new editor window by opening the document from the given source.
 */
public EditorPane open(Object aSource)
{
    // If source is already opened, return editor pane
    WebURL url = WebURL.getURL(aSource);
    //if(!SnapUtils.equals(url, getSourceURL())) {
    //    EditorPane epanes[] = WindowView.getOpenWindowOwners(EditorPane.class);
    //    for(EditorPane epane : epanes) if(SnapUtils.equals(url, epane.getSourceURL())) return epane; }
    
    // Load document (if not found, just return)
    ParentView view = getParentView(aSource); if(view==null) return null;

    // Set document
    getEditor().setContent(view);
    getEditor()._url = url;
    
    // If source is string, add to recent files menu
    //if(url!=null) RecentFilesPanel.addRecentFile(url.getString());
    
    // Return the editor
    return this;
}

/**
 * Creates a ParentView from given source.
 */
protected ParentView getParentView(Object aSource)
{
    // If document source is null, just return null
    if(aSource==null || aSource instanceof ParentView) return (ParentView)aSource;
    
    // Load document
    ViewArchiver archiver = new ViewArchiver(); ViewArchiver.setUseRealClass(false);
    ParentView view = null; try { view = archiver.getParentView(aSource); }
    
    // If there was an XML parse error loading aSource, show error dialog
    catch(Exception e) {
        e.printStackTrace();
        String msg = StringUtils.wrap("Error reading file:\n" + e.getMessage(), 40);
        runLater(() -> {
            DialogBox dbox = new DialogBox("Error Reading File"); dbox.setErrorMessage(msg);
            dbox.showMessageDialog(getUI()); });
    }
    ViewArchiver.setUseRealClass(true);
    return view;
}

/**
 * Saves the current editor document, running the save panel.
 */
public void saveAs()
{
    // Make sure editor isn't previewing
    //setEditing(true);
    
    // Get extensions - if there is an existing extension, make sure it's first in the exts array
    String exts[] = { "snp" };
    if(getSourceURL()!=null && FilePathUtils.getExtension(getSourceURL().getPath())!=null) {
        List ex = new ArrayList(Arrays.asList(exts));
        ex.add(0, "." + FilePathUtils.getExtension(getSourceURL().getPath()));
        exts = (String[])ex.toArray(new String[ex.size()]);
    }
    
    // Run save panel, set Document.Source to path and re-save (or just return if cancelled)
    String path = FileChooser.showSavePanel(getEditor(), "SnapKit UI file", exts); if(path==null) return;
    setSourceURL(WebURL.getURL(path));
    save();
}

/**
 * Saves the current editor document, running the save panel if needed.
 */
public void save()
{
    // If can't save to current source, do SaveAs instead
    if(getSourceURL()==null) { saveAs(); return; }
    
    // Make sure editor isn't previewing
    //setEditing(true);
    
    // Do actual save - if exception, print stack trace and set error string
    try { getEditor().save(); }
    catch(Throwable e) {
        e.printStackTrace();
        String msg = "The file " + getSourceURL().getPath() + " could not be saved (" + e + ").";
        DialogBox dbox = new DialogBox("Error on Save"); dbox.setErrorMessage(msg);
        dbox.showMessageDialog(getUI());
        return;
    }
    
    // Add URL.String to RecentFilesMenu and reset UI
    //if(getSourceURL()!=null) RecentFilesPanel.addRecentFile(getSourceURL().getString());
    resetLater();
}

/**
 * Reloads the current editor document from the last saved version.
 */
public void revert()
{
    // Get filename (just return if null)
    WebURL surl = getSourceURL(); if(surl==null) return;

    // Run option panel for revert confirmation (just return if denied)
    String msg = "Revert to saved version of " + surl.getPathName() + "?";
    DialogBox dbox = new DialogBox("Revert to Saved"); dbox.setQuestionMessage(msg);
    if(!dbox.showConfirmDialog(getUI())) return;
        
    // Re-open filename
    getSourceURL().getFile().reload();
    open(getSourceURL());
}

/**
 * Closes this editor pane
 */
public boolean close()
{
    // Make sure editor isn't previewing
    //setEditing(true);
    
    // If unsaved changes, run panel to request save
    /*if(getEditor().undoerHasUndos()) {
        String filename = getSourceURL()==null? "untitled document" : getSourceURL().getPathName();
        DialogBox dbox = new DialogBox("Unsaved Changes");
        dbox.setWarningMessage("Save changes to " + filename + "?"); dbox.setOptions("Save", "Don't Save", "Cancel");
        switch(dbox.showOptionDialog(getUI(), "Save")) {
            case 0: save();
            case 1: break;
            default: return false;
        }
    }*/
    
    // Deactive current tool, so it doesn't reference this editor
    //getEditor().getCurrentTool().deactivateTool();
    
    // Close window, called EditorClosed and return true to indicate we closed the window
    getWindow().hide();
    editorClosed();
    return true;
}

/**
 * Called when editor is closed.
 */
protected void editorClosed()
{
    // If another open editor is available focus on it, otherwise run WelcomePanel
    EditorPane epane = WindowView.getOpenWindowOwner(EditorPane.class);
    if(epane!=null)
        epane.getEditor().requestFocus();
    else WelcomePanel.getShared().showPanel();
}

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
    
    // Get CommandText TextField
    _cmdText = getView("CommandText", TextField.class);
    
    // Get/configure ViewTree
    _viewTree = getView("ViewTree", TreeView.class);
    _viewTree.setResolver(new ViewTreeResolver());
    _viewTree.setOwner(this);
    getView("EditorSplitView", SplitView.class).removeItem(_viewTree);
    
    // Get ActionBrowser
    _actBrwsr = getView("ActionBrowser", BrowserView.class); _actBrwsr.setPrefColCount(3); _actBrwsr.setRowHeight(25);
    _actBrwsr.setFocusWhenPressed(false);
    _actBrwsr.setFireActionOnRelease(true);
    _actBrwsr.setResolver(new ActionResolver());
    enableEvents(_actBrwsr, MouseRelease);
    
    // Set default actions
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
    if(_viewTree.isShowing()) {
        _viewTree.setItems(getContent());
        _viewTree.setSelectedItem(null);
        _viewTree.collapseItem(getContent());
        _viewTree.expandItem(getContent());
        for(View v=getSelView();v!=getContent();v=v.getParent())
            _viewTree.expandItem(v);
        _viewTree.setSelectedItem(getSelView());
    }
    
    // If title has changed, update window title
    if(isWindowVisible()) {
        String title = getWindowTitle();
        WindowView win = getWindow();
        if(!SnapUtils.equals(title, win.getTitle())) {
            win.setTitle(title);
            win.setDocURL(getSourceURL());
        }
    }
}

/**
 * Respond to UI.
 */
protected void respondUI(ViewEvent anEvent)
{
    // Handle ShowXMLButton
    if(anEvent.equals("ShowXMLButton"))
        toggleShowXML();

    // Handle ShowViewTreeButton
    if(anEvent.equals("ShowViewTreeButton"))
        toggleShowViewTree();

    // Handle ViewTree
    if(anEvent.equals(_viewTree)) {
        View view = _viewTree.getSelectedItem();
        getEditor().setSelView(view);
    }
    
    // Handle ActionBrowser
    if(anEvent.equals(_actBrwsr)) {
        
        // Handle MouseClick double-click: Invoke action
        if(anEvent.isMouseClick() && anEvent.getClickCount()==2) {
            Action act = getSelAction();
            if(act!=null) invokeAction(act);
        }
        
        // Handle Action: Update CommandText
        else if(anEvent.isActionEvent()) {
            _cmdText.setText(_actBrwsr.getPath(" "));
            _cmdText.setSel(_cmdText.length());
        }
    }
    
    // Handle EscapeAction
    if(anEvent.equals("EscapeAction")) {
        View sview = getSelView(), par = sview.getParent();
        if(sview!=getContent())
            setSelView(par);
        else beep();
    }
    
    // Handle SaveMenuItem, SaveButton, SaveAsMenuItem, SaveAsPDFMenuItem, RevertMenuItem
    if(anEvent.equals("SaveMenuItem") || anEvent.equals("SaveButton")) save();
    if(anEvent.equals("SaveAsMenuItem")) saveAs();
    if(anEvent.equals("RevertMenuItem")) revert();
}

/**
 * Invokes the given action.
 */
public void invokeAction(Action anAct)
{
    anAct.invoke(this);
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
    Action items[] = getActions(sview);
    _actBrwsr.setItems((Object[])items);
    _actBrwsr.setSelectedItem(items[0]);
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
 * Shows/Hides ViewTree.
 */
protected void toggleShowViewTree()
{
    SplitView split = getView("EditorSplitView", SplitView.class);

    if(_viewTree.getParent()==null) {
        split.addItemWithAnim(_viewTree, 160, 0);
        updateXMLText();
    }
    else {
        split.removeItemWithAnim(_viewTree);
    }
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
 * Returns the window title.
 */
public String getWindowTitle()
{
    // Get window title: Basic filename + optional "Doc edited asterisk + optional "Doc Scaled"
    String title = getSourceURL()!=null? getSourceURL().getPath() : null; if(title==null) title = "Untitled";

    // If has undos, add asterisk. If zoomed, add ZoomFactor
    //if(getEditor().getUndoer()!=null && getEditor().getUndoer().hasUndos()) title = "* " + title;
    return title;
}

/**
 * Returns the actions for given view.
 */
protected Action[] getActions(View aView)  { return ViewHpr.getHpr(aView).getActions(); }

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
        //String name = anItem.getName(); if(name!=null) str += " - " + name;
        //String text = anItem.getText(); if(text!=null) str += " \"" + text + "\" ";
        return str;
    }

    /** Return the image to be used for given item. */
    public View getGraphic(View anItem)  { return null; }
}

}