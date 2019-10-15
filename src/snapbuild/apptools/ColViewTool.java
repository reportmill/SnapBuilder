package snapbuild.apptools;
import snapbuild.app.ViewTool;
import snap.view.*;

/**
 * A ViewTool subclass for ColView.
 */
public class ColViewTool <T extends ColView> extends ViewTool <T> {

/**
 * Reset UI.
 */
protected void resetUI()
{
    // Get selected view
    T selView = getSelView();

    // Update FillWidthCheckBox
    setViewValue("FillWidthCheckBox", selView.isFillWidth());
}

/**
 * Respond to UI.
 */
protected void respondUI(ViewEvent anEvent)
{
    // Get selected view
    T selView = getSelView();

    // Handle FillWidthCheckBox
    if(anEvent.equals("FillWidthCheckBox"))
        selView.setFillWidth(anEvent.getBoolValue());
}

}