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
        _editorPane.openSource(file);

        // Return EditorPane.UI
        return _editorPane.getUI();
    }

    /**
     * Creates a new file for use with showNewFilePanel method.
     */
    protected WebFile createNewFile(String aPath)
    {
        // Create file
        WebFile newFile = super.createNewFile(aPath);

        // Create text
        ParentView newDocView = EditorUtils.createNewDocView();
        ViewArchiver viewArchiver = new ViewArchiver();
        String fileText = viewArchiver.writeToXML(newDocView).getString();
        newFile.setText(fileText);

        // Return
        return newFile;
    }
}