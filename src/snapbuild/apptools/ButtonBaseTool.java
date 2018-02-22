package snapbuild.apptools;
import snap.util.SnapUtils;
import snapbuild.app.ViewTool;
import snap.gfx.Pos;
import snap.view.*;

/**
 * A class to manage UI to edit properties of a ButtonBase view.
 */
public class ButtonBaseTool <T extends ButtonBase> extends ViewTool <T> {

/**
 * Reset UI.
 */
protected void resetUI()
{
    // Get selected view
    T selView = getSelView();

    // Update ImageNameText, ShowBorderCheckBox
    setViewValue("ImageNameText", selView.getImageName());
    setViewValue("ShowBorderCheckBox", selView.isShowBorder());
    
    // Update Pos buttons
    Pos pos = selView.getPosition();
    ToggleButton selBtn = pos!=null? getView("Pos" + pos.ordinal(), ToggleButton.class) : null;
    getToggleGroup("tga").setSelected(selBtn);
}

/**
 * Respond to UI.
 */
protected void respondUI(ViewEvent anEvent)
{
    // Get selected view
    T selView = getSelView();

    // Handle ImageNameText, ShowBorderCheckBox
    if(anEvent.equals("ImageNameText")) selView.setImageName(anEvent.getStringValue());
    if(anEvent.equals("ShowBorderCheckBox")) selView.setShowBorder(anEvent.getBoolValue());
    
    // Respond to Pos buttons
    if(anEvent.getName().startsWith("Pos")) {
        int pval = SnapUtils.intValue(anEvent.getName());
        Pos pos = Pos.values()[pval];
        selView.setPosition(pos);
    }
}

}