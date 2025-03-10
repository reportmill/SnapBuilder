package snapbuild.app;
import snap.util.Convert;
import snap.view.View;
import snap.view.ViewArchiver;
import snap.view.ViewHost;

/**
 * This class represents View and XML data.
 */
public class ViewXML {

    // The root view
    private View _rootView;

    // The XML string
    private String _xmlStr;

    /**
     * Constructor.
     */
    public ViewXML(View aView)
    {
        _rootView = aView;

        _xmlStr = new ViewArchiver().toXML(aView).getString();

        // Set char indexes for all views and return string
        setCharIndexes(_rootView, _xmlStr, 0);
    }

    /**
     * Returns the XML string.
     */
    public String getXmlString()  { return _xmlStr; }

    /**
     * Sets the char indexes for HTML text.
     */
    int setCharIndexes(View aView, String aStr, int aStart)
    {
        // Get tag name for view and find/set element open char index
        String tag = aView.getClass().getSimpleName();
        int start2 = aStr.indexOf('<' + tag, aStart);
        setCharStart(aView, start2);
        start2 += tag.length() + 1;

        // If view is ViewHost, recurse
        if (aView instanceof ViewHost) {
            ViewHost host = (ViewHost) aView;
            for (View child : host.getGuests())
                start2 = setCharIndexes(child, aStr, start2);
        }

        // Find/set element close char index and return
        start2 = aStr.indexOf('>', start2);
        start2++;
        setCharEnd(aView, start2);
        return start2;
    }

    /**
     * Returns the Char start index for a view.
     */
    public int getCharStart(View aView)
    {
        return Convert.intValue(aView.getProp("CharStart"));
    }

    /**
     * Sets the Char start index for a view.
     */
    public void setCharStart(View aView, int aStart)
    {
        aView.setProp("CharStart", aStart);
    }

    /**
     * Returns the Char end index for a view.
     */
    public int getCharEnd(View aView)
    {
        return Convert.intValue(aView.getProp("CharEnd"));
    }

    /**
     * Sets the Char end index for a view.
     */
    public void setCharEnd(View aView, int aStart)
    {
        aView.setProp("CharEnd", aStart);
    }

    /**
     * Returns the View in given char range.
     */
    public View getViewInCharRange(int aStart, int aEnd)
    {
        return getViewInCharRange(aStart, aEnd, _rootView);
    }

    /**
     * Returns the View in given char range.
     */
    View getViewInCharRange(int aStart, int aEnd, View aView)
    {
        // If view is host view, recurse to see if any Guests contain range (if so, return them)
        if (aView instanceof ViewHost) {
            ViewHost host = (ViewHost) aView;
            for (View child : host.getGuests()) {
                View c2 = getViewInCharRange(aStart, aEnd, child);
                if (c2 != null)
                    return c2;
            }
        }

        // If View contains range, return it
        int cstart = getCharStart(aView), cend = getCharEnd(aView);
        if (cstart <= aStart && aEnd <= cend)
            return aView;

        // Return null
        return null;
    }
}
