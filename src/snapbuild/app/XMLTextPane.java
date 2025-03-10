package snapbuild.app;
import snap.view.*;

/**
 * This class displays the editor content as XML and allows for editing.
 */
public class XMLTextPane extends ViewOwner {

    // The EditorPane
    private EditorPane _editorPane;

    // The ViewXML that holds content view + xml
    private ViewXML _viewXML;

    // The TextView
    private static TextView _textView;

    // Indicates TextView selection changed in this pane (as opposed to externally)
    private boolean _textViewSelChanging;

    /**
     * Constructor.
     */
    public XMLTextPane(EditorPane editorPane)
    {
        super();
        _editorPane = editorPane;
    }

    /**
     * Resets the XML TextView.
     */
    protected void resetXML()
    {
        // If not showing, just return
        if (_textView == null || !_textView.isShowing())
            return;

        // Get ViewXML
        View rootView = _editorPane.getContent();
        _viewXML = new ViewXML(rootView);

        // Reset TextView text
        String xmlString = _viewXML.getXmlString();
        _textView.setText(xmlString);

        // Reset colors
        XMLTextColorizer.syntaxColorTextView(_textView, xmlString);

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
        View selView = editor.getSelView();
        int startCharIndex = _viewXML.getStartCharIndexForView(selView);
        int endCharIndex = _viewXML.getEndCharIndexForView(selView);
        _textView.setSel(startCharIndex, endCharIndex);
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
     * Creates the UI.
     */
    protected View createUI()
    {
        // Create TextView for xml
        _textView = new TextView(true);
        _textView.setDefaultTextStyleString("Font:Arial 15");

        // Get/config TextArea
        _textView.addPropChangeListener(pc -> handleTextViewSelChange(), TextArea.Selection_Prop);

        // Return
        return _textView;
    }
}