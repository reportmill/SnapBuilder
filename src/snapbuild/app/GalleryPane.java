package snapbuild.app;
import snap.view.*;

/**
 * Manages the UI for Gallery of views and arrange controls (move up/down, group/ungroup).
 */
public class GalleryPane extends ViewOwner {

    // The editor pane
    EditorPane        _epane;
    
    // The GalleryView
    GalleryView       _galleryView;
    
/**
 * Creates a new GalleryPane.
 */
public GalleryPane(EditorPane anEP)
{
    _epane = anEP;
}

/**
 * Initialize UI.
 */
protected void initUI()
{
    _galleryView = getView("GalleryView", GalleryView.class);
    _galleryView._galleryPane = this;
}

}