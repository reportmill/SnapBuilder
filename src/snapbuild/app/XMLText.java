package snapbuild.app;
import snap.gfx.Font;
import snap.util.SnapUtils;
import snap.view.*;

/**
 * A class to manage XMLText view.
 */
public class XMLText extends ViewOwner {
    
    // The EditorPane
    EditorPane      _epane;
    
    // The View
    View            _content;

    // The XML string
    String          _xmlStr;
    
    // The TextView
    TextView        _xmlText;

    // 
    boolean         _xmlTextSelChanging;

/**
 * Creates new XMLText for EditorPane.
 */
public XMLText(EditorPane anEP)  { _epane = anEP; }

/**
 * Returns the XML string.
 */
public String getXMLString()  { return getXMLString(true); }

/**
 * Returns the XML string.
 */
public String getXMLString(boolean useCache)
{
    // If already set, just return
    if(_xmlStr!=null && useCache) return _xmlStr;
    
    // Get text for content
    _content = _epane.getContent();
    _xmlStr = SnapUtils.getText(new ViewArchiver().toXML(_content).getBytes());
    
    // Set char indexes for all views and return string
    setCharIndexes(_content, _xmlStr, 0);
    return _xmlStr;
}

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
    
    // If view is HostView, recurse
    if(aView instanceof HostView) { HostView host = (HostView)aView;
        for(View child : host.getGuests())
            start2 = setCharIndexes(child, aStr, start2); }
    
    // Find/set element close char index and return
    start2 = aStr.indexOf('>', start2); start2++;
    setCharEnd(aView, start2);
    return start2;
}

/**
 * Returns the Char start index for a view.
 */
public int getCharStart(View aView)  { return SnapUtils.intValue(aView.getProp("CharStart")); }

/**
 * Sets the Char start index for a view.
 */
public void setCharStart(View aView, int aStart)  { aView.setProp("CharStart", aStart); }

/**
 * Returns the Char end index for a view.
 */
public int getCharEnd(View aView)  { return SnapUtils.intValue(aView.getProp("CharEnd")); }

/**
 * Sets the Char end index for a view.
 */
public void setCharEnd(View aView, int aStart)  { aView.setProp("CharEnd", aStart); }

/**
 * Returns the View in given char range.
 */
public View getViewInCharRange(int aStart, int aEnd)  { return getViewInCharRange(aStart, aEnd, _content); }

/**
 * Returns the View in given char range.
 */
View getViewInCharRange(int aStart, int aEnd, View aView)
{
    // If view is host view, recurse to see if any Guests contain range (if so, return them)
    if(aView instanceof HostView) { HostView host = (HostView)aView;
        for(View child : host.getGuests()) {
            View c2 = getViewInCharRange(aStart, aEnd, child);
            if(c2!=null)
                return c2;
        }
    }
    
    // If View contains range, return it
    int cstart = getCharStart(aView), cend = getCharEnd(aView);
    if(cstart<=aStart && aEnd<=cend)
        return aView;
        
    // Return null
    return null;
}

/**
 * Creates the UI.
 */
protected View createUI()
{
    // Create XMLText TextView
    _xmlText = new TextView(); _xmlText.setFont(Font.Arial14);
    _xmlText.getTextArea().addPropChangeListener(pc -> xmlTextSelDidChange(), TextView.Selection_Prop);
    return _xmlText;
}

/**
 * Updates the XMLText TextView.
 */
protected void updateXMLText()
{
    // If not showing, just return
    if(_xmlText==null || !_xmlText.isShowing()) return;
    
    // Get View
    String text = getXMLString(false);
    _xmlText.setText(text);
    runLaterDelayed(200, () -> updateXMLTextSel());
}

/**
 * Updates the XMLText TextView.
 */
protected void updateXMLTextSel()
{
    // If not showing, just return
    if(_xmlText==null || !_xmlText.isShowing() || _xmlTextSelChanging) return;
    
    // Get View
    View sview = _epane.getSelView();
    int start = getCharStart(sview);
    int end = getCharEnd(sview);
    _xmlText.setSel(start, end);
}

/**
 * Updates the XMLText TextView.
 */
protected void xmlTextSelDidChange()
{
    // Get View that fully contains selection (just return if none)
    int start = _xmlText.getSelStart(), end = _xmlText.getSelEnd();
    View view = getViewInCharRange(start, end); if(view==null) return;
    
    // Set SelView to view with suppression so that we don't update text selection
    _xmlTextSelChanging = true;
    _epane.setSelViewKeepPath(view);
    _xmlTextSelChanging = false;
}

}