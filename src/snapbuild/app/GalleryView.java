package snapbuild.app;
import snap.view.*;

/**
 * A View to hold Gallery items.
 */
public class GalleryView extends ParentView {

    // The GalleryPane
    GalleryPane _galleryPane;

    /**
     * Creates new GalleryView.
     */
    public GalleryView()
    {
        // Basic Classes
        Class<? extends View>[] classes = new Class[]{Label.class, Button.class, ToggleButton.class, CheckBox.class,
                RadioButton.class, TextField.class, ComboBox.class, Slider.class, ThumbWheel.class,
                ProgressBar.class, Spinner.class, ArrowView.class,

                // Containers
                TextView.class, TitleView.class, TabView.class, ScrollView.class, SplitView.class,

                // Lists
                ListView.class, TableView.class, TreeView.class, BrowserView.class,

                // Simple containers
                BoxView.class, ColView.class, RowView.class, BorderView.class, View.class

                // Graphics
                //ImageView.class, RectView.class, PathView.class, ArcView.class, StringView.class DocView.class, PageView.class
        };

        for (Class<? extends View> cls : classes)
            addChild(new ItemView(cls));
    }

    /**
     * Override to return column layout.
     */
    @Override
    protected ViewLayout<?> getViewLayoutImpl()  { return new ColViewLayout<>(this, true); }

    /**
     * A class to show individual item.
     */
    public class ItemView extends BoxView {

        /**
         * Create new ItemView.
         */
        public ItemView(Class<? extends View> aCls)
        {
            // Configure this view
            //setBorder(Color.LIGHTGRAY,1);
            enableEvents(MousePress);

            setName(aCls.getSimpleName());
            setPadding(10, 10, 10, 10);
            setMinHeight(40);

            // Create item view, configure and add
            View view = null;
            try { view = aCls.newInstance(); }
            catch (Exception e) { throw new RuntimeException(e); }
            view.setMinSize(24, 12);
            view.setPickable(false);
            ViewHpr.getHpr(view).configureGallery(view);
            setContent(view);
        }

        /**
         * Handle events.
         */
        protected void processEvent(ViewEvent anEvent)
        {
            if (anEvent.isMousePress())
                _galleryPane._epane.getEditor().addViewToContentForViewClass(getContent().getClass());
        }
    }
}