package snapbuild.app;
import snap.gfx.*;
import snap.parse.*;
import snap.text.TextBlock;
import snap.text.TextStyle;
import snap.util.Convert;
import snap.view.*;

/**
 * A class to manage XMLText view.
 */
public class XMLText extends ViewOwner {

    // The EditorPane
    private EditorPane _epane;

    // The View
    private View _content;

    // The XML string
    private String _xmlStr;

    // The TextView
    private static TextView _xmlText;

    // Indicates that XMLText changed selection (so we know resulting editor selection isn't externa)
    private boolean _xmlTextSelChanging;

    // Colors
    private static Color NAME_COLOR = new Color("#7D1F7C"); //336633
    private static Color KEY_COLOR = new Color("#8F4A19");
    private static Color VALUE_COLOR = new Color("#5E1B9F"); // CC0000

    /**
     * Creates new XMLText for EditorPane.
     */
    public XMLText(EditorPane anEP)
    {
        _epane = anEP;
    }

    /**
     * Returns the XML string.
     */
    public String getXMLString()
    {
        return getXMLString(true);
    }

    /**
     * Returns the XML string.
     */
    public String getXMLString(boolean useCache)
    {
        // If already set, just return
        if (_xmlStr != null && useCache) return _xmlStr;

        // Get text for content
        _content = _epane.getContent();
        _xmlStr = new ViewArchiver().toXML(_content).getString();

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
        return getViewInCharRange(aStart, aEnd, _content);
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

    /**
     * Creates the UI.
     */
    protected View createUI()
    {
        // Create XMLText TextView
        _xmlText = new TextView(true);

        // Create/set RichText
        TextBlock richText = _xmlText.getTextBlock();
        richText.setTextStyleValue(TextStyle.Font_Prop, Font.Arial14.copyForSize(15), 0, 0);

        // Get/config TextArea
        _xmlText.addPropChangeListener(pc -> xmlTextSelDidChange(), TextArea.Selection_Prop);

        // Return
        return _xmlText;
    }

    /**
     * Updates the XMLText TextView.
     */
    protected void updateXMLText()
    {
        // If not showing, just return
        if (_xmlText == null || !_xmlText.isShowing()) return;

        // Get View
        String text = getXMLString(false);
        _xmlText.setText(text);
        new XMLParser().parse(text);
        runDelayed(() -> updateXMLTextSel(), 200);
    }

    /**
     * Updates the XMLText TextView.
     */
    protected void updateXMLTextSel()
    {
        // If not showing, just return
        if (_xmlText == null || !_xmlText.isShowing() || _xmlTextSelChanging) return;

        // Get View
        Editor editor = _epane.getEditor();
        View sview = editor.getSelView();
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
        int start = _xmlText.getSelStart();
        int end = _xmlText.getSelEnd();
        View view = getViewInCharRange(start, end);
        if (view == null) return;

        // Set SelView to view with suppression so that we don't update text selection
        _xmlTextSelChanging = true;
        _epane.setSelViewKeepPath(view);
        _xmlTextSelChanging = false;
    }

    /**
     * Sets the text color for a range.
     */
    static void setColor(Color aColor, int aStart, int aEnd)
    {
        TextBlock textBlock = _xmlText.getTextBlock();
        textBlock.setTextStyleValue(TextStyle.COLOR_KEY, aColor, aStart, aEnd);
    }

    /**
     * A class to parse XML.
     */
    public class XMLParser extends Parser {

        /**
         * Override to set simple rule handlers.
         */
        public XMLParser()
        {
            getRuleForName("Element").setHandler(new ElementHandler());
            getRuleForName("Attribute").setHandler(new AttributeHandler());
        }

        /**
         * Override to load rules from /snap/util/XMLParser.txt.
         */
        @Override
        protected Grammar createGrammar()
        {
            return Grammar.createGrammarForParserClass(snap.util.XMLParser.class);
        }

        /**
         * Override to return XMLTokenizer.
         */
        protected Tokenizer createTokenizer()
        {
            return new snap.util.XMLParser.XMLTokenizer();
        }
    }

    /**
     * Element Handler: Element { "<" Name Attribute* ("/>" | (">" Content "</" Name ">")) }
     */
    public static class ElementHandler extends ParseHandler {

        /**
         * ParseHandler method.
         */
        public void parsedOne(ParseNode aNode, String anId)
        {
            // Handle Name
            if (anId == "Name")
                setColor(NAME_COLOR, aNode.getStart(), aNode.getEnd());

                // Handle close: On first close, check for content
            else if (anId == "<" || anId == ">" || anId == "</" || anId == "/>")
                setColor(NAME_COLOR, aNode.getStart(), aNode.getEnd());
        }
    }

    /**
     * Attribute Handler: Attribute { Name "=" String }
     */
    public static class AttributeHandler extends ParseHandler {

        /**
         * ParseHandler method.
         */
        public void parsedOne(ParseNode aNode, String anId)
        {
            // Handle Name
            if (anId == "Name")
                setColor(KEY_COLOR, aNode.getStart(), aNode.getEnd());

                // Handle String
            else if (anId == "String")
                setColor(VALUE_COLOR, aNode.getStart(), aNode.getEnd());

                // Handle "="
            else if (anId == "=")
                setColor(NAME_COLOR, aNode.getStart(), aNode.getEnd());
        }
    }
}