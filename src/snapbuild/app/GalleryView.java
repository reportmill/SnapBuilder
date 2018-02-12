package snapbuild.app;
import snap.gfx.Color;
import snap.view.*;

/**
 * A custom class.
 */
public class GalleryView extends ParentView {
    
    // The GalleryPane
    GalleryPane       _galleryPane;
    
    // The Height of a GalleryItem
    double ITEM_HEIGHT = 60;

/**
 * Creates new GalleryView.
 */
public GalleryView()
{
    // Basic Classes
    Class <? extends View> classes[] = new Class[] { Label.class, Button.class, ToggleButton.class, CheckBox.class,
        RadioButton.class, TextField.class, Slider.class, ThumbWheel.class, ProgressBar.class, Spinner.class,
            
        // Containers
        ColView.class, RowView.class, TitleView.class, TabView.class, ScrollView.class, SplitView.class,
        BorderView.class,
            
        // Lists
        ListView.class, TableView.class, TreeView.class, BrowserView.class,
            
        // Graphics
        //ImageView.class, RectView.class, PathView.class, ArcView.class, StringView.class DocView.class, PageView.class
    };
    
    for(Class cls : classes)
        addChild(new ItemView(cls));
}
    
/**
 * Override to return preferred height of content.
 */
protected double getPrefHeightImpl(double aW)
{
    int colCount = (int)aW/150, childCount = getChildCount(); if(colCount==0) return 0;
    int rowCount = childCount/colCount + (childCount%colCount>0? 1 : 0);
    return rowCount*ITEM_HEIGHT;
}

/**
 * Actual method to layout children.
 */
protected void layoutImpl()
{
    double w = getWidth(), h = getHeight();
    
    int colCount = (int)w/150;
    double cw = Math.floor(w/colCount);
    
    
    double x = 0, y = 0;
    for(int i=0,iMax=getChildCount();i<iMax;i++) { View child = getChild(i);
        child.setBounds(x,y,cw,ITEM_HEIGHT);
        x += cw; if(x+cw>w) { x = 0; y += ITEM_HEIGHT; }
    }
}


/**
 * A class to show individual item.
 */
public class ItemView extends BoxView {
    
    /** Create new ItemView. */
    public ItemView(Class <? extends View> aCls)
    {
        // Configure this view
        setBorder(Color.LIGHTGRAY,1);
        enableEvents(MousePress);
        
        // Create item view, configure and add
        View view = null; try { view = aCls.newInstance(); } catch(Exception e) { }
        view.setMinSize(24,12); view.setPickable(false);
        ViewHpr.getHpr(view).configureGallery(view);
        setContent(view);
    }
    
    /** Handle events. */
    protected void processEvent(ViewEvent anEvent)
    {
        if(anEvent.isMousePress())
            _galleryPane._epane.getEditor().addView(getContent().getClass());
    }
}

}