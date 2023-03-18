package snapbuild.app;
import snap.gfx.Image;
import snap.util.SnapUtils;
import snap.util.XMLElement;
import snap.view.*;

/**
 * A CopyPaster implementation for Editor.
 */
public class EditorCopyPaster {

    // The Editor
    private Editor _editor;

    // The MIME type for archival format
    public static final String SNAP_XML_TYPE = "snap-studio/xml";

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
    public Editor getEditor()
    {
        return _editor;
    }

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
        // Get SelView
        View selView = _editor.getSelView();

        // Get clipboard
        Clipboard cb = Clipboard.getCleared();

        // If browser, just copy XML as text
        if (SnapUtils.isTeaVM) {
            XMLElement xml = new ViewArchiver().writeToXML(selView);
            String xmlStr = xml.getString();
            cb.addData(xmlStr);
            return;
        }

        // Get image and add to clipbard
        int scale = ViewUtils.isAltDown() ? 1 : 0;
        Image image = ViewUtils.getImageForScale(selView, scale);
        cb.addData(image);

        // Get xml string for selected shapes and add to clipboard as SNAP_XML
        XMLElement xml = new ViewArchiver().writeToXML(selView);
        String xmlStr = xml.getString();
        cb.addData(SNAP_XML_TYPE, xmlStr);

        // Add xml as String
        cb.addData(xmlStr);
    }

    /**
     * Handles editor paste operation.
     */
    public void paste()
    {
        // Get clipboard - if not loaded come back when it is
        Clipboard cb = Clipboard.get();
        if (!cb.isLoaded()) {
            cb.addLoadListener(() -> paste());
            return;
        }

        // If browser, just try for XML string
        if (SnapUtils.isTeaVM) {
            if (cb.hasString()) {
                String str = cb.getString();
                byte[] bytes = str.getBytes();
                View view = new ViewArchiver().getViewForBytes(bytes);
                _editor.addView(view);
            }
            return;
        }

        // Handle SNAP_XML: Get bytes, unarchive view and add
        if (cb.hasData(SNAP_XML_TYPE)) {
            byte[] bytes = cb.getDataBytes(SNAP_XML_TYPE);
            View view = new ViewArchiver().getViewForBytes(bytes);
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
        ViewHost host = sview.getHost();
        if (host == null) {
            ViewUtils.beep();
            return;
        }
        int ind = sview.indexInHost();
        host.removeGuest(sview);

        // Set new selected view
        if (host.getGuestCount() > 0)
            _editor.setSelView(host.getGuest(ind < host.getGuestCount() ? ind : ind - 1));
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
        _editor.setSelView(_editor.getContent());
        ViewUtils.beep();
    }
}
