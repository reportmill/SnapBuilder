package snapbuild.app;
import snap.geom.*;
import snap.gfx.*;
import snap.util.*;
import snap.view.*;
import snap.web.*;

/**
 * A view to hold the UI.
 */
public class Editor extends ParentView {
    
    // The Source URL
    protected WebURL  _url;

    // The content
    private View  _content;
    
    // The context box
    private BoxView  _cbox;

    // The undoer
    private Undoer  _undoer = new Undoer();

    // The editor selection
    private EditorSel _sel = new EditorSel(this);

    // The Styler
    private EditorStyler _styler = new EditorStyler(this);

    // The default CopyPaster
    private EditorCopyPaster _copyPaster;

    // The DeepChangeListener
    DeepChangeListener  _contentDeepChangeLsnr = (src,pce) -> contentDidDeepChange(src,pce);
    
    // Constants for properties
    public static final String SelView_Prop = "SelView";
    public static final String SuperSelView_Prop = "SuperSelView";
    public static final String SelSpot_Prop = "SelSpot";

    // Constants for ordering
    public enum Order { BEFORE, ON, AFTER }

    // Constants
    public static Color BACK_FILL = new Color(165, 179, 216).brighter(); // ViewUtils.getBackDarkFill()
    private Color SEL_COLOR = new Color(.3,.3,1,.33);
    private Stroke SEL_STROKE = new Stroke(3);

    /**
     * Creates the Editor view.
     */
    public Editor()
    {
        // Configure this view
        setAlign(Pos.CENTER);
        setPadding(15,15,15,15);
        setFill(BACK_FILL);
        enableEvents(MouseRelease);
        setFocusable(true);
        setFocusWhenPressed(true);

        // Configure ContentBox
        _cbox = new BoxView();
        _cbox.setFillWidth(true);
        _cbox.setFillHeight(true);
        _cbox.setFill(ViewUtils.getBackFill());
        _cbox.setBorder(new Color("#99"),1);
        _cbox.setEffect(new ShadowEffect());
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
     * Returns the editor selection.
     */
    public EditorSel getSel()  { return _sel; }

    /**
     * Returns the selected view.
     */
    public View getSelView()  { return _sel.getSelView(); }

    /**
     * Sets the selected view.
     */
    public void setSelView(View aView)  { _sel.setSelView(aView); }

    /**
     * Returns the selected or super selected view.
     */
    public View getSelOrSuperSelView()
    {
        return _sel.getSelOrSuperSelView();
    }

    /**
     * Lets EditorSel fire prop changes.
     */
    protected void fireSelPropChange(String aProp, View anOld, View aNew)
    {
        firePropChange(aProp, anOld, aNew);
    }

    /**
     * Adds a view to content.
     */
    public void addView(Class <? extends View> aCls)
    {
        // Create view from class, configure, add
        View view = null; try { view = aCls.newInstance(); } catch(Exception e) { }
        ViewHpr.getHpr(view).configure(view);
        addView(view);
    }

    /**
     * Adds a view to content.
     */
    public void addView(View aView)
    {
        // Get AddView and index
        EditorSel.Tuple<View,Integer> addViewAndIndex = _sel.getAddViewAndIndex();
        View host = addViewAndIndex.getA();
        int index = addViewAndIndex.getB();

        // If selected view parent is host, add to it
        if(host!=null)
            ((ViewHost)host).addGuest(aView, index);

        // Otherwise bail and complain
        else { ViewUtils.beep(); return; }

        // Select view
        setSelView(aView);
    }

    /**
     * Returns the Styler.
     */
    public EditorStyler getStyler()
    {
        return _styler;
    }

    /**
     * Returns the editor copy/paster.
     */
    public EditorCopyPaster getCopyPaster()
    {
        if (_copyPaster!=null) return _copyPaster;
        return _copyPaster = new EditorCopyPaster(this);
    }

    /**
     * Standard clipboard cut functionality.
     */
    public void cut()  { getCopyPaster().cut(); }

    /**
     * Standard clipboard copy functionality.
     */
    public void copy()  { getCopyPaster().copy(); }

    /**
     * Standard clipbard paste functionality.
     */
    public void paste()  { getCopyPaster().paste(); }

    /**
     * Deletes all the currently selected views.
     */
    public void delete()  { getCopyPaster().delete(); }

    /**
     * Causes all the children of the current super selected view to become selected.
     */
    public void selectAll()  { getCopyPaster().selectAll(); }

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
        return varch.writeToXML(getContent());
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

    /**
     * Layout method: Override.
     */
    protected double getPrefWidthImpl(double aH)
    {
        double pw = BoxView.getPrefWidth(this, _cbox, aH);
        return pw;
    }

    /**
     * Layout method: Override.
     */
    protected double getPrefHeightImpl(double aW)
    {
        double ph = BoxView.getPrefHeight(this, _cbox, aW);
        return ph;
    }

    /**
     * Layout method: Override.
     */
    protected void layoutImpl()
    {
        BoxView.layout(this, _cbox, null, false, false);
    }

    /**
     * Override to handle events.
     */
    protected void processEvent(ViewEvent anEvent)
    {
        if (anEvent.isMouseRelease()) mouseRelease(anEvent);
    }

    /**
     * Called when there is a MouseRelease.
     */
    protected void mouseRelease(ViewEvent anEvent)
    {
        _sel.setSelectionForPoint(anEvent.getPoint());
    }

    /**
     * Override to repaint selected view with highlite.
     */
    protected void paintAbove(Painter aPntr)
    {
        // Get round rect for selected view
        View sview = getSelOrSuperSelView();
        Rect bnds = sview.localToParent(sview.getBoundsShape(), this).getBounds();
        RoundRect rrect = new RoundRect(bnds.x-1, bnds.y-1, bnds.width+2, bnds.height+2, 3);

        // Paint selection for SelOrSuperSelView
        if (sview!=getContent()) {
            aPntr.setColor(SEL_COLOR);
            aPntr.setStroke(SEL_STROKE); aPntr.draw(rrect);
            aPntr.setStroke(Stroke.Stroke1);
        }

        // Paint SelSpot
        _sel.paintSel(aPntr);

        // Repaint selected view
        if (sview.getRotate()==0) {
            Point pnt = sview.getParent().localToParent(sview.getX(), sview.getY(), this);
            aPntr.translate(pnt.x, pnt.y);
            ViewUtils.paintAll(sview, aPntr);
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
        if (getUndoer()!=null && getUndoer().getUndoSetLast()!=null) {
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
        if (getUndoer()!=null && getUndoer().getRedoSetLast()!=null) {
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
        if (aSel instanceof View) setSelView((View)aSel);
    }

    /**
     * Called when ContentBox has deep change.
     */
    protected void contentDidDeepChange(Object aView, PropChange aPC)
    {
        // Get source and prop name (if not View, just return)
        Object src = aPC.getSource();
        View view = (View)aView, sview = src instanceof View ? (View)src : null; if(view==null) return;
        String pname = aPC.getPropertyName();

        // Ignore properties: Showing, NeedsLayout
        if (pname==Parent_Prop) return;
        if (pname==Showing_Prop) return;
        if (pname==NeedsLayout_Prop) return;
        if (pname==ParentView.Child_Prop) {
            if(!(view instanceof ViewHost))
                return;
        }

        // Ignore layout changes
        if (view instanceof ParentView && ((ParentView)view).isInLayout()) return;
        if (sview instanceof ParentView && ((ParentView)sview).isInLayout()) return;

        // If undoer exists, set selected objects and add property change
        Undoer undoer = getUndoer();
        if (undoer!=null) {

            // If no changes yet, set selected objects
            if(undoer.getActiveUndoSet().getChangeCount()==0)
                undoer.setUndoSelection(getSelOrSuperSelView()); //new ArrayList(getSelectedOrSuperSelectedViews())

            // Add property change
            undoer.addPropChange(aPC);

            // Save UndoerChanges after delay
            saveUndoerChangesLater();

            // Set updator
            WebFile file = getSourceFile(false);
            if(file!=null) file.setUpdater(undoer.hasUndos()? _updr : null);
        }

        // Forward DeepChanges to EditorPane. Should have add/removeDeepChagneLister methods for this.
        //EditorPane ep = getEditorPane(); if(ep!=null) ep.resetLater();
        repaint();
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
        if (ViewUtils.isMouseDown()) { saveUndoerChangesLater(); return; }

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

    // Support for delayed saveUnderChanges()
    private Runnable _saveChangesRun, _scrShared = () -> saveUndoerChanges();
}