package snapbuild.apptools;
import snapbuild.app.ViewTool;
import snap.view.*;

/**
 * A class to manage UI for editing Labels.
 */
public class LabelTool <T extends Label> extends ViewTool <T> {

/**
 * Reset UI.
 */
protected void resetUI()
{
    // Get selected view
    T selView = getSelView();

    // Update ImageNameText
    setViewValue("ImageNameText", selView.getImageName());
}

/**
 * Respond to UI.
 */
protected void respondUI(ViewEvent anEvent)
{
    // Get selected view
    T selView = getSelView();

    // Handle ImageNameText
    if(anEvent.equals("ImageNameText"))
        selView.setImageName(anEvent.getStringValue());
}

}