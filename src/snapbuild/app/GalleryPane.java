package snapbuild.app;
import snap.gfx.Image;
import snap.util.URLUtils;
import snap.view.*;
import snap.viewx.TextPane;
import java.util.*;

/**
 * Manages the UI for Gallery of views and arrange controls (move up/down, group/ungroup).
 */
public class GalleryPane extends ViewController {

    // The editor pane
    protected EditorPane _epane;

    // The GalleryView
    private GalleryView _galleryView;

    // The FlatIconPanel
    private FlatIconPanel _flatIcon;

    /**
     * Creates a new GalleryPane.
     */
    public GalleryPane(EditorPane anEP)
    {
        _epane = anEP;
        _flatIcon = new FlatIconPanel();
        _flatIcon.setItemSelectedHandler(item -> {
                Image img = item.getSample();
                _epane.getEditor().addImageToContent(img);
        });
    }

    /**
     * Returns the editor.
     */
    public Editor getEditor()
    {
        return _epane.getEditor();
    }

    /**
     * Initialize UI.
     */
    protected void initUI()
    {
        // Add Collapser with label for ArrangeBox
        View arrangeBox = getView("ArrangeBox");
        CollapseView arrangeCollapser = CollapseView.replaceViewWithCollapseView(arrangeBox, "Arrange Views");
        arrangeCollapser.setCollapsed(true);

        // Add Collapser with label for ViewsBox
        View viewsBox = getView("ViewsBox");
        CollapseView viewCollapser = CollapseView.replaceViewWithCollapseView(viewsBox, "Select View");
        viewCollapser.setGroupForName("GalleryPane");
        viewCollapser.setFirstFocus(getView("SearchTextField"));

        // Get/configure SearchText: radius, prompt, image, animation
        TextField searchText = getView("SearchTextField", TextField.class);
        searchText.getLabel().setImage(Image.getImageForClassResource(TextPane.class, "Find.png"));
        ViewAnimUtils.configureTextFieldImageToAnimateLeftOnFocused(searchText);
        searchText.addEventHandler(this::handleSearchTextKeyTypeEvent, KeyType);

        _galleryView = getView("GalleryView", GalleryView.class);
        _galleryView._galleryPane = this;

        // Add FlatIcon UI
        ColView colView = getUI(ColView.class);
        ColView flatIconView = _flatIcon.getUI(ColView.class);
        flatIconView.setGrowHeight(true);
        colView.addChild(flatIconView);
        CollapseView imageSearchCollapse = CollapseView.replaceViewWithCollapseView(flatIconView, "Select Image");
        imageSearchCollapse.setCollapsed(true);
        imageSearchCollapse.setGroupForName("GalleryPane");
        imageSearchCollapse.setFirstFocus(_flatIcon.getView("SearchTextField"));
    }

    @Override
    protected void resetUI()
    {
        // Update DocButton
        Editor editor = getEditor();
        View sview = editor.getSelView();
        setViewText("DocButton", sview != null ? sview.getClass().getSimpleName() + " Doc" : "SnapKit Doc");

        // Update ChangeHostButton.Enabled
        setViewEnabled("ChangeHostButton", getEditor().getSelView() instanceof ViewHost);
    }

    @Override
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle SearchTextField
        if (anEvent.equals("SearchTextField"))
            handleSearchTextField();

        // Handle DocButton
        if (anEvent.equals("DocButton")) {
            URLUtils.openURL(getJavaDocURL());
        }

        // Handle MoveUpButton, MoveDownButton, MoveOutButton, GroupInButton, UngroupButton, ChangeHostButton
        if (anEvent.equals("MoveUpButton")) EditorUtils.moveViewUp(getEditor());
        if (anEvent.equals("MoveDownButton")) EditorUtils.moveViewDown(getEditor());
        if (anEvent.equals("MoveOutButton")) EditorUtils.moveViewOut(getEditor());
        if (anEvent.equals("GroupInButton")) EditorUtils.groupView(getEditor());
        if (anEvent.equals("UngroupButton")) EditorUtils.ungroupView(getEditor());
        if (anEvent.equals("ChangeHostButton")) EditorUtils.changeHost(getEditor());
    }

    /**
     * Returns the JavaDoc url for currently selected view.
     */
    private String getJavaDocURL()
    {
        Editor editor = getEditor();
        View view = editor.getSelView();
        if (view == null) return "http://reportmill.com/snap1/javadoc/";
        String cname = view.getClass().getName();
        return "http://reportmill.com/snap1/javadoc/index.html?" + cname.replace('.', '/') + ".html";
    }

    /**
     * Handle SearchTextField changes.
     */
    private void handleSearchTextField()
    {
        // Get prefix text and current selection
        TextField searchText = getView("SearchTextField", TextField.class);
        String text = searchText.getText();

        // Look for possible completion
        List<GalleryView.ItemView> items = getItemsForPrefix(text);
        GalleryView.ItemView item = !items.isEmpty() ? items.get(0) : null;

        // If completion available, set completion text
        if (item != null) {
            _epane.getEditor().addViewToContentForViewClass(item.getContent().getClass());
        }

        // Clear SearchTextField
        setViewText("SearchTextField", null);
    }

    /**
     * Called after SearchText has KeyType.
     */
    private void handleSearchTextKeyTypeEvent(ViewEvent anEvent)
    {
        // Get prefix text and current selection
        TextField searchText = getView("SearchTextField", TextField.class);
        String text = searchText.getText();

        // Look for possible completion
        List<GalleryView.ItemView> items = getItemsForPrefix(text);
        String item = !items.isEmpty() ? items.get(0).getName() : null;

        // If completion available, set completion text
        if (item != null)
            searchText.setCompletionText(item);
    }

    private List<GalleryView.ItemView> getItemsForPrefix(String prefixStr)
    {
        List<GalleryView.ItemView> galleryItems = (List<GalleryView.ItemView>) (List<?>) _galleryView.getChildren();
        if (prefixStr.isEmpty()) {
            for (GalleryView.ItemView item : galleryItems) {
                item.setVisible(true);
                item.setManaged(true);
            }
            return Collections.EMPTY_LIST;
        }

        String prefixLC = prefixStr.toLowerCase();
        List<GalleryView.ItemView> prefixGalleryItems = new ArrayList<>();

        for (GalleryView.ItemView galleryItem : galleryItems) {
            if (galleryItem.getName().toLowerCase().contains(prefixLC)) {
                prefixGalleryItems.add(galleryItem);
                galleryItem.setVisible(true);
            }
            else galleryItem.setVisible(false);
            galleryItem.setManaged(galleryItem.isVisible());
        }

        // Sort and return
        prefixGalleryItems.sort((o1, o2) -> compareForPrefix(o1.getName(), o2.getName(), prefixLC));
        return prefixGalleryItems;
    }

    private int compareForPrefix(String o1, String o2, String prefixStr)
    {
        boolean b1 = o1.toLowerCase().startsWith(prefixStr);
        boolean b2 = o2.toLowerCase().startsWith(prefixStr);
        if (b1 ^ b2)
            return b1 ? -1 : 1;
        return o1.compareToIgnoreCase(o2);
    }
}