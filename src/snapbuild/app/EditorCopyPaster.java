package snapbuild.app;
import snap.util.XMLElement;
import snap.view.*;

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

        // Get xml for selected shapes, and get as string
        XMLElement xml = new ViewArchiver().writeToXML(view);
        String xmlStr = xml.toString();

        // Get clipboard and add data as XML string (RMData) and plain string
        Clipboard cb = Clipboard.get();
        cb.addData(SNAP_XML_TYPE, xmlStr);
        cb.addData(xmlStr);
    }

    /**
     * Handles editor paste operation.
     */
    public void paste()
    {
        // If Clipboard has View Data, paste it
        Clipboard cb = Clipboard.get();
        if (cb.hasData(SNAP_XML_TYPE)) {

            // Get bytes, unarchive view and add
            byte bytes[] = cb.getDataBytes(SNAP_XML_TYPE);
            View view = new ViewArchiver().getView(bytes);
            _editor.addView(view);
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
