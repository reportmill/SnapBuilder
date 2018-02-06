package snapbuild.app;
import snap.gfx.*;
import snap.util.XMLElement;
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
    
    // Constants for properties
    public static final String SelView_Prop = "SelView";

/**
 * Creates the Editor view.
 */    
public Editor()
{
    // Configure this view
    setAlign(Pos.CENTER);
    setFill(ViewUtils.getBackDarkFill());
    enableEvents(MouseRelease);
    
    // Configure ContentBox
    _cbox = new BoxView(); _cbox.setFillWidth(true); _cbox.setFillHeight(true); //_cbox.setMinSize(400,400);
    _cbox.setFill(ViewUtils.getBackFill());
    _cbox.setPickable(false);
    addChild(_cbox);
    
    // Set default content
    RowView row = new RowView(); row.setPadding(4,4,4,4); row.setSpacing(4); row.setGrowWidth(true);
    ColView col = new ColView(); col.setPadding(4,4,4,4); col.setSpacing(4); col.addChild(row);
    setContent(col);
    setSelView(row);
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
    //getUndoer().reset();
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

}