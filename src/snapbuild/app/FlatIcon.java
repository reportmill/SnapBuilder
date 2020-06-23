package snapbuild.app;

import snap.geom.Pos;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.gfx.Image;
import snap.util.JSONNode;
import snap.util.SnapUtils;
import snap.view.TextArea;
import snap.view.ViewUtils;
import snap.viewx.FilePanel;
import snap.web.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A Class to work with FlatIcon.
 */
public class FlatIcon {

    // The Email for this DropBox
    private String  _token;

    // Constants for DropBox endpoints
    private static final String AUTHENTICATION = "https://api.flaticon.com/v2/app/authentication";
    private static final String GET_ICONS = "https://api.flaticon.com/v2/search/icons";
    private static final String DOWNLOAD = "https://api.flaticon.com/v2/item/icon/download";

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
        if (_token!=null) return _token;

        // Create Request
        HTTPRequest req = new HTTPRequest(AUTHENTICATION);
        req.addHeader("Content-Type", "application/json");
        req.addHeader("Accept", "application/json");

        // Add apikey to request
        addParamsToRequestAsJSON(req, false, "apikey", DropBoxHelp.flaticon);

        // Get HTTP Response
        HTTPResponse resp;
        try { resp = req.getResponse(); } //getResponseHTTP(req, aResp);
        catch(Exception e) { throw new RuntimeException(e); }
        if (resp==null || resp.getCode()!=HTTPResponse.OK)
            return null;

        // Get JSON response
        JSONNode json = resp.getJSON();
        if (json==null)
            return null;

        // Get data
        JSONNode dataNode = json.getNode("data");
        String token = dataNode.getNodeString("token");
        String expires = dataNode.getNodeString("expires");

        System.out.println("Found token: " + token);

        return _token = token;
    }

    /**
     * Returns the token.
     */
    public ImageItem[] getImageItemsForSearchString(String aSearchString)
    {
        //
        String searchString = aSearchString.replace(" ", "%20");

        // Create Request
        HTTPRequest req = new HTTPRequest(GET_ICONS + "?q=" + searchString);
        req.addHeader("Accept", "application/json");
        req.addHeader("Authorization", "Bearer " + getToken());

        // Get HTTP Response
        HTTPResponse resp;
        try { resp = req.getResponse(); } //getResponseHTTP(req, aResp);
        catch(Exception e) { throw new RuntimeException(e); }
        if (resp==null || resp.getCode()!=HTTPResponse.OK)
            return null;

        // Get JSON response
        JSONNode json = resp.getJSON();
        if (json==null)
            return null;

        //
        JSONNode metaNode = json.getNode("metadata");
        int count = SnapUtils.intValue(metaNode.getNodeValue("count"));
        int total = SnapUtils.intValue(metaNode.getNodeValue("total"));
        System.out.println("Found " + count + " of " + total);

        JSONNode dataNode = json.getNode("data");

        List <ImageItem> items = new ArrayList<>();
        for (int i=0; i<count; i++) {
            JSONNode imageNode = dataNode.getNode(i);
            ImageItem imgItem = new ImageItem(imageNode);
            items.add(imgItem);

        }

        return items.toArray(new ImageItem[0]);
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
        try { resp = req.getResponse(); } //getResponseHTTP(req, aResp);
        catch(Exception e) { throw new RuntimeException(e); }
        if (resp==null || resp.getCode()!=HTTPResponse.OK) {
            System.out.println("Get image failed: " + resp.getMessage());
            return null;
        }

        byte bytes[] = resp.getBytes();
        Image img = Image.get(bytes);

        // Why would this happen?
        if (img.getWidth()!=aSize) {
            img = img.cloneForSizeAndScale(aSize, aSize, 1);
        }

        return img;
    }

    /**
     * Adds a JSON Header to given HTTP Request.
     */
    private static void addParamsToRequestAsJSON(HTTPRequest aReq, boolean asHeader, String ... thePairs)
    {
        // Create JSON Request and add pairs
        JSONNode jsonReq = new JSONNode();
        for (int i=0; i<thePairs.length; i+=2)
            jsonReq.addKeyValue(thePairs[i], thePairs[i+1]);

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
    private static <T,R> List<R> getMappedList(Collection<T> aList, Function<? super T, ? extends R> mapper)
    {
        return aList.stream().map(mapper).collect(Collectors.toList());
    }

    /**
     * A class to hold an image item.
     */
    public static class ImageItem {

        // The image item node
        private JSONNode  _itemNode;

        // The Id
        private int  _id;

        // The description
        private String  _desc;

        // The sample image
        private Image _sample;

        /**
         * Constructor.
         */
        public ImageItem(JSONNode aNode)
        {
            _itemNode = aNode;

            String idStr = _itemNode.getNodeString("id");
            _id = SnapUtils.intValue(idStr);

            _desc = _itemNode.getNodeString("description");

//            JSONNode imagesNode = _itemNode.getNode("images");
//            JSONNode pngNode = imagesNode.getNode("png");
//            int pngCount = pngNode.getNodeCount();
//
//            for (int j=0; j<pngCount; j++) {
//                JSONNode sizeNode = pngNode.getNode(j);
//                int size = SnapUtils.intValue(sizeNode.getKey());
//                if (size > 128 || j+1==pngCount) {
//                    String urls = sizeNode.getString();
//                    continue;
//                }
//            }
        }

        /**
         * Returns the description.
         */
        public String getDescription()  { return _desc; }

        /**
         * Returns the image for given size.
         */
        public Image getSample()
        {
            if (_sample!=null) return _sample;

            Image img = FlatIcon.SHARED.getImageForIdAndSize(_id, 32);

            if (img==null) {
                TextArea text = new TextArea();
                text.setBorder(Color.BLACK, 1);
                text.setPadding(10,10,10,10);
                text.setWrapLines(true);
                text.setAlign(Pos.CENTER);
                text.setText("Image not found: " + _desc);
                text.setSize(128, 128);
                text.setFont(Font.Arial10);
                img = ViewUtils.getImageForScale(text, 1);
            }
            return _sample = img;
        }

    }
}
