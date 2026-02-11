package snapbuild.app;
import snap.geom.Pos;
import snap.geom.Rect;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.gfx.Image;
import snap.view.*;
import snap.viewx.TextPane;
import java.util.List;
import java.util.function.Consumer;

/**
 * A ViewOwner subclass to provide UI to search and select images.
 */
public class FlatIconPanel extends ViewOwner {

    // The ImageItems
    private List<FlatIconItem> _items;

    // The ScrollView to show items
    private ScrollView _scrollView;

    // The view to show items
    private GridView _itemsView;

    // A consumer to handle item selected
    Consumer<FlatIconItem> _itemSelectedHandler;

    // Constants
    private static Color ITEM_VIEW_MOUSE_OVER_COLOR = Color.CYAN.blend(ViewUtils.getBackFill().getColor(), .6);

    /**
     * Initialize UI.
     */
    protected void initUI()
    {
        // Get/configure SearchText: radius, prompt, image, animation
        TextField searchText = getView("SearchTextField", TextField.class);
        searchText.setBorderRadius(10);
        searchText.setPromptText("Search");
        searchText.getLabel().setImage(Image.getImageForClassResource(TextPane.class, "Find.png"));
        ViewAnimUtils.configureTextFieldImageToAnimateLeftOnFocused(searchText);

        _itemsView = new GridView();

        _scrollView = getView("ScrollView", ScrollView.class);
        _scrollView.setFillWidth(true);
        _scrollView.setContent(_itemsView);
        _scrollView.getScroller().addPropChangeListener(pc -> handleScrollBoundsChange(),
                Scroller.ScrollX_Prop, Scroller.ScrollY_Prop, View.Width_Prop, View.Height_Prop);

        getView("FlatIconLabel", Label.class).setTextColor(Color.DARKGRAY);
    }

    /**
     * Respond UI.
     */
    @Override
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle SearchTextField
        if (anEvent.equals("SearchTextField")) {
            imageSearchStart(anEvent.getStringValue());
        }
    }

    /**
     * Starts an image search.
     */
    private void imageSearchStart(String aString)
    {
        // Remove items
        _itemsView.removeChildren();

        // Add ProgressBar
        ProgressBar progressBar = new ProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setPrefSize(100, 20);
        _itemsView.addChild(progressBar);

        // Get items in background thread
        new Thread(() -> {
            System.out.println("Image search start: " + aString);
            List<FlatIconItem> items = FlatIcon.SHARED.getImageItemsForSearchString(aString);
            int itemCount = items != null ? items.size() : -1;
            System.out.println("Image search completed: " + itemCount + " items found");
            runLater(() -> handleImageSearchFinished(items));
        }).start();
    }

    /**
     * Called on app thread when items are loaded.
     */
    private void handleImageSearchFinished(List<FlatIconItem> theItems)
    {
        // Remove items (ProgressBar)
        _itemsView.removeChildren();

        // Set items
        _items = theItems;

        // Iterate over items and create/add ImageItem
        for (FlatIconItem item : _items) {
            ItemView itemView = new ItemView(item);
            _itemsView.addChild(itemView);
        }
    }

    /**
     * Called when the ScrollView scroll bounds change to activate items that need sample images.
     */
    private void handleScrollBoundsChange()
    {
        // Get visible rect and extend down
        Rect bounds = _itemsView.getVisibleBounds();
        bounds.height += 500;

        ItemView[] children = _itemsView.getChildren().getViewsIntersectingShape(bounds, ItemView.class);
        for (ItemView child : children)
            child.setActive(true);
    }

    /**
     * Returns the handler called when item is selected.
     */
    public Consumer<FlatIconItem> getItemSelectedHandler()  { return _itemSelectedHandler; }

    /**
     * Sets the handler called when item is selected.
     */
    public void setItemSelectedHandler(Consumer<FlatIconItem> aHandler)  { _itemSelectedHandler = aHandler; }

    /**
     * Called when an item view is clicked.
     */
    protected void itemWasClicked(FlatIconItem anItem)
    {
        if (getItemSelectedHandler() != null)
            _itemSelectedHandler.accept(anItem);
    }

    /**
     * A view subclass to represent an item.
     */
    private class ItemView extends Label {

        // The item
        private FlatIconItem _item;

        // Whether item needs to have real image
        private boolean _active;

        /**
         * Constructor.
         */
        public ItemView(FlatIconItem anItem)
        {
            _item = anItem;
            setAlign(Pos.CENTER);
            Image loadingImage = getLoadingImage();
            ImageView imageView = new ImageView(loadingImage);
            imageView.setPrefSize(64, 64);
            setGraphic(imageView);
            setPrefSize(96, 96);
            addEventHandler(e -> itemViewMouseEnteredOrExited(e), View.MouseEnter, View.MouseExit);
            addEventHandler(e -> itemViewMouseClicked(e), View.MouseRelease);
        }

        /**
         * Sets whether item should have real sample image.
         */
        public void setActive(boolean aValue)
        {
            if (aValue == _active) return;
            _active = aValue;

            // Load image
            new Thread(() -> {
                Image sampleImage = _item.getSample();
                if (sampleImage.isLoaded())
                    ViewUtils.runLater(() -> setSampleImage(sampleImage));
                else sampleImage.addLoadListener(() -> setSampleImage(_item.getSample()));
            }).start();
        }

        protected void setSampleImage(Image anImage)
        {
            ImageView imageView = new ImageView(anImage);
            imageView.setPrefSize(64, 64);
            setGraphic(imageView);
        }

        private void itemViewMouseEnteredOrExited(ViewEvent anEvent)
        {
            if (anEvent.isMouseEnter())
                setFill(ITEM_VIEW_MOUSE_OVER_COLOR);
            else if (anEvent.isMouseExit())
                setFill(null);
        }

        private void itemViewMouseClicked(ViewEvent anEvent)
        {
            if (anEvent.isMouseClick() && anEvent.getClickCount() == 2) {
                itemWasClicked(_item);
            }
        }
    }

    /**
     * Returns the stand-in image for loading.
     */
    private static Image _loadingImage;

    private static Image getLoadingImage()
    {
        if (_loadingImage != null) return _loadingImage;
        TextArea text = new TextArea();
        text.setPadding(10, 10, 10, 10);
        text.setWrapLines(true);
        text.setAlign(Pos.CENTER);
        text.setText("Image loading...");
        text.setSize(64, 64);
        text.setFont(Font.Arial10);
        Image img = ViewUtils.getImageForScale(text, 1);
        return _loadingImage = img;
    }

    public static void main(String[] args)
    {
        FlatIconPanel fi = new FlatIconPanel();
        fi.getWindow().setPrefSize(500, 500);
        fi.setWindowVisible(true);
    }
}
