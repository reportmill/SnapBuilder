package snapbuild.app;
import snap.gfx.Color;
import snap.gfx.Image;
import snap.view.*;
import snap.viewx.TextPane;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        // Get/configure SearchComboBox
        ComboBox <GalleryView.ItemView> searchComboBox = getView("SearchComboBox", ComboBox.class);
        searchComboBox.setItemTextFunction(itm -> itm.getName());
        searchComboBox.getListView().setItemTextFunction(itm -> itm.getName() + " - " + itm.getName());
        searchComboBox.setPrefixFunction(s -> getItemsForPrefix(s));

        // Get/configure SearchComboBox.PopupList
        PopupList searchPopup = searchComboBox.getPopupList();
        searchPopup.setRowHeight(22); searchPopup.setPrefWidth(300); searchPopup.setMaxRowCount(15);
        searchPopup.setAltPaint(Color.get("#F8F8F8"));

        // Get/configure SearchText: radius, prompt, image, animation
        TextField searchText = searchComboBox.getTextField(); searchText.setRadius(8);
        searchText.setPromptText("Search"); searchText.getLabel().setImage(Image.get(TextPane.class, "Find.png"));
        TextField.setBackLabelAlignAnimatedOnFocused(searchText, true);

        _galleryView = getView("GalleryView", GalleryView.class);
        _galleryView._galleryPane = this;
    }

    @Override
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle SearchComboBox
        if(anEvent.equals("SearchComboBox"))
            handleSearchComboBox(anEvent);

    }

    /**
     * Handle SearchComboBox changes.
     */
    public void handleSearchComboBox(ViewEvent anEvent)
    {
        // Get selected file and/or text
        //WebFile file = (WebFile)anEvent.getSelItem();
        String text = anEvent.getStringValue();

        // If file available, open file
        //if(file!=null)
        //    getAppBrowser().setFile(file);

        // If text available, either open URL or search for string
//        else if(text!=null && text.length()>0) {
//            int colon = text.indexOf(':');
//            if(colon>0 && colon<6) {
//                WebURL url = WebURL.getURL(text);
//                getAppBrowser().setURL(url);
//            }
//            else {
//                getAppPane().getSearchPane().search(text);
//                getAppPane().setSupportTrayIndex(SupportTray.SEARCH_PANE);
//            }
//        }

        // Clear SearchComboBox
        setViewText("SearchComboBox", null);
    }

    private List<GalleryView.ItemView> getItemsForPrefix(String aPfx)
    {
        List<GalleryView.ItemView> items = new ArrayList(Arrays.asList(_galleryView.getChildren()));

        for (GalleryView.ItemView item : items.toArray(new GalleryView.ItemView[0]))
        {
            if (!item.getName().toLowerCase().startsWith(aPfx))
                items.remove(item);
        }
        return items;
    }
}