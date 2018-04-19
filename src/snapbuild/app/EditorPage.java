package snapbuild.app;
import snap.view.*;
import snap.viewx.*;
import snap.web.WebFile;

/**
 * Provides a WebPage version of EditorPane.
 */
public class EditorPage extends SnapPage {

/**
 * Override to wrap ReportPage in pane with EditButton.
 */
protected View createUI()  { return new EditorPane().open(getFile()).getUI(); }

/**
 * Creates a new file for use with showNewFilePanel method.
 */
protected WebFile createNewFile(String aPath)
{
    // Create file
    WebFile file = super.createNewFile(aPath);
    
    // Create text
    StringBuffer sb = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    sb.append("<ColView Padding=\"4\" Spacing=\"4\">\n");
    sb.append("</ColView>\n");
    file.setText(sb.toString());
    return file;
}

}