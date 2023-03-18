package snapbuild.apptools;
import snapbuild.app.ViewTool;
import snap.view.*;

/**
 * A ViewTool subclass for editing TextFields.
 */
public class TextFieldTool<T extends TextField> extends ViewTool<T> {

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Get selected view
        T selView = getSelView();

        // Update PromptText, RoundingSpinner, ColCountSpinner
        setViewValue("PromptText", selView.getPromptText());
        setViewValue("RoundingSpinner", selView.getBorderRadius());
        setViewValue("ColCountSpinner", selView.getColCount());
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get selected view
        T selView = getSelView();

        // Handle PromptText, RoundingSpinner, ColCountSpinner
        if (anEvent.equals("PromptText")) selView.setPromptText(anEvent.getStringValue());
        if (anEvent.equals("RoundingSpinner")) selView.setBorderRadius(anEvent.getFloatValue());
        if (anEvent.equals("ColCountSpinner")) selView.setColCount(anEvent.getIntValue());
    }

}