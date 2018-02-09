package snapbuild.app;
import snap.gfx.*;
import snap.util.*;
import snap.view.*;
import snap.web.*;

/**
 * A view to hold the UI.
 */
public class Editor extends ParentView {
    
    // The Source URL
    WebURL           _url;

    // The content
    View             _content;
    
    // The context box
    BoxView          _cbox;

    // The Selected View
    View             _selView;
    
    // The undoer
    Undoer           _undoer = new Undoer();
    
    // The DeepChangeListener
    DeepChangeListener  _contentDeepChangeLsnr = (src,pce) -> contentDidDeepChange(src,pce);
    
    // Constants for properties
    public static final String SelView_Prop = "SelView";

    // The MIME type for reportmill xstring
    public static final String    SNAP_XML_TYPE = "snap-studio/xml";
    
/**
 * Creates the Editor view.
 */    
public Editor()
{
    // Configure this view
    setAlign(Pos.CENTER); setPadding(15,15,15,15);
    setFill(ViewUtils.getBackDarkFill());
    enableEvents(MouseRelease);
    
    // Configure ContentBox
    _cbox = new BoxView(); _cbox.setFillWidth(true); _cbox.setFillHeight(true); //_cbox.setMinSize(400,400);
    _cbox.setFill(ViewUtils.getBackFill()); _cbox.setBorder(new Color("#99"),1);
    _cbox.setPickable(false);
    _cbox.addDeepChangeListener(_contentDeepChangeLsnr);
    addChild(_cbox);
    
    // Set default content
    setContent(new ColView());
}

/**
 * Returns the source URL.
 */
public WebURL getSourceURL()  { return _url; }

/**
 * Sets the source URL.
 */
public void setSourceURL(WebURL aURL)  { _url = aURL; }

/**
 * Returns the source URL.
 */
public WebFile getSourceFile(boolean doCreate)
{
    if(_url==null) return null;
    WebFile file = _url.getFile();
    if(file==null && doCreate) file = _url.createFile(false);
    return file;
}

/**
 * Returns the content box.
 */
public View getContentBox()  { return _cbox; }

/**
 * Returns the content view.
 */
public View getContent()  { return _cbox.getContent(); }

/**
 * Sets the content view.
 */
public void setContent(View aView)
{
    _cbox.setContent(aView);
    setSelView(aView);
}

/**
 * Returns the selected view.
 */
public View getSelView()  { return _selView; }

/**
 * Sets the selected view.
 */
public void setSelView(View aView)
{
    firePropChange(SelView_Prop, _selView, _selView = aView);
    repaint();
}

/**
 * Sets whether editor is really doing editing.
 */
public void setEditing(boolean aValue)
{
    _cbox.setPickable(true);
    disableEvents(MouseRelease);
    _cbox.removeDeepChangeListener(_contentDeepChangeLsnr);
}

/**
 * Returns the content XML.
 */
public XMLElement getContentXML()
{
    ViewArchiver varch = new ViewArchiver();
    return varch.writeObject(getContent());
}

/**
 * The real save method.
 */
public void save() throws Exception
{
    // Get source file and save (update file might get called from here)
    updateFile();
    WebFile file = getSourceFile(true);
    file.save();
    
    // Clear undoer
    getUndoer().reset();
}

/**
 * Updates the source file from editor.
 */
public void updateFile()
{
    WebFile file = getSourceFile(true);
    XMLElement xml = getContentXML();
    byte bytes[] = xml.getBytes();
    file.setBytes(bytes);
}

/** Override. */
protected double getPrefWidthImpl(double aH)  { return BoxView.getPrefWidth(this, _cbox, aH); }

/** Override. */
protected double getPrefHeightImpl(double aW)  { return BoxView.getPrefHeight(this, _cbox, aW); }

/** Override. */
protected void layoutImpl()  { BoxView.layout(this, _cbox, null, false, false); }

/**
 * Override to handle events.
 */
protected void processEvent(ViewEvent anEvent)
{
    // Handle MouseRelease
    if(anEvent.isMouseRelease()) mouseRelease(anEvent);
}

/**
 * Called when there is a MouseRelease.
 */
protected void mouseRelease(ViewEvent anEvent)
{
    Point pnt = anEvent.getPoint(_cbox);
    View view = ViewUtils.getDeepestChildAt(_cbox, pnt.getX(), pnt.getY());
    while(view!=null && !(view.getParent() instanceof ChildView) && view.getParent()!=_cbox)
        view = view.getParent();
    if(view==null || view==_cbox) view = getContent();
    setSelView(view);
}

/**
 * Override to repaint selected view with highlite.
 */
protected void paintAbove(Painter aPntr)
{
    // Get round rect for selected view
    View sview = getSelView();
    Rect bnds = sview.localToParent(sview.getBoundsShape(), this).getBounds();
    RoundRect rrect = new RoundRect(bnds.x-1, bnds.y-1, bnds.width+2, bnds.height+2, 3);
    
    // Set color and draw rect
    if(sview!=getContent()) {
        aPntr.setColor(new Color(.3,.3,1,.33)); aPntr.setStroke(new Stroke(3)); aPntr.draw(rrect); }
    
    // Repaint selected view
    Point pnt = sview.getParent().localToParent(sview.getX(), sview.getY(), this);
    aPntr.translate(pnt.getX(), pnt.getY());
    ViewUtils.paintAll(sview, aPntr);
}

/**
 * Handles editor cut operation.
 */
public void cut()  { copy(); delete(); }

/**
 * Handles editor copy operation.
 */
public void copy()
{
    // If SelView is Content, just return
    if(getSelView()==getContent()) { ViewUtils.beep(); return; }
    
    // Get xml for selected shapes, and get as string
    XMLElement xml = new ViewArchiver().writeObject(getSelView());
    String xmlStr = xml.toString();
    
    // Get clipboard and add data as XML string (RMData) and plain string
    Clipboard cb = Clipboard.get();
    cb.addData(SNAP_XML_TYPE, xmlStr);
    cb.addData(xmlStr);
}

/**
 * Handles editor paste operation.
 */
public void paste()
{
    // If Clipboard has View Data, paste it
    Clipboard cb = Clipboard.get();
    if(cb.hasData(SNAP_XML_TYPE)) {
        
        // Get bytes and view
        byte bytes[] = cb.getDataBytes(SNAP_XML_TYPE);
        View view = new ViewArchiver().getView(bytes);
        
        // Get selected view
        View sview = getSelView();
        ParentView par = sview.getParent();
        int ind = sview.indexInParent();
        
        // Add view and select it
        ViewHpr.getHpr(par).addChild(par, view, ind+1);
        setSelView(view);
    }
}

/**
 * Handles editor delete operation.
 */
public void delete()
{
    View sview = getSelView();
    ParentView par = sview.getParent();
    int ind = sview.indexInParent();
    if(par instanceof ChildView) { ChildView cview = (ChildView)par;
        cview.removeChild(sview);
        if(cview.getChildCount()>0)
            setSelView(ind<cview.getChildCount()? cview.getChild(ind) : cview.getChild(ind-1));
        else setSelView(cview);
    }
}

/**
 * Override to return Undoer.
 */
public Undoer getUndoer()  { return _undoer; }

/**
 * Called to undo the last edit operation in the editor.
 */
public void undo()
{
    // If undoer exists, do undo, select views and repaint
    if(getUndoer()!=null && getUndoer().getUndoSetLast()!=null) {
        UndoSet undoSet = getUndoer().undo();
        setUndoSelection(undoSet.getUndoSelection());
        repaint();
    }

    // Otherwise beep
    else ViewUtils.beep();
}

/**
 * Called to redo the last undo operation in the editor.
 */
public void redo()
{
    // If undoer exists, do undo, select views and repaint
    if(getUndoer()!=null && getUndoer().getRedoSetLast()!=null) {
        UndoSet redoSet = getUndoer().redo();
        setUndoSelection(redoSet.getRedoSelection());
        repaint();
    }

    // Otherwise beep
    else ViewUtils.beep();
}

/**
 * Sets the undo selection.
 */
protected void setUndoSelection(Object aSel)
{
    // Handle List <View>
    //if(aSel instanceof List) setSelectedViews((List)aSelection);
    if(aSel instanceof View) setSelView((View)aSel);
}

/**
 * Called when ContentBox has deep change.
 */
protected void contentDidDeepChange(Object aView, PropChange anEvent)
{
    // Get source and prop name (if not View, just return)
    Object src = anEvent.getSource();
    View view = (View)aView, sview = src instanceof View? (View)src : null; if(view==null) return;
    String pname = anEvent.getPropertyName();
    
    // Ignore properties: Showing, NeedsLayout
    if(pname==Parent_Prop) return;
    if(pname==Showing_Prop) return;
    if(pname==NeedsLayout_Prop) return;
    
    // Ignore layout changes
    if(view instanceof ParentView && ((ParentView)view).isInLayout()) return;
    if(sview instanceof ParentView && ((ParentView)sview).isInLayout()) return;
    
    // If undoer exists, set selected objects and add property change
    Undoer undoer = getUndoer();
    if(undoer!=null) {
        
        // If no changes yet, set selected objects
        if(undoer.getActiveUndoSet().getChangeCount()==0)
            undoer.setUndoSelection(getSelView()); //new ArrayList(getSelectedOrSuperSelectedViews())
        
        // Add property change
        undoer.addPropertyChange(anEvent);
        
        // Save UndoerChanges after delay
        saveUndoerChangesLater();
        
        // Set updator
        WebFile file = getSourceFile(false);
        if(file!=null) file.setUpdater(undoer.hasUndos()? _updr : null);
    }
    
    // Forward DeepChanges to EditorPane. Should have add/removeDeepChagneLister methods for this.
    //EditorPane ep = getEditorPane(); if(ep!=null) ep.resetLater();
}

// A Shared updater to kick off save
private WebFile.Updater _updr = file -> updateFile();

/**
 * Saves Undo Changes.
 */
protected void saveUndoerChanges()
{
    // If MouseIsDown, come back later
    _saveChangesRun = null;
    if(ViewUtils.isMouseDown()) { saveUndoerChangesLater(); return; }

    // Get undoer
    Undoer undoer = getUndoer(); if(undoer==null || !undoer.isEnabled()) return;
    
    // Set undo selected-views
    //List views = getSelectedViewCount()>0? getSelectedViews() : getSuperSelectedViews();
    //if(undoer.getRedoSelection()==null) undoer.setRedoSelection(new ArrayList(views));
    
    // Save undo changes
    undoer.saveChanges();
}

/**
 * Saves undo changes after a delay.
 */
protected void saveUndoerChangesLater()
{
    if(_saveChangesRun==null)
        getEnv().runDelayed(_saveChangesRun = _scrShared, 400, true);
}

private Runnable _saveChangesRun, _scrShared = () -> saveUndoerChanges();

}