package snapbuild.app;

import snap.util.JSONNode;
import snap.util.JSONParser;
import snap.viewx.FilePanel;
import snap.web.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A Class to work with DropBox.
 */
public class DropBox extends WebSite {

    // The Email for this DropBox
    private String  _email;

    // The Site path
    private String _sitePath;

    // Constants for DropBox endpoints
    private static final String GET_METADATA = "/files/get_metadata";
    private static final String GET_CONTENT = "/files/download";
    private static final String LIST_FOLDER = "/files/list_folder";

    // The root for DropBox HTTP API calls
    private static String DROPBOX_ROOT = "dbox://dbox.com";
    private static String DROPBOX_API_ROOT = "https://api.dropboxapi.com/2";
    private static String DROPBOX_CONTENT_API_ROOT = "https://content.dropboxapi.com/2";

    // Token
    private static String _atok = "7bETIxvsar8AAAAAAAAAIfhjz12dECbKOYXhq8IWCzlNEUVKsPgp-gDuFjNt7zrI";

    // Date format
    private static DateFormat _fmt = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");

    // Shared instance
    private static DropBox  _shared;

    /**
     * Constructor.
     */
    private DropBox(String anEmail)
    {
        _email = anEmail;
        _sitePath = getPathForEmail(anEmail);
        String urls = DROPBOX_ROOT + _sitePath;
        WebURL url = WebURL.getURL(urls);
        setURL(url);
    }

    /**
     * Handles getting file info, contents or directory files.
     */
    @Override
    protected void doGetOrHead(WebRequest aReq, WebResponse aResp, boolean isHead)
    {
        // Always do Head
        doHead(aReq, aResp);
        if (isHead)
            return;

        // If error, just return
        if (aResp.getCode()!=WebResponse.OK)
            return;

        // If directory, get files
        FileHeader fhdr = aResp.getFileHeader();
        if (fhdr.isDir())
            doGetDir(aReq, aResp);

        // Otherwise, get contents
        else doGetFileContents(aReq, aResp);
    }

    /**
     * Get Head for request.
     */
    protected void doHead(WebRequest aReq, WebResponse aResp)
    {
        // Get URL and Path
        WebURL url = aReq.getURL();
        String path = url.getPath();
        String dboxPath = _sitePath + (path.length()>1 ? path : "");

        // Create Request
        HTTPRequest req = createRequestForEndpoint(GET_METADATA);

        // Create JSON Request
        JSONNode jsonReq = new JSONNode();
        jsonReq.addKeyValue("path", dboxPath);

        // Set Request bytes
        String jsonReqStr = jsonReq.toString();
        req.setBytes(jsonReqStr.getBytes());

        // Get JSON response
        JSONNode json = getResponseJSON(req, aResp);
        if (json==null)
            return;

        // Get JSON response
        FileHeader fhdr = createFileHeaderForJSON(json);
        fhdr.setPath(path);
        aResp.setFileHeader(fhdr);
    }

    /**
     * Get Directory listing for request.
     */
    protected void doGetDir(WebRequest aReq, WebResponse aResp)
    {
        // Get URL, Path and DropBoxPath
        WebURL url = aReq.getURL();
        String path = url.getPath();
        String dboxPath = _sitePath + (path.length()>1 ? path : "");

        // Create Request
        HTTPRequest req = createRequestForEndpoint(LIST_FOLDER);

        // Set Data
        JSONNode jsonReq = new JSONNode();
        jsonReq.addKeyValue("path", dboxPath);
        String jsonReqStr = jsonReq.toString();
        req.setBytes(jsonReqStr.getBytes());

        // Get JSON response
        JSONNode json = getResponseJSON(req, aResp);
        if (json==null)
            return;

        // Get json for entries
        JSONNode entriesNode = json.getNode("entries");
        List<JSONNode> entryNodes = entriesNode.getNodes();

        // Get FileHeader List for json entries
        List<FileHeader> fhdrs = getMappedList(entryNodes, e -> createFileHeaderForJSON(e));

        // Strip SitePath from FileHeaders
        for (FileHeader fhdr : fhdrs)
            fhdr.setPath(fhdr.getPath().substring(_sitePath.length()));

        // Set FileHeaders
        aResp.setFileHeaders(fhdrs);
    }

    /**
     * Get file request.
     */
    protected void doGetFileContents(WebRequest aReq, WebResponse aResp)
    {
        // Get URL, Path and DropBox path
        WebURL url = aReq.getURL();
        String path = url.getPath();
        String dboxPath = _sitePath + (path.length()>1 ? path : "");

        // Create Request
        String dboxUrl = DROPBOX_CONTENT_API_ROOT + GET_CONTENT;
        HTTPRequest req = new HTTPRequest(dboxUrl);
        req.addHeader("Authorization", "Bearer " + _atok);

        // Create JSON Request
        JSONNode jsonReq = new JSONNode();
        jsonReq.addKeyValue("path", dboxPath);

        // Set Request Header
        String jsonReqStr = jsonReq.toStringCompacted();
        jsonReqStr = jsonReqStr.replace("\"", "\\\"");
        jsonReqStr = jsonReqStr.replace("\\", "");
        req.addHeader("Dropbox-API-Arg", jsonReqStr);

        // Get response
        HTTPResponse resp;
        try { resp = req.getResponse(); }
        catch (Exception e) { throw new RuntimeException(e); }

        // Get response bytes
        int code = resp.getCode();
        if (code==HTTPResponse.OK) {
            byte bytes[] = resp.getBytes();
            aResp.setBytes(bytes);
        }
    }

    /**
     * Gets response for HTTPRequest, update WebResponse, and returns JSON, if available.
     */
    private static JSONNode getResponseJSON(HTTPRequest aReq, WebResponse aResp)
    {
        // Get HTTP Response
        HTTPResponse resp = getResponseHTTP(aReq, aResp);
        if (resp==null)
            return null;

        // If there was an error, just return
        int code = resp.getCode();
        if (code!=HTTPResponse.OK)
            return null;

        // Get JSON response
        String text = resp.getText();
        JSONNode json = new JSONParser().readString(text);
        return json;
    }

    /**
     * Sends the HTTP request and loads results into WebResponse.
     */
    private static HTTPResponse getResponseHTTP(HTTPRequest aReq, WebResponse aResp)
    {
        // Get response
        HTTPResponse resp;
        try { resp = aReq.getResponse(); }
        catch (Exception e)
        {
            aResp.setException(e);
            return null;
        }

        // Handle error
        int code = resp.getCode();
        aResp.setCode(code);
        return resp;
    }

    /**
     * Creates a request for given endpoint path.
     */
    private static HTTPRequest createRequestForEndpoint(String anEndpoint)
    {
        // Create Request
        String url = DROPBOX_API_ROOT + anEndpoint;
        HTTPRequest req = new HTTPRequest(url);

        // Add Headers
        req.addHeader("Authorization", "Bearer " + _atok);
        req.addHeader("Content-Type", "application/json");
        return req;
    }

    /**
     * Returns a FileHeader for DropBox File Entry JSONNode.
     */
    private static FileHeader createFileHeaderForJSON(JSONNode aFileEntryNode)
    {
        // Get attributes
        String path = aFileEntryNode.getNodeString("path_display");
        String tag = aFileEntryNode.getNodeString(".tag");
        boolean isFile = tag.equals("file");

        // Create FileHeader
        FileHeader fhdr = new FileHeader(path, !isFile);

        // Get additional file attributes
        if (isFile) {

            // Get/set size
            String sizeStr = aFileEntryNode.getNodeString("size");
            long size = Long.valueOf(sizeStr);
            fhdr.setSize(size);

            // Get/set ModTime
            String mod = aFileEntryNode.getNodeString("server_modified");
            if (mod.endsWith("Z")) {
                try {
                    Date date = _fmt.parse(mod);
                    fhdr.setModTime(date.getTime());
                }
                catch (Exception e) { System.err.println(e); }
            }
        }

        // Return FileHeader
        return fhdr;
    }

    /**
     * List root files.
     */
    public void listRootFiles()
    {
        WebFile roodDir = getRootDir();
        List<WebFile> files = roodDir.getFiles();
        for (WebFile file : files)
            System.out.println("File: " + file.getPath() + ", date: " + file.getModDate() + ", size: " + file.getSize());

        WebFile file0 = files.get(0);
        String text = file0.getText();
        System.out.println("File bytes for " + file0.getPath() + ":");
        System.out.println(text);
    }

    /**
     * Returns a path for email. E.G.: jack@abc.com = /com/abc/jack.
     * We're storing files at DropBox in this format.
     */
    private String getPathForEmail(String anEmail)
    {
        // Get email name
        int domInd = anEmail.indexOf('@'); if (domInd<0) return null;
        String name = anEmail.substring(0, domInd).replace('.', '_');

        // Get email domain parts
        String domain = anEmail.substring(domInd+1);
        String dirs[] = domain.split("\\.");

        // Add domain parts
        String path = "/";
        for (int i=dirs.length-1; i>=0; i--)
            path += dirs[i] + '/';

        // Add name and return
        path += name;
        return path;
    }

    /**
     * Returns a list of derived items for given collection of original items.
     */
    private static <T,R> List<R> getMappedList(Collection<T> aList, Function<? super T, ? extends R> mapper)
    {
        return aList.stream().map(mapper).collect(Collectors.toList());
    }

    /**
     * Returns shared instance.
     */
    public static DropBox getShared()
    {
        if (_shared!=null) return _shared;
        //System.setProperty("javax.net.debug","all");
        DropBox shared = new DropBox("jack@reportmill.com");
        FilePanel.setSiteDefault(shared);
        return _shared = shared;
    }
}
