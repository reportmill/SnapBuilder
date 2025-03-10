package snapbuild.app;
import snap.gfx.Color;
import snap.parse.*;
import snap.text.TextBlock;
import snap.text.TextStyle;
import snap.view.TextView;

/**
 * This class colors XML text.
 */
public class XMLTextColorizer {

    // The TextView
    private static TextView _textView;

    // Colors
    private static Color NAME_COLOR = new Color("#7D1F7C"); //336633
    private static Color KEY_COLOR = new Color("#8F4A19");
    private static Color VALUE_COLOR = new Color("#5E1B9F"); // CC0000

    /**
     * Performs syntax coloring on given TextView.
     */
    public static synchronized void syntaxColorTextView(TextView textView, String xmlString)
    {
        _textView = textView;

        // Reset colors
        new XMLParser().parse(xmlString);
    }

    /**
     * Sets the text color for a range.
     */
    private static void setColor(Color aColor, int aStart, int aEnd)
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
    public static class ElementHandler extends ParseHandler<Void> {

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
    public static class AttributeHandler extends ParseHandler<Void> {

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
