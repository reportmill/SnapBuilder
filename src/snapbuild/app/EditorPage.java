package snapbuild.app;
import snap.view.*;
import snap.viewx.*;

/**
 * Provides a WebPage version of EditorPane.
 */
public class EditorPage extends SnapPage {

/**
 * Override to wrap ReportPage in pane with EditButton.
 */
protected View createUI()  { return new EditorPane().open(getFile()).getUI(); }

}