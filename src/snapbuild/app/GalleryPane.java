package snapbuild.app;
import snap.gfx.Image;
import snap.view.*;
import snap.viewx.TextPane;
import java.util.*;

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
        // Get/configure SearchText: radius, prompt, image, animation
        TextField searchText = getView("SearchTextField", TextField.class);
        searchText.setRadius(10);
        searchText.setPromptText("Search");
        searchText.getLabel().setImage(Image.get(TextPane.class, "Find.png"));
        TextField.setBackLabelAlignAnimatedOnFocused(searchText, true);
        searchText.addEventFilter(e -> ViewUtils.runLater(() -> textFieldKeyTyped(e)), KeyPress);

        _galleryView = getView("GalleryView", GalleryView.class);
        _galleryView._galleryPane = this;
    }

    @Override
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle SearchTextField
        if(anEvent.equals("SearchTextField"))
            handleSearchTextField(anEvent);

    }

    /**
     * Handle SearchTextField changes.
     */
    public void handleSearchTextField(ViewEvent anEvent)
    {
        // Get prefix text and current selection
        TextField searchText = getView("SearchTextField", TextField.class);
        String text = searchText.getText();

        // Look for possible completion
        List<GalleryView.ItemView> items = getItemsForPrefix(text);
        GalleryView.ItemView item = items.size()>0 ? items.get(0) : null;

        // If completion available, set completion text
        if (item!=null) {
            _epane.getEditor().addView(item.getContent().getClass());
        }

        // Clear SearchTextField
        setViewText("SearchTextField", null);
    }

    /**
     * Called after TextField has KeyType.
     */
    protected void textFieldKeyTyped(ViewEvent anEvent)
    {
        // Get prefix text and current selection
        TextField searchText = getView("SearchTextField", TextField.class);
        String text = searchText.getText();

        // Look for possible completion
        List<GalleryView.ItemView> items = getItemsForPrefix(text);
        String item = items.size()>0 ? items.get(0).getName() : null;

        // If completion available, set completion text
        if (item!=null)
            searchText.setCompletionText(item);
    }

    private List<GalleryView.ItemView> getItemsForPrefix(String aPfx)
    {
        String pfx = aPfx.toLowerCase();
        List<GalleryView.ItemView> items = new ArrayList(Arrays.asList(_galleryView.getChildren()));
        if (pfx.length()==0) {
            for (GalleryView.ItemView item : items) {
                item.setVisible(true);
                item.setManaged(true);
            }
            return Collections.EMPTY_LIST;
        }

        for (GalleryView.ItemView item : items.toArray(new GalleryView.ItemView[0]))
        {
            if (pfx.length()>0 && !item.getName().toLowerCase().contains(pfx)) {
                items.remove(item);
                item.setVisible(false);
            }
            else item.setVisible(true);
            item.setManaged(item.isVisible());
        }
        Collections.sort(items, (o1,o2) -> compareForPrefix(o1.getName(), o2.getName(), pfx));
        return items;
    }

    private int compareForPrefix(String o1, String o2, String aPfx)
    {
        boolean b1 = o1.toLowerCase().startsWith(aPfx);
        boolean b2 = o2.toLowerCase().startsWith(aPfx);
        if (b1 ^ b2)
            return b1 ? -1 : 1;
        return o1.compareToIgnoreCase(o2);
    }
}