package snapbuild.apptools;
import snapbuild.app.ViewTool;
import snap.view.*;

/**
 * A ViewTool subclass for RowView.
 */
public class RowViewTool <T extends RowView> extends ViewTool <T> {

/**
 * Reset UI.
 */
protected void resetUI()
{
    // Get selected view
    T selView = getSelView();

    // Update FillHeightCheckBox
    setViewValue("FillHeightCheckBox", selView.isFillHeight());
}

/**
 * Respond to UI.
 */
protected void respondUI(ViewEvent anEvent)
{
    // Get selected view
    T selView = getSelView();

    // Handle FillHeightCheckBox
    if(anEvent.equals("FillHeightCheckBox"))
        selView.setFillHeight(anEvent.getBoolValue());
}

}