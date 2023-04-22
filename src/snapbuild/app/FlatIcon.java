package snapbuild.app;
import snap.gfx.Image;
import snap.util.Convert;
import snap.util.JSArray;
import snap.util.JSObject;
import snap.web.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A Class to work with FlatIcon.
 */
public class FlatIcon {

    // The access token for FlatIcon api: https://api.flaticon.com
    private String _token;

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
    public String getToken()
    {
        // If already set, just return
        if (_token != null) return _token;

        // Create Request
        HTTPRequest req = new HTTPRequest(AUTHENTICATION);
        req.addHeader("Content-Type", "application/json");
        req.addHeader("Accept", "application/json");

        // Add apikey to request
        addParamsToRequestAsJSON(req, false, "apikey", DropBoxHelp.flaticon);

        // Get HTTP Response
        HTTPResponse resp;
        try {
            resp = req.getResponse();
        } //getResponseHTTP(req, aResp);
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (resp == null || resp.getCode() != HTTPResponse.OK)
            return null;

        // Get JSON response
        JSObject json = (JSObject) resp.getJSON();
        if (json == null)
            return null;

        // Get data
        JSObject dataNode = (JSObject) json.getValue("data");
        String token = dataNode.getStringValue("token");
        String expires = dataNode.getStringValue("expires");

        System.out.println("Authorize OK, expires: " + expires);

        return _token = token;
    }

    /**
     * Returns the token.
     */
    public FlatIconItem[] getImageItemsForSearchString(String aSearchString)
    {
        // Get search string with escaped spaces
        String searchString = aSearchString.replace(" ", "%20");

        // Create Request
        HTTPRequest req = new HTTPRequest(GET_ICONS + "?q=" + searchString);
        req.addHeader("Accept", "application/json");
        req.addHeader("Authorization", "Bearer " + getToken());

        // Get HTTP Response
        HTTPResponse resp;
        try {
            resp = req.getResponse();
        } //getResponseHTTP(req, aResp);
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (resp == null || resp.getCode() != HTTPResponse.OK)
            return null;

        // Get JSON response
        JSObject json = (JSObject) resp.getJSON();
        if (json == null)
            return null;

        // Get metadata, count, total
        JSObject metaNode = (JSObject) json.getValue("metadata");
        int count = Convert.intValue(metaNode.getNativeValue("count"));
        int total = Convert.intValue(metaNode.getNativeValue("total"));
        System.out.println("Found " + count + " of " + total);

        // Get data
        JSArray dataArrayJS = (JSArray) json.getValue("data");

        List<FlatIconItem> items = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            JSObject imageNode = (JSObject) dataArrayJS.getValue(i);
            FlatIconItem imgItem = new FlatIconItem(imageNode);
            items.add(imgItem);
        }

        return items.toArray(new FlatIconItem[0]);
    }

    /**
     * Returns the image for given size.
     */
    public Image getImageForIdAndSize(int anId, int aSize)
    {
        // Create Request
        HTTPRequest req = new HTTPRequest(DOWNLOAD + "/" + anId + "?size=" + aSize);
        req.addHeader("Accept", "application/json");
        req.addHeader("Authorization", "Bearer " + getToken());

        // Get HTTP Response
        HTTPResponse resp;
        try {
            resp = req.getResponse();
        } //getResponseHTTP(req, aResp);
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (resp == null || resp.getCode() != HTTPResponse.OK) {
            System.out.println("Get image failed: " + resp.getMessage());
            return null;
        }

        byte bytes[] = resp.getBytes();
        Image img = Image.get(bytes);
        return img;
    }

    /**
     * Adds a JSON Header to given HTTP Request.
     */
    private static void addParamsToRequestAsJSON(HTTPRequest aReq, boolean asHeader, String... thePairs)
    {
        // Create JSON Request and add pairs
        JSObject jsonReq = new JSObject();
        for (int i = 0; i < thePairs.length; i += 2)
            jsonReq.setNativeValue(thePairs[i], thePairs[i + 1]);

        // Add as header
        if (asHeader) {
            String jsonReqStr = jsonReq.toStringCompacted();
            jsonReqStr = jsonReqStr.replace("\"", "\\\"");
            jsonReqStr = jsonReqStr.replace("\\", "");
            aReq.addHeader("Dropbox-API-Arg", jsonReqStr);
        }

        // Add as send-bytes
        else {
            String jsonReqStr = jsonReq.toString();
            aReq.setBytes(jsonReqStr.getBytes());
        }
    }

    /**
     * Returns a list of derived items for given collection of original items.
     */
    private static <T, R> List<R> getMappedList(Collection<T> aList, Function<? super T, ? extends R> mapper)
    {
        return aList.stream().map(mapper).collect(Collectors.toList());
    }

}
