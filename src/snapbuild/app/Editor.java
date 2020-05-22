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

    // The Selected View
    private View  _selView;
    
    // The Super Selected View
    private View  _superSelView;

    // The relative spot view (if selection is a spot)
    private View  _spotView;

    // The relaive spot order
    private Order  _spotOrder;

    // The timer to handle spot painting
    private ViewTimer  _spotTimer;

    // Whether to suppress spot painting
    private boolean  _hideSpot;

    // The undoer
    private Undoer  _undoer = new Undoer();

    // The Styler
    private EditorStyler _styler = new EditorStyler(this);
    
    // The DeepChangeListener
    DeepChangeListener  _contentDeepChangeLsnr = (src,pce) -> contentDidDeepChange(src,pce);
    
    // Constants for properties
    public static final String SelView_Prop = "SelView";
    public static final String SuperSelView_Prop = "SuperSelView";
    public static final String SelSpot_Prop = "SelSpot";

    // Constants for ordering
    public enum Order { BEFORE, ON, AFTER }

    // Constants
    public static final String    SNAP_XML_TYPE = "snap-studio/xml";
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
     * Returns the selected view.
     */
    public View getSelView()  { return _selView; }

    /**
     * Sets the selected view.
     */
    public void setSelView(View aView)
    {
        // If already set, just return
        if (aView==getSelView()) return;

        // Set value
        View old = _selView;
        _selView = aView;
        _spotView = null;
        _spotOrder = null;

        // Set SuperSelView
        View par = _selView!=null ? _selView.getParent() : null;
        if (par!=null)
            setSuperSelView(par);

        // FirePropChange and repaint
        firePropChange(SelView_Prop, old, _selView);
        repaint();
        setSpotAnim();
    }

    /**
     * Sets the super selected view.
     */
    public View getSuperSelView()  { return _superSelView; }

    /**
     * Sets the super selected view.
     */
    public void setSuperSelView(View aView)
    {
        // If value already set, just return
        if (aView==getSuperSelView()) return;

        // Set value
        View old = _superSelView;
        _superSelView = aView;

        // FirePropChange and repaint
        firePropChange(SuperSelView_Prop, old, _superSelView);
        repaint();
    }

    /**
     * Returns the selected or super selected view.
     */
    public View getSelOrSuperSelView()
    {
        return _selView!=null ? _selView : _superSelView;
    }

    /**
     * Returns whether selection is really just a spot.
     */
    public boolean isSelSpot()  { return _spotView!=null; }

    /**
     * Returns the spot selection view.
     */
    public View getSelSpot()  { return _spotView; }

    /**
     * Sets a selected spot relative to given view (before = -1, on = 0, after = 1).
     */
    public void setSelSpot(View aView, Order anOrder)
    {
        // If already set, just return
        if (aView==_spotView && anOrder==_spotOrder) return;

        // Set value(s)
        View old = _spotView; _spotView = aView;
        _spotOrder = anOrder;

        // Clear SelView
        _selView = null;

        // Set SuperSelView
        View par = anOrder==Order.ON ? _spotView : _spotView!=null ? _spotView.getParent() : null;
        if (par!=null)
            setSuperSelView(par);

        // Set Value, FirePropChange and repaint
        firePropChange(SelSpot_Prop, old, _spotView);
        repaint();
        setSpotAnim();
    }

    /**
     * Returns the spot order.
     */
    public Order getSelSpotOrder()  { return _spotOrder; }

    /**
     * Sets the caret animation to whether it's needed.
     */
    private void setSpotAnim()
    {
        boolean isNeeded = isFocused() && isSelSpot() && isShowing();
        setSpotAnim(isNeeded);
    }

    /**
     * Returns whether ProgressBar is animating.
     */
    private boolean isSpotAnim()  { return _spotTimer!=null; }

    /**
     * Sets anim.
     */
    private void setSpotAnim(boolean aValue)
    {
        if (aValue==isSpotAnim()) return;
        if (aValue) {
            _spotTimer = new ViewTimer(500, t -> toggleShowSpot());
            _spotTimer.start();
        }
        else { _spotTimer.stop(); _spotTimer = null; _hideSpot = false; repaint(); }
    }

    /**
     * Called to make spot show/hid.
     */
    private void toggleShowSpot()
    {
        _hideSpot = !_hideSpot;
        repaint();
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
        // Get selected view
        View sview = getSelOrSuperSelView();

        // Get index
        int index = sview instanceof ViewHost ? ((ViewHost)sview).getGuestCount() : 0;
        if (isSelSpot()) {
            sview = getSelSpot();
            index = sview.indexInHost();
            if (getSelSpotOrder()==Order.AFTER) index++;
        }

        // If selected view is host, add to it
        if(sview instanceof ViewHost)       //ViewHpr.getHpr(sview).addView(sview, view);
            ((ViewHost)sview).addGuest(aView);

        // If selected view parent is host, add to it
        else if(sview.getHost()!=null)
            sview.getHost().addGuest(aView, index);

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
        // Get event point in ContentBox
        Point pnt = anEvent.getPoint(_cbox);
        View view = ViewUtils.getDeepestChildAt(_cbox, pnt.x, pnt.y);

        // Get deepest guest view (child of ViewHost)
        while (view!=null && !view.isGuest() && view.getParent()!=_cbox)
            view = view.getParent();

        // If not found, constrain to Editor.Content
        if (view==null || view==_cbox)
            view = getContent();

        // If point close to edge, setSpotView
        Point pnt2 = view.parentToLocal(pnt.x, pnt.y, _cbox);
        if (pnt2.x>view.getWidth()-6)
            setSelSpot(view, Order.AFTER);
        else if (pnt2.x<6)
            setSelSpot(view, Order.BEFORE);

        // Select new view
        else setSelView(view);
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
        if (isFocused() && isSelSpot() && !_hideSpot) {
            Shape shape = getSelSpotShape();
            aPntr.setPaint(SEL_COLOR); aPntr.setStroke(Stroke.Stroke1);
            aPntr.draw(shape);
        }

        // Repaint selected view
        if (sview.getRotate()==0) {
            Point pnt = sview.getParent().localToParent(sview.getX(), sview.getY(), this);
            aPntr.translate(pnt.x, pnt.y);
            ViewUtils.paintAll(sview, aPntr);
        }
    }

    /**
     * Returns the Shape used to paint SelSpot.
     */
    private Shape getSelSpotShape()
    {
        View sview = _spotView;
        Rect bnds = sview.localToParent(sview.getBoundsShape(), this).getBounds();
        double x = _spotOrder==Order.BEFORE ? bnds.x - 2 : bnds.getMaxX() + 2;
        Rect rect = new Rect(x, bnds.y-1, 0, bnds.height+2);
        return rect;
    }

    /**
     * Returns the counterpart to the SelSpot View.
     */
    private View getSelSpotOther()
    {
        if (_spotView==null || _spotOrder==null || _spotOrder==Order.ON) return null;
        ParentView par = _spotView.getParent(); if (par==null) return null;
        int ind = _spotView.indexInParent();
        if (_spotOrder==Order.BEFORE)
            return ind>0 ? par.getChild(ind-1) : null;
        return ind+1<par.getChildCount() ? par.getChild(ind+1) : null;
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
        View view = getSelOrSuperSelView();
        if (view==getContent()) { ViewUtils.beep(); return; }

        // Get xml for selected shapes, and get as string
        XMLElement xml = new ViewArchiver().writeToXML(view);
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
        if (cb.hasData(SNAP_XML_TYPE)) {

            // Get bytes, unarchive view and add
            byte bytes[] = cb.getDataBytes(SNAP_XML_TYPE);
            View view = new ViewArchiver().getView(bytes);
            addView(view);
        }
    }

    /**
     * Handles editor delete operation.
     */
    public void delete()
    {
        // Get selected view and parent
        View sview = getSelOrSuperSelView();
        ParentView par = sview.getParent();

        // Get par as host (just return if not host) and remove guest
        ViewHost host = sview.getHost(); if (host==null) { ViewUtils.beep(); return; }
        int ind = sview.indexInHost();
        host.removeGuest(sview);

        // Set new selected view
        if (host.getGuestCount()>0)
            setSelView(host.getGuest(ind<host.getGuestCount()? ind : ind -1));
        else setSelView(par);

        /*ParentView par = sview.getParent();
        int ind = sview.indexInParent();
        if(par instanceof ChildView) { ChildView cview = (ChildView)par;
            cview.removeChild(sview);
            if(cview.getChildCount()>0)
                setSelView(ind<cview.getChildCount()? cview.getChild(ind) : cview.getChild(ind-1));
            else setSelView(cview);
        }*/
    }

    /**
     * Handles SelectAll.
     */
    public void selectAll()
    {
        ViewUtils.beep();
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

    private Runnable _saveChangesRun, _scrShared = () -> saveUndoerChanges();
}