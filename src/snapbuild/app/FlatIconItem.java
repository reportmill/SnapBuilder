package snapbuild.app;
import snap.geom.Pos;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.gfx.Image;
import snap.util.Convert;
import snap.util.JsonNode;
import snap.util.JsonObject;
import snap.view.TextArea;
import snap.view.ViewUtils;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A class to hold an image item.
 */
public class FlatIconItem {

    // The image item node
    private JsonObject _itemNode;

    // The Id
    private int _id;

    // The description
    private String _desc;

    // The image URLs
    private Map<Integer, String> _pngURLs = new LinkedHashMap<>();

    // The sample image
    private Image _sample;

    // Constants
    public static final int SAMPLE_SIZE = 32;

    /**
     * Constructor.
     */
    public FlatIconItem(JsonObject aNode)
    {
        _itemNode = aNode;

        // Get Id
        String idStr = _itemNode.getStringValue("id");
        _id = Convert.intValue(idStr);

        // Get description
        _desc = _itemNode.getStringValue("description");

        // Get images.png node and pngCount
        JsonObject imagesNode = (JsonObject) _itemNode.getValue("images");
        JsonObject pngNode = imagesNode; //(JSObject) imagesNode.getValue("png");
        Map<String, JsonNode> keyValues = pngNode.getKeyValues();

        // Iterate over PNGs and add to PNG_URLs
        for (Map.Entry<String, JsonNode> entry : keyValues.entrySet()) {
            String key = entry.getKey();
            JsonNode sizeNode = entry.getValue();
            int size = Convert.intValue(key);
            String urls = sizeNode.getValueAsString();
            _pngURLs.put(size, urls);
        }
    }

    /**
     * Returns the description.
     */
    public String getDescription()
    {
        return _desc;
    }

    /**
     * Returns the PNG URL for the given size.
     */
    public String getPNG_URL_ForSize(int aSize)
    {
        String last = null;
        for (Integer i : _pngURLs.keySet()) {
            last = _pngURLs.get(i);
            if (i >= aSize)
                return last;
        }

        return last;
    }

    /**
     * Returns the image for given size.
     */
    public Image getSample()
    {
        if (_sample != null) return _sample;

        String url = getPNG_URL_ForSize(SAMPLE_SIZE);
        if (url == null)
            return _sample = getFailImage();


        _sample = url != null ? Image.getImageForSource(url) : getFailImage();

        if (_sample.isLoaded())
            sampleImageLoaded();
        else _sample.addLoadListener(() -> sampleImageLoaded());

        return _sample;
    }

    /**
     * Called to adjust Sample image to make sure it's sample size.
     */
    private void sampleImageLoaded()
    {
        if (_sample.getPixWidth() != SAMPLE_SIZE)
            _sample = _sample.copyForSize(SAMPLE_SIZE, SAMPLE_SIZE);
    }

    /**
     * Returns the image for given size.
     */
    public Image getSample2()
    {
        if (_sample != null) return _sample;

        Image img = FlatIcon.SHARED.getImageForIdAndSize(_id, SAMPLE_SIZE);

        if (img == null)
            getFailImage();

        return _sample = img;
    }

    /**
     * Returns a stand-in image.
     */
    public Image getFailImage()
    {
        TextArea text = new TextArea();
        text.setBorder(Color.BLACK, 1);
        text.setPadding(10, 10, 10, 10);
        text.setWrapLines(true);
        text.setAlign(Pos.CENTER);
        text.setText("Image not found: " + _desc);
        text.setSize(128, 128);
        text.setFont(Font.Arial10);
        return ViewUtils.getImageForScale(text, 1);
    }
}
