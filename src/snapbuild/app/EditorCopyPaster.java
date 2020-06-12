package snapbuild.app;
import snap.gfx.Image;
import snap.util.XMLElement;
import snap.view.*;
import snap.web.MIMEType;

/**
 * A CopyPaster implementation for Editor.
 */
public class EditorCopyPaster {

    // The Editor
    private Editor _editor;

    // The MIME type for archival format
    public static final String    SNAP_XML_TYPE = "snap-studio/xml";

    /**
     * Creates EditorCopyPaster for given editor.
     */
    public EditorCopyPaster(Editor anEditor)
    {
        _editor = anEditor;
    }

    /**
     * Returns the editor.
     */
    public Editor getEditor()  { return _editor; }

    /**
     * Handles editor cut operation.
     */
    public void cut()
    {
        _editor.copy();
        _editor.delete();
    }

    /**
     * Handles editor copy operation.
     */
    public void copy()
    {
        // If SelView is Content, just return
        View view = _editor.getSelView();
        if (view==_editor.getContent()) { ViewUtils.beep(); return; }

        // Get clipboard
        Clipboard cb = Clipboard.get();

        // Get image and add to clipbard
        int scale = ViewUtils.isAltDown() ? 1 : 0;
        Image image = ViewUtils.getImageForScale(view, scale);
        cb.addData(image);

        // Get xml string for selected shapes and add to clipboard as SNAP_XML
        XMLElement xml = new ViewArchiver().writeToXML(view);
        String xmlStr = xml.getString();
        cb.addData(SNAP_XML_TYPE, xmlStr);

        // Add xml as String (probably stupid)
        cb.addData(xmlStr);
    }

    /**
     * Handles editor paste operation.
     */
    public void paste()
    {
        // Get Clipboard
        Clipboard cb = Clipboard.get();

        // Handle SNAP_XML: Get bytes, unarchive view and add
        if (cb.hasData(SNAP_XML_TYPE)) {
            byte bytes[] = cb.getDataBytes(SNAP_XML_TYPE);
            View view = new ViewArchiver().getView(bytes);
            _editor.addView(view);
        }

        // Paste Image
        else if (cb.hasImage()) {
            ClipboardData idata = cb.getImageData();
            byte bytes[] = idata.getBytes();
            ImageView iview = new ImageView(bytes);
            _editor.addView(iview);
        }
    }

    /**
     * Deletes all the currently selected shapes.
     */
    public void delete()
    {
        // Get selected view and parent
        View sview = _editor.getSelView();
        ParentView par = sview.getParent();

        // Get par as host (just return if not host) and remove guest
        ViewHost host = sview.getHost(); if (host==null) { ViewUtils.beep(); return; }
        int ind = sview.indexInHost();
        host.removeGuest(sview);

        // Set new selected view
        if (host.getGuestCount()>0)
            _editor.setSelView(host.getGuest(ind<host.getGuestCount()? ind : ind -1));
        else _editor.setSelView(par);

        /*ParentView par = sview.getParent();
        int ind = sview.indexInParent();
        if(par instanceof ChildView) { ChildView cview = (ChildView)par;
            cview.removeChild(sview);
            if(cview.getChildCount()>0)
                setSelView(ind<cview.getChildCount()? cview.getChild(ind) : cview.getChild(ind-1));
            else setSelView(cview);
        }*/
    }

    /**
     * Causes all the children of the current super selected shape to become selected.
     */
    public void selectAll()
    {
        ViewUtils.beep();
    }
}
