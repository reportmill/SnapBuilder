package snapbuild.app;
import java.util.*;

import snap.geom.Pos;
import snap.geom.Size;
import snap.geom.VPos;
import snap.gfx.*;
import snap.util.*;
import snap.view.*;
import snap.viewx.DialogBox;
import snap.viewx.DialogSheet;
import snap.web.WebResponse;
import snap.web.WebURL;

/**
 * A class to show samples.
 */
public class SamplesPane extends ViewOwner {

    // The editor pane
    private EditorPane _editorPane;

    // The selected index
    private int _selIndex;

    // The dialog box
    private DialogSheet _dialogSheet;

    // The shared document names
    private static String[] _docNames;

    // The shared document images
    private static Image[] _docImages;

    // The shared image paths
    private static String[] _imagePaths;

    // Constants
    private static final String SAMPLES_ROOT = "https://reportmill.com/snaptea/SnapBuilderSamples/";
    private static final String SAMPLES_EXT = ".snp";
    private static final String SAMPLES_LABEL = "Select a UI file:";
    private static final Size DOC_SIZE = new Size(102, 102);
    private static final Effect SHADOW = new ShadowEffect();
    private static final Effect SHADOW_SEL = new ShadowEffect(10, Color.get("#038ec3"), 0, 0);

    /**
     * Shows the samples pane.
     */
    public void showSamples(EditorPane anEP)
    {
        _editorPane = anEP;
        ChildView aView = (ChildView) anEP.getUI();

        _dialogSheet = new DialogSheet();
        _dialogSheet.setContent(getUI());
        _dialogSheet.showConfirmDialog(aView);
        _dialogSheet.addPropChangeListener(pc -> dialogBoxClosed(), DialogBox.Showing_Prop);
    }

    /**
     * Called when dialog box closed.
     */
    private void dialogBoxClosed()
    {
        if (_dialogSheet.isCancelled())
            return;
        _editorPane.getEditor().setContent(getDoc(_selIndex));
        _editorPane.getEditor().requestFocus();
    }

    /**
     * Creates UI.
     */
    protected View createUI()
    {
        // Create main ColView to hold RowViews for samples
        ColView colView = new ColView();
        colView.setName("ItemColView");
        colView.setSpacing(25);
        colView.setPadding(25, 15, 20, 15);
        colView.setAlign(Pos.TOP_CENTER);
        colView.setFillWidth(true);
        colView.setFill(new Color(.97, .97, 1d));
        colView.setBorder(Color.GRAY, 1);
        colView.setPrefWidth(557);

        // Add loading label
        Label loadLabel = new Label("Loading...");
        loadLabel.setFont(Font.Arial16.copyForSize(32).getBold());
        loadLabel.setTextColor(Color.GRAY);
        colView.addChild(loadLabel);

        // Create ScrollView
        ScrollView scroll = new ScrollView(colView);
        scroll.setPrefHeight(420);
        scroll.setShowHBar(false);
        scroll.setShowVBar(true);

        // Create "Select template" label
        Label selectLabel = new Label(SAMPLES_LABEL);
        selectLabel.setFont(Font.Arial16.copyForSize(20).getBold());

        // Create HeaderRow to hold SelectLabel
        RowView headerRow = new RowView();
        headerRow.addChild(selectLabel);

        // Create top level col view to hold HeaderRow and ColView
        ColView boxView = new ColView();
        boxView.setSpacing(8);
        boxView.setFillWidth(true);
        boxView.setChildren(headerRow, scroll);
        return boxView;
    }

    /**
     * Initialize UI.
     */
    protected void initUI()
    {
        if (_docNames == null)
            loadIndexFile();
        else buildUI();
    }

    /**
     * Starts loading.
     */
    private void loadIndexFile()
    {
        WebURL url = WebURL.getUrl(SAMPLES_ROOT + "index.txt");
        url.getResponseAndCall(resp -> indexFileLoaded(resp));
    }

    /**
     * Loads content.
     */
    private void indexFileLoaded(WebResponse aResp)
    {
        // If response is bogus, report it
        if (aResp.getCode() != WebResponse.OK) {
            runLater(() -> indexFileLoadFailed(aResp));
            return;
        }

        // Get text and break into lines
        String text = aResp.getText();
        String[] lines = text.split("\\s*\n\\s*");

        // Get names list from lines
        List<String> docNamesList = new ArrayList<>();
        for (String line : lines) {
            line = line.trim();
            if (line.length() > 0)
                docNamesList.add(line);
        }

        // Get DocNames from list
        _docNames = docNamesList.toArray(new String[0]);
        _docImages = new Image[_docNames.length];

        // Rebuild UI
        runLater(() -> buildUI());
    }

    /**
     * Loads failure condition.
     */
    private void indexFileLoadFailed(WebResponse aResp)
    {
        // Get error string and TextArea
        String str = "Failed to load index file.\n" + "Response code: " + aResp.getCodeString() + "\n" +
                "Exception: " + aResp.getException();
        TextArea textArea = new TextArea();
        textArea.setText(str);

        // Add to ColView
        ColView colView = getView("ItemColView", ColView.class);
        colView.setAlign(Pos.CENTER);
        colView.addChild(textArea);
    }

    /**
     * Loads samples.
     */
    private void buildUI()
    {
        // Get ItemColView and remove children
        ColView colView = getView("ItemColView", ColView.class);
        colView.removeChildren();

        // Create RowViews
        RowView rowView = null;
        for (int i = 0; i < _docNames.length; i++) {
            String name = _docNames[i];

            // Create/add new RowView for every three samples
            if (i % 3 == 0) {
                rowView = new RowView();
                rowView.setAlign(Pos.CENTER);
                colView.addChild(rowView);
            }

            // Create/add new ItemBox for index
            View itemBox = createItemBox(i);
            rowView.addChild(itemBox);
        }

        // Make sure all row views and image boxes are owned by ui
        for (View child : colView.getChildren())
            child.setOwner(this);

        // Load images
        loadImagesInBackground();
    }

    /**
     * Creates an item box for given index.
     */
    private ColView createItemBox(int anIndex)
    {
        // Create ImageViewX for sample
        ImageView imageView = new ImageView();
        imageView.setPrefSize(DOC_SIZE);
        imageView.setFill(Color.WHITE);
        imageView.setName("ImageView" + anIndex);

        // Create label for sample
        String name = getDocName(anIndex);
        Label label = new Label(name + SAMPLES_EXT);
        label.setFont(Font.Arial13);
        label.setPadding(3, 4, 3, 4);
        label.setLeanY(VPos.BOTTOM);

        // Create/add ItemBox for Sample and add ImageView + Label
        ColView itemBox = new ColView();
        itemBox.setPrefSize(175, 175);
        itemBox.setAlign(Pos.CENTER);
        itemBox.setPadding(0, 0, 8, 0);
        itemBox.setName("ItemBox" + anIndex);
        itemBox.addEventHandler(e -> itemBoxWasPressed(itemBox, e), MousePress);
        itemBox.setChildren(imageView, label);
        setItemBoxSelected(itemBox, anIndex == 0);

        // Return
        return itemBox;
    }

    /**
     * Configures an item box.
     */
    private void setItemBoxSelected(ColView itemBox, boolean isSelected)
    {
        // Set ImageView.Effect
        ImageView imageView = (ImageView) itemBox.getChild(0);
        imageView.setEffect(isSelected ? SHADOW_SEL : SHADOW);

        // Set Label Fill and TextColor
        Label oldLabel = (Label) itemBox.getChild(1);
        oldLabel.setFill(isSelected ? Color.BLUE : null);
        oldLabel.setTextColor(isSelected ? Color.WHITE : null);
    }

    /**
     * Called when template ItemBox is clicked.
     */
    private void itemBoxWasPressed(ColView anItemBox, ViewEvent anEvent)
    {
        // Get name and index of pressed ItemBox
        String name = anItemBox.getName();
        int index = StringUtils.intValue(name);

        // Set attributes of current selection back to normal
        ColView oldItemBox = getView("ItemBox" + _selIndex, ColView.class);
        setItemBoxSelected(oldItemBox, false);

        // Set attributes of new selection to selected effect
        setItemBoxSelected(anItemBox, true);

        // Set new index
        _selIndex = index;

        // If double-click, confirm dialog box
        if (anEvent.getClickCount() > 1) _dialogSheet.confirm();
    }

    /**
     * Returns the number of docs.
     */
    private static int getDocCount()
    {
        return _docNames.length;
    }

    /**
     * Returns the doc name at index.
     */
    private static String getDocName(int anIndex)
    {
        return _docNames[anIndex];
    }

    /**
     * Returns the doc at given index.
     */
    private static View getDoc(int anIndex)
    {
        // Get document name, URL string and URL
        String name = getDocName(anIndex);
        String urls = SAMPLES_ROOT + name + '/' + name + SAMPLES_EXT;
        WebURL url = WebURL.getUrl(urls);

        // Get bytes (complain if not found)
        byte[] bytes = url.getBytes();
        if (bytes == null) {
            System.err.println("SamplesPane.getDoc: Couldn't load " + url);
            return null;
        }

        // Return document
        ViewArchiver arch = new SamplesViewArchiver();
        arch.setSourceURL(url);
        View doc = arch.getViewForBytes(bytes);
        return doc;
    }

    /**
     * Returns the doc thumnail image at given index.
     */
    private Image getDocImage(int anIndex)
    {
        // If image already set, just return
        Image img = _docImages[anIndex];
        if (img != null) return img;

        // Get image name, URL string, and URL
        String name = getDocName(anIndex);
        String urls = SAMPLES_ROOT + name + '/' + name + ".png";
        WebURL imgURL = WebURL.getUrl(urls);

        // Create Image. Then make sure image is loaded by requesting Image.Native.
        img = _docImages[anIndex] = Image.getImageForSource(imgURL);
        img.getNative();
        return img;
    }

    /**
     * Loads the thumbnail image for each sample in background thread.
     */
    private void loadImagesInBackground()
    {
        new Thread(() -> loadImages()).start();
    }

    /**
     * Loads the thumbnail image for each sample in background thread.
     */
    private void loadImages()
    {
        // Iterate over sample names and load/set images
        for (int i = 0; i < getDocCount(); i++) {
            int index = i;
            Image img = getDocImage(i);
            runLater(() -> setImage(img, index));
        }
    }

    /**
     * Called after an image is loaded to set in ImageView in app thread.
     */
    private void setImage(Image anImg, int anIndex)
    {
        String name = "ImageView" + anIndex;
        ImageView iview = getView(name, ImageView.class);
        iview.setImage(anImg);
        iview.setPrefSize(-1, -1);
    }

    /**
     * Returns the image paths.
     */
    public static String[] getImagePaths()
    {
        if (_imagePaths != null) return _imagePaths;

        WebURL url = WebURL.getUrl(SAMPLES_ROOT + "images/index.txt");
        String pathsStr = url.getText();
        String[] pathLines = pathsStr.split("\\s*\n\\s*");
        List<String> pathsList = new ArrayList<>();
        for (String line : pathLines) {
            if (line.length() > 0)
                pathsList.add(line);
        }
        String[] paths = pathsList.toArray(new String[0]);
        return _imagePaths = paths;
    }

    /**
     * Returns an image path for given name.
     */
    public static String getImagePathForName(String aName)
    {
        String name = aName;
        int ind = name.lastIndexOf('/');
        if (ind >= 0) name = name.substring(ind + 1);

        for (String path : getImagePaths()) {
            if (path.endsWith(name))
                return path;
        }
        return null;
    }

    /**
     * A ViewArchiver that looks for images in samples dir.
     */
    private static class SamplesViewArchiver extends ViewArchiver {

        /**
         * Override to look for images in samples dir.
         */
        public Image getImage(String aPath)
        {
            String path = getImagePathForName(aPath);
            if (path != null) {
                String urls = SAMPLES_ROOT + "images/" + path;
                WebURL url = WebURL.getUrl(urls);
                return Image.getImageForSource(url);
            }

            // Do normal version
            return super.getImage(aPath);
        }
    }
}