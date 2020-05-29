package snapbuild.apptools;
import snap.view.ImageView;
import snap.view.ViewEvent;
import snapbuild.app.ViewTool;

/**
 * A class to manage UI to edit properties of a ImageView.
 */
public class ImageViewTool<T extends ImageView> extends ViewTool <T> {

/**
 * Reset UI.
 */
protected void resetUI()
{
    // Get selected view
    T selView = getSelView();

    // Update ImageNameText
    setViewValue("ImageNameText", selView.getImageName());

    // FillWidthCheckBox, FillHeightCheckBox, KeepAspectCheckBox
    setViewValue("FillWidthCheckBox", selView.isFillWidth());
    setViewValue("FillHeightCheckBox", selView.isFillHeight());
    setViewValue("KeepAspectCheckBox", selView.isKeepAspect());
}

/**
 * Respond to UI.
 */
protected void respondUI(ViewEvent anEvent)
{
    // Get selected view
    T selView = getSelView();

    // Handle ImageNameText
    if(anEvent.equals("ImageNameText")) selView.setImageName(anEvent.getStringValue());

    // Handle FillWidthCheckBox, FillHeightCheckBox, KeepAspectCheckBox
    if(anEvent.equals("FillWidthCheckBox"))
        selView.setFillWidth(anEvent.getBoolValue());
    if(anEvent.equals("FillHeightCheckBox"))
        selView.setFillHeight(anEvent.getBoolValue());
    if(anEvent.equals("KeepAspectCheckBox"))
        selView.setKeepAspect(anEvent.getBoolValue());
}

}