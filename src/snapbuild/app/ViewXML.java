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
    private int setCharIndexes(View aView, String aStr, int aStart)
    {
        // Get tag name for view and find/set element open char index
        String tagName = aView.getClass().getSimpleName();
        int startCharIndex = aStr.indexOf('<' + tagName, aStart);
        setStartCharIndexForView(aView, startCharIndex);
        startCharIndex += tagName.length() + 1;

        // If view is ViewHost, recurse
        if (aView instanceof ViewHost) {
            ViewHost host = (ViewHost) aView;
            for (View child : host.getGuests())
                startCharIndex = setCharIndexes(child, aStr, startCharIndex);
        }

        // Find/set element close char index and return
        int endCharIndex = aStr.indexOf('>', startCharIndex);
        endCharIndex++;
        setEndCharIndexForView(aView, endCharIndex);
        return endCharIndex;
    }

    /**
     * Returns the Char start index for a view.
     */
    public int getStartCharIndexForView(View aView)
    {
        return Convert.intValue(aView.getProp("CharStart"));
    }

    /**
     * Sets the Char start index for a view.
     */
    private void setStartCharIndexForView(View aView, int aStart)  { aView.setProp("CharStart", aStart); }

    /**
     * Returns the Char end index for a view.
     */
    public int getEndCharIndexForView(View aView)
    {
        return Convert.intValue(aView.getProp("CharEnd"));
    }

    /**
     * Sets the Char end index for a view.
     */
    private void setEndCharIndexForView(View aView, int aStart)  { aView.setProp("CharEnd", aStart); }

    /**
     * Returns the View in given char range.
     */
    public View getViewInCharRange(int aStart, int aEnd)
    {
        return getViewInCharRangeImpl(aStart, aEnd, _rootView);
    }

    /**
     * Returns the View in given char range.
     */
    private View getViewInCharRangeImpl(int aStart, int aEnd, View aView)
    {
        // If view is host view, recurse to see if any Guests contain range (if so, return them)
        if (aView instanceof ViewHost) {
            ViewHost host = (ViewHost) aView;
            for (View child : host.getGuests()) {
                View childInRange = getViewInCharRangeImpl(aStart, aEnd, child);
                if (childInRange != null)
                    return childInRange;
            }
        }

        // If View contains range, return it
        int startCharIndex = getStartCharIndexForView(aView);
        int endCharIndex = getEndCharIndexForView(aView);
        if (startCharIndex <= aStart && aEnd <= endCharIndex)
            return aView;

        // Return null
        return null;
    }
}
