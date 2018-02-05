package snapbuild.app;
import snap.gfx.*;
import snap.view.*;

/**
 * A view to hold the UI.
 */
public class Editor extends ParentView {
    
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
    
    // Configure ContentBox
    _cbox = new BoxView(); _cbox.setFillWidth(true); _cbox.setFillHeight(true); //_cbox.setMinSize(400,400);
    _cbox.setFill(ViewUtils.getBackFill());
    addChild(_cbox);
    
    // Set default content
    RowView row = new RowView(); row.setPadding(4,4,4,4); row.setSpacing(4);
    ColView col = new ColView(); col.setPadding(4,4,4,4); col.setSpacing(4); col.addChild(row);
    setContent(col);
    setSelView(row);
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
}

/**
 * Override.
 */
protected double getPrefWidthImpl(double aH)  { return BoxView.getPrefWidth(this, _cbox, aH); }

/**
 * Override.
 */
protected double getPrefHeightImpl(double aW)  { return BoxView.getPrefHeight(this, _cbox, aW); }

/**
 * Override.
 */
protected void layoutImpl()  { BoxView.layout(this, _cbox, null, false, false); }

}