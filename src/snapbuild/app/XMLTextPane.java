package snapbuild.app;
import snap.gfx.*;
import snap.parse.*;
import snap.text.TextBlock;
import snap.text.TextStyle;
import snap.view.*;

/**
 * This class displays the editor content as XML and allows for editing.
 */
public class XMLTextPane extends ViewOwner {

    // The EditorPane
    private EditorPane _editorPane;

    // The ViewXML
    private ViewXML _viewXML;

    // The TextView
    private static TextView _textView;

    // Indicates TextView selection changed in this pane (as opposed to externally)
    private boolean _textViewSelChanging;

    // Colors
    private static Color NAME_COLOR = new Color("#7D1F7C"); //336633
    private static Color KEY_COLOR = new Color("#8F4A19");
    private static Color VALUE_COLOR = new Color("#5E1B9F"); // CC0000

    /**
     * Constructor.
     */
    public XMLTextPane(EditorPane editorPane)
    {
        super();
        _editorPane = editorPane;
    }

    /**
     * Creates the UI.
     */
    protected View createUI()
    {
        // Create TextView for xml
        _textView = new TextView(true);

        // Create/set RichText
        TextBlock textBlock = _textView.getTextBlock();
        textBlock.setTextStyleValue(TextStyle.Font_Prop, Font.Arial14.copyForSize(15), 0, 0);

        // Get/config TextArea
        _textView.addPropChangeListener(pc -> handleTextViewSelChange(), TextArea.Selection_Prop);

        // Return
        return _textView;
    }

    /**
     * Updates the XML TextView.
     */
    protected void updateXMLText()
    {
        // If not showing, just return
        if (_textView == null || !_textView.isShowing())
            return;

        // Get ViewXML
        View rootView = _editorPane.getContent();
        _viewXML = new ViewXML(rootView);

        // Reset TextView text
        String text = _viewXML.getXmlString();
        _textView.setText(text);

        // Reset colors
        new XMLParser().parse(text);

        // Reset textview selection
        runDelayed(() -> handleEditorSelViewChange(), 200);
    }

    /**
     * Called when Editor.SelView property changes.
     */
    protected void handleEditorSelViewChange()
    {
        // If not showing, just return
        if (_textView == null || !_textView.isShowing() || _textViewSelChanging)
            return;

        // Get View
        Editor editor = _editorPane.getEditor();
        View sview = editor.getSelView();
        int start = _viewXML.getCharStart(sview);
        int end = _viewXML.getCharEnd(sview);
        _textView.setSel(start, end);
    }

    /**
     * Called when TextView.Selection property changes to update EditorPane SelView.
     */
    protected void handleTextViewSelChange()
    {
        // Get View that fully contains selection (just return if none)
        int start = _textView.getSelStart();
        int end = _textView.getSelEnd();
        View view = _viewXML.getViewInCharRange(start, end);
        if (view == null) return;

        // Set SelView to view with suppression so that we don't update text selection
        _textViewSelChanging = true;
        _editorPane.setSelViewKeepPath(view);
        _textViewSelChanging = false;
    }

    /**
     * Sets the text color for a range.
     */
    static void setColor(Color aColor, int aStart, int aEnd)
    {
        TextBlock textBlock = _textView.getTextBlock();
        textBlock.setTextStyleValue(TextStyle.COLOR_KEY, aColor, aStart, aEnd);
    }

    /**
     * A class to parse XML.
     */
    private static class XMLParser extends Parser {

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
            switch (anId) {

                // Handle Name
                case "Name": setColor(NAME_COLOR, aNode.getStart(), aNode.getEnd()); break;

                // Handle close: On first close, check for content
                case "<": case ">": case "</": case "/>":
                    setColor(NAME_COLOR, aNode.getStart(), aNode.getEnd());
                    break;
            }
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
            switch (anId) {

                // Handle Name
                case "Name": setColor(KEY_COLOR, aNode.getStart(), aNode.getEnd()); break;

                // Handle String
                case "String": setColor(VALUE_COLOR, aNode.getStart(), aNode.getEnd()); break;

                // Handle "="
                case "=": setColor(NAME_COLOR, aNode.getStart(), aNode.getEnd()); break;
            }
        }
    }
}