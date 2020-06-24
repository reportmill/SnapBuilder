package snapbuild.app;
import snap.geom.Pos;
import snap.geom.Rect;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.gfx.Image;
import snap.view.*;
import snap.viewx.TextPane;

/**
 * A ViewOwner subclass to provide UI to search and select images.
 */
public class FlatIconPanel extends ViewOwner {

    // The ImageItems
    private FlatIconItem _items[];

    // The ScrollView to show items
    private ScrollView  _scrollView;

    // The view to show items
    private GridView _itemsView;

    // Constants
    private static Color ITEM_VIEW_MOUSE_OVER_COLOR = Color.CYAN.blend(ViewUtils.getBackFill().getColor(), .6);

    /**
     * Initialize UI.
     */
    protected void initUI()
    {
        // Get/configure SearchText: radius, prompt, image, animation
        TextField searchText = getView("SearchTextField", TextField.class);
        searchText.setRadius(10);
        searchText.setPromptText("Search");
        searchText.getLabel().setImage(Image.get(TextPane.class, "Find.png"));
        TextField.setBackLabelAlignAnimatedOnFocused(searchText, true);
        //searchText.addEventFilter(e -> ViewUtils.runLater(() -> textFieldKeyTyped(e)), KeyPress);

        _itemsView = new GridView();
        _itemsView.setPrefHeight(256);

        _scrollView = getView("ScrollView", ScrollView.class);
        _scrollView.setFillWidth(true);
        _scrollView.setContent(_itemsView);
        _scrollView.getScroller().addPropChangeListener(pc -> scrollBoundsDidChange(),
            Scroller.ScrollX_Prop, Scroller.ScrollY_Prop, View.Width_Prop, View.Height_Prop);

        getView("FlatIconLabel", Label.class).setTextFill(Color.DARKGRAY);
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
        ProgressBar pbar = new ProgressBar();
        pbar.setPrefSize(100, 20);
        _itemsView.addChild(pbar);
        pbar.setIndeterminate(true);

        // Get items in background thread
        new Thread(() -> {
            System.out.println("Image search start: " + aString);
            FlatIconItem[] items = FlatIcon.SHARED.getImageItemsForSearchString(aString);
            System.out.println("Image search completed: " + items.length + " items found");
            runLater(() -> imageSearchGotItems(items));
        }).start();
    }

    /**
     * Called on app thread when items are loaded.
     */
    private void imageSearchGotItems(FlatIconItem[] theItems)
    {
        // Cap items at 10 for now
        //if (theItems.length>10) theItems = Arrays.copyOf(theItems, 10);

        // Remove items (ProgressBar)
        _itemsView.removeChildren();

        // Set items
        _items = theItems;

        // Iterate over items and create/add ImageItem
        for (FlatIconItem item : _items) {
            ItemView iview = new ItemView(item);
            _itemsView.addChild(iview);
        }

        runLater(() -> scrollBoundsDidChange());
    }

    /**
     * Called when the ScrollView scroll bounds change to activate items that need sample images.
     */
    private void scrollBoundsDidChange()
    {
        // Get visible rect and extend down
        Rect bounds = _itemsView.getVisRect();
        bounds.height += 500;

        ItemView children[] = _itemsView.getViewList().getViewsIntersectingShape(bounds, ItemView.class);
        for (ItemView child : children)
            child.setActive(true);
    }

    /**
     * Called when an item view is clicked.
     */
    protected void itemWasClicked(FlatIconItem anItem)
    {

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
            Image img = getLoadingImage();
            ImageView iview = new ImageView(img);
            iview.setPrefSize(64, 64);
            setGraphic(iview);
            setPrefSize(96, 96);
            addEventHandler(e -> itemViewMouseEnteredOrExited(e), View.MouseEnter, View.MouseExit);
            addEventHandler(e -> itemViewMouseClicked(e), View.MouseRelease);
        }

        /**
         * Sets whether item should have real sample image.
         */
        public void setActive(boolean aValue)
        {
            if (aValue==_active) return;
            _active = aValue;

            // Load image
            new Thread(() -> {
                Image img = _item.getSample();
                if (img.isLoaded())
                    ViewUtils.runLater(() -> setSampleImage(img));
                else img.addLoadListener(() -> setSampleImage(_item.getSample()));
            }).start();
        }

        protected void setSampleImage(Image anImage)
        {
            ImageView iview = new ImageView(anImage);
            iview.setPrefSize(64, 64);
            setGraphic(iview);
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
            if (anEvent.isMouseClick() && anEvent.getClickCount()==2) {
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
        if (_loadingImage!=null) return _loadingImage;
        TextArea text = new TextArea();
        text.setPadding(10,10,10,10);
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
