package snapbuild.apptools;
import snapbuild.app.ViewTool;
import snap.view.*;

/**
 * A class to manage UI to edit properties of a ButtonBase view.
 */
public class ButtonBaseTool <T extends ButtonBase> extends ViewTool <T> {

/**
 * Returns the name.
 */
public String getName()  { return "ButtonBase Props"; }

/**
 * Reset UI.
 */
protected void resetUI()
{
    // Get selected view
    T selView = getSelView();

    // Update ShowBorderCheckBox
    setViewValue("ShowBorderCheckBox", selView.isShowBorder());
}

/**
 * Respond to UI.
 */
protected void respondUI(ViewEvent anEvent)
{
    // Get selected view
    T selView = getSelView();

    // Handle ShowBorderCheckBox
    if(anEvent.equals("ShowBorderCheckBox"))
        selView.setShowBorder(anEvent.getBoolValue());
}

}