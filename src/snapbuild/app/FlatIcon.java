package snapbuild.app;
import snap.gfx.Image;
import snap.util.Convert;
import snap.util.JSArray;
import snap.util.JSObject;
import snap.web.*;
import java.util.*;

/**
 * A Class to work with FlatIcon.
 */
public class FlatIcon {

    // The access token for FlatIcon api: https://api.flaticon.com
    private static String _token;

    // Constants for FlatIcon api endpoints: https://api.flaticon.com/v3/docs/index.html#flaticon-api
    private static final String AUTHENTICATION = "https://api.flaticon.com/v3/app/authentication";
    private static final String GET_ICONS = "https://api.flaticon.com/v3/search/icons";
    private static final String DOWNLOAD = "https://api.flaticon.com/v3/item/icon/download";

    // Shared instance
    public static FlatIcon SHARED = new FlatIcon();

    /**
     * Constructor.
     */
    private FlatIcon()
    {
    }

    /**
     * Returns the token.
     */
    public FlatIconItem[] getImageItemsForSearchString(String aSearchString)
    {
        // Get search string with escaped spaces
        String searchString = aSearchString.replace(" ", "%20");

        // Create http request
        HTTPRequest httpReq = new HTTPRequest(GET_ICONS + "?q=" + searchString);
        httpReq.addHeader("Accept", "application/json");
        httpReq.addHeader("Authorization", "Bearer " + getToken());

        // Get http response
        HTTPResponse httpResp;
        try { httpResp = httpReq.getResponse(); }
        catch (Exception e) { throw new RuntimeException(e); }
        if (httpResp == null || httpResp.getCode() != HTTPResponse.OK)
            return null;

        // Get JSON response
        JSObject json = (JSObject) httpResp.getJSON();
        if (json == null)
            return null;

        // Get metadata, count, total
        JSObject metaNode = (JSObject) json.getValue("metadata");
        int count = Convert.intValue(metaNode.getNativeValue("count"));
        int total = Convert.intValue(metaNode.getNativeValue("total"));
        System.out.println("Found " + count + " of " + total);

        // Get data
        JSArray dataArrayJS = (JSArray) json.getValue("data");

        List<FlatIconItem> imageItems = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            JSObject imageNode = (JSObject) dataArrayJS.getValue(i);
            FlatIconItem imgItem = new FlatIconItem(imageNode);
            imageItems.add(imgItem);
        }

        return imageItems.toArray(new FlatIconItem[0]);
    }

    /**
     * Returns the image for given size.
     */
    public Image getImageForIdAndSize(int anId, int aSize)
    {
        // Create http request
        HTTPRequest httpReq = new HTTPRequest(DOWNLOAD + "/" + anId + "?size=" + aSize);
        httpReq.addHeader("Accept", "application/json");
        httpReq.addHeader("Authorization", "Bearer " + getToken());

        // Get http response
        HTTPResponse httpResp;
        try { httpResp = httpReq.getResponse(); }
        catch (Exception e) { throw new RuntimeException(e); }
        if (httpResp == null || httpResp.getCode() != HTTPResponse.OK) {
            System.out.println("Get image failed: " + (httpResp != null ? httpResp.getMessage() : "null"));
            return null;
        }

        byte[] bytes = httpResp.getBytes();
        Image img = Image.getImageForSource(bytes);
        return img;
    }

    /**
     * Returns the token.
     */
    private static String getToken()
    {
        // If already set, just return
        if (_token != null) return _token;

        // Create http request
        HTTPRequest httpReq = new HTTPRequest(AUTHENTICATION);
        httpReq.addHeader("Content-Type", "application/json");
        httpReq.addHeader("Accept", "application/json");
        addParamsToRequestAsJSON(httpReq, "apikey", FlatIconHelp.flaticon);

        // Get http response
        HTTPResponse httpResp;
        try { httpResp = httpReq.getResponse(); }
        catch (Exception e) { throw new RuntimeException(e); }
        if (httpResp == null || httpResp.getCode() != HTTPResponse.OK)
            return null;

        // Get JSON response
        JSObject json = (JSObject) httpResp.getJSON();
        if (json == null)
            return null;

        // Get data
        JSObject dataNode = (JSObject) json.getValue("data");
        String token = dataNode.getStringValue("token");
        //String expires = dataNode.getStringValue("expires");

        return _token = token;
    }

    /**
     * Adds a JSON Header to given HTTP Request.
     */
    private static void addParamsToRequestAsJSON(HTTPRequest httpReq, String... thePairs)
    {
        // Create JSON Request and add pairs
        JSObject jsonReq = new JSObject();
        for (int i = 0; i < thePairs.length; i += 2)
            jsonReq.setNativeValue(thePairs[i], thePairs[i + 1]);

        // Add as send-bytes
        String jsonReqStr = jsonReq.toString();
        httpReq.setBytes(jsonReqStr.getBytes());
    }
}
