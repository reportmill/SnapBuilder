package snapbuild.app;
import snap.view.*;
import snap.viewx.*;
import snap.web.WebFile;

/**
 * Provides an EditorPane as WebPage.
 */
public class SnapBuilderPage extends WebPage {

    // The EditorPane
    private EditorPane  _editorPane;

    /**
     * Constructor.
     */
    public SnapBuilderPage()
    {
        super();
        _editorPane = new EditorPane();
    }

    /**
     * Override to wrap ReportPage in pane with EditButton.
     */
    protected View createUI()
    {
        // Set file in EditorPane
        WebFile file = getFile();
        _editorPane.open(file);

        // Return EditorPane.UI
        return _editorPane.getUI();
    }

    /**
     * Creates a new file for use with showNewFilePanel method.
     */
    protected WebFile createNewFile(String aPath)
    {
        // Create file
        WebFile file = super.createNewFile(aPath);

        // Create text
        String sb = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<ColView PrefWidth=\"400\" PrefHeight=\"400\">\n" +
                "</ColView>\n";
        file.setText(sb);
        return file;
    }
}