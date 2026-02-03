package snapbuild.app;
import snap.geom.*;
import snap.gfx.*;
import snap.props.DeepChangeListener;
import snap.props.PropChange;
import snap.props.UndoSet;
import snap.props.Undoer;
import snap.util.*;
import snap.view.*;
import snap.web.*;

/**
 * A view to hold the UI.
 */
public class Editor extends ParentView {

    // The Source URL
    protected WebURL _url;

    // The context box
    private BoxView _contentBox;

    // The undoer
    private Undoer _undoer;

    // The editor selection
    private EditorSel _sel = new EditorSel(this);

    // The Styler
    private EditorStyler _styler = new EditorStyler(this);

    // The default CopyPaster
    private EditorCopyPaster _copyPaster;

    // The default DragDropper
    private EditorDragDropper _dragDropper;

    // The DeepChangeListener
    DeepChangeListener _contentDeepChangeLsnr = (src, pce) -> contentDidDeepChange(src, pce);

    // A Shared updater to kick off save
    private WebFile.Updater _fileUpdater = file -> updateFile();

    // Constants for properties
    public static final String SelView_Prop = "SelView";

    // Constants
    public static Color BACK_FILL = new Color(165, 179, 216).brighter(); // ViewUtils.getBackDarkFill()
    private Color SEL_COLOR = new Color(.3, .3, 1, .33);
    private Stroke SEL_STROKE = new Stroke(3);

    /**
     * Creates the Editor view.
     */
    public Editor()
    {
        // Configure this view
        setAlign(Pos.CENTER);
        setPadding(15, 15, 15, 15);
        setFill(BACK_FILL);
        enableEvents(MouseRelease);
        enableEvents(DragEvents);
        setFocusable(true);
        setFocusWhenPressed(true);

        // Configure ContentBox
        _contentBox = new BoxView();
        _contentBox.setFillWidth(true);
        _contentBox.setFillHeight(true);
        _contentBox.setFill(ViewUtils.getBackFill());
        _contentBox.setFill(BACK_FILL.blend(Color.WHITE, .8));
        _contentBox.setBorder(new Color("#99"), 1);
        _contentBox.setEffect(new ShadowEffect());
        _contentBox.setPickable(false);
        _contentBox.setMinSize(200, 200);
        _contentBox.addDeepChangeListener(_contentDeepChangeLsnr);
        addChild(_contentBox);

        // Set default content
        setContent(new ColView());

        // Create undoer
        _undoer = new Undoer();
        _undoer.setAutoSave(true);
    }

    /**
     * Returns the source URL.
     */
    public WebURL getSourceURL()  { return _url; }

    /**
     * Sets the source URL.
     */
    public void setSourceURL(WebURL aURL)
    {
        _url = aURL;
    }

    /**
     * Returns the source URL.
     */
    public WebFile getSourceFile(boolean doCreate)
    {
        if (_url == null) return null;
        WebFile file = _url.getFile();
        if (file == null && doCreate) file = _url.createFile(false);
        return file;
    }

    /**
     * Returns the content box.
     */
    public View getContentBox()  { return _contentBox; }

    /**
     * Returns the content view.
     */
    public View getContent()  { return _contentBox.getContent(); }

    /**
     * Sets the content view.
     */
    public void setContent(View aView)
    {
        _contentBox.setContent(aView);
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
    public void setSelView(View aView)
    {
        _sel.setSelView(aView);
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
    public void addViewToContentForViewClass(Class<? extends View> aCls)
    {
        // Create view from class, configure, add
        View view = null;
        try { view = aCls.newInstance(); }
        catch (Exception ignored) { }
        ViewHpr.getHpr(view).configure(view);
        addViewToContent(view);
    }

    /**
     * Adds a view to content.
     */
    public void addViewToContent(View aView)
    {
        // Get AddView and index
        EditorSel.ViewIndex addViewAndIndex = _sel.getSelHostViewAndIndex();
        View host = addViewAndIndex.view();
        int index = addViewAndIndex.index();

        // If selected view parent is host, add to it
        if (host != null)
            ((ViewHost) host).addGuest(aView, index);

            // Otherwise bail and complain
        else {
            ViewUtils.beep();
            return;
        }

        // Select view
        setSelView(aView);
    }

    /**
     * Adds a given view at given point.
     */
    public void addViewToContentAtPoint(View aView, Point aPoint)
    {
        EditorSel.ViewIndex viewIndex = getSel().getHostViewAndIndexForPoint(aPoint);
        View hostView = viewIndex.view();
        int index = viewIndex.index();
        ViewHost host = (ViewHost) hostView;
        host.addGuest(aView, index);
        setSelView(aView);
    }

    /**
     * Adds an image.
     */
    public void addImageToContent(Image anImage)
    {
        View selView = getSelView();
        if (selView instanceof ButtonBase)
            ((ButtonBase) selView).setImage(anImage);
        else if (selView instanceof Label)
            ((Label) selView).setImage(anImage);
        else {
            ImageView imageView = new ImageView(anImage);
            addViewToContent(imageView);
        }
    }

    /**
     * Returns the Styler.
     */
    public EditorStyler getStyler()  { return _styler; }

    /**
     * Returns the editor copy/paster.
     */
    public EditorCopyPaster getCopyPaster()
    {
        if (_copyPaster != null) return _copyPaster;
        return _copyPaster = new EditorCopyPaster(this);
    }

    /**
     * Standard clipboard cut functionality.
     */
    public void cut()
    {
        getCopyPaster().cut();
    }

    /**
     * Standard clipboard copy functionality.
     */
    public void copy()
    {
        getCopyPaster().copy();
    }

    /**
     * Standard clipbard paste functionality.
     */
    public void paste()
    {
        getCopyPaster().paste();
    }

    /**
     * Deletes all the currently selected views.
     */
    public void delete()
    {
        getCopyPaster().delete();
    }

    /**
     * Causes all the children of the current super selected view to become selected.
     */
    public void selectAll()
    {
        getCopyPaster().selectAll();
    }

    /**
     * Returns the editor copy/paster.
     */
    public EditorDragDropper getDragDropper()
    {
        if (_dragDropper != null) return _dragDropper;
        return _dragDropper = new EditorDragDropper(this);
    }

    /**
     * Sets whether editor is really doing editing.
     */
    public void setEditing(boolean aValue)
    {
        _contentBox.setPickable(true);
        disableEvents(MouseRelease);
        _contentBox.removeDeepChangeListener(_contentDeepChangeLsnr);
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
        WebFile sourceFile = getSourceFile(true);
        XMLElement xml = getContentXML();
        byte[] bytes = xml.getBytes();
        sourceFile.setBytes(bytes);
    }

    /**
     * Override to return box layout.
     */
    @Override
    protected ViewLayout getViewLayoutImpl()
    {
        return new BoxViewLayout(this, _contentBox, false, false);
    }

    /**
     * Override to handle events.
     */
    protected void processEvent(ViewEvent anEvent)
    {
        // Handle MouseEvent
        if (anEvent.isMouseRelease())
            mouseRelease(anEvent);

            // Handle DragEvent
        else if (anEvent.isDragEvent())
            getDragDropper().processDragEvent(anEvent);
    }

    /**
     * Called when there is a MouseRelease.
     */
    protected void mouseRelease(ViewEvent anEvent)
    {
        _sel.setSelForPoint(anEvent.getPoint());
    }

    /**
     * Override to repaint selected view with highlite.
     */
    protected void paintAbove(Painter aPntr)
    {
        // Get round rect for selected view
        View selView = getSelView();
        Rect bnds = selView.localToParent(selView.getBoundsShape(), this).getBounds();
        RoundRect rrect = new RoundRect(bnds.x - 1, bnds.y - 1, bnds.width + 2, bnds.height + 2, 3);

        // Paint selection for SelView
        if (selView != getContent()) {
            aPntr.setColor(SEL_COLOR);
            aPntr.setStroke(SEL_STROKE);
            aPntr.draw(rrect);
            aPntr.setStroke(Stroke.Stroke1);
        }

        // Repaint SelView so selection is behind
        if (selView.getRotate() == 0) {
            Point selViewXYInParent = selView.getParent().localToParent(selView.getX(), selView.getY(), this);
            aPntr.translate(selViewXYInParent.x, selViewXYInParent.y);
            ViewUtils.paintAll(selView, aPntr);
            aPntr.translate(-selViewXYInParent.x, -selViewXYInParent.y);
        }

        // Paint SelSpot caret (if needed)
        _sel.paintSel(aPntr);

        // Paint Drag
        getDragDropper().paintDrag(aPntr);
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
        Undoer undoer = getUndoer();
        if (undoer != null && undoer.getLastUndoSet() != null) {
            UndoSet undoSet = undoer.undo();
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
        Undoer undoer = getUndoer();
        if (undoer != null && undoer.getLastRedoSet() != null) {
            UndoSet redoSet = undoer.redo();
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
        if (aSel instanceof View)
            setSelView((View) aSel);
    }

    /**
     * Called when ContentBox has deep change.
     */
    protected void contentDidDeepChange(Object aView, PropChange aPC)
    {
        // Get source and prop name (if not View, just return)
        Object src = aPC.getSource();
        View propChangeView = src instanceof View ? (View) src : null;
        View view = (View) aView;
        if (view == null)
            return;
        String propName = aPC.getPropName();

        // Ignore properties: Showing, NeedsLayout
        if (propName == Parent_Prop) return;
        if (propName == Showing_Prop) return;
        if (propName == ParentView.Children_Prop) {
            if (!(view instanceof ViewHost))
                return;
        }

        // Ignore layout changes
        if (view instanceof ParentView parentView && parentView.isInLayout())
            return;
        if (propChangeView instanceof ParentView parentView && parentView.isInLayout())
            return;

        // If undoer exists, set selected objects and add property change
        Undoer undoer = getUndoer();
        if (undoer != null) {

            // If no changes yet, set selected objects
            if (undoer.getActiveUndoSet().isEmpty())
                undoer.setUndoSelection(getSelView()); //new ArrayList(getSelectedOrSuperSelectedViews())

            // Add property change
            undoer.addPropChange(aPC);

            // Set updator
            WebFile file = getSourceFile(false);
            if (file != null)
                file.setUpdater(undoer.hasUndos() ? _fileUpdater : null);
        }

        // Forward DeepChanges to EditorPane. Should have add/removeDeepChagneLister methods for this.
        //EditorPane ep = getEditorPane(); if(ep!=null) ep.resetLater();
        repaint();
    }

    /**
     * ContentBox.
     */
    private static class ContentBox extends BoxView {

        @Override
        protected void paintBack(Painter aPntr)
        {
            double w = getWidth();
            double h = getHeight();
            aPntr.setColor(Color.WHITE);
            aPntr.fillRect(0, 0, w, h);

            int GRID_SIZE = 15;
            aPntr.setColor(Color.BLUE.blend(Color.CYAN, .5).blend(Color.WHITE, .95));
            aPntr.setStroke(Stroke.Stroke1);
            for (double x = .5; x < w; x += GRID_SIZE)
                aPntr.drawLine(x, .5, x, h - 1);
            for (double y = .5; y < h; y += GRID_SIZE)
                aPntr.drawLine(.5, y, w - 1, y);
        }
    }
}