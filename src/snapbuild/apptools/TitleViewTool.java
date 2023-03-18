package snapbuild.apptools;
import snapbuild.app.ViewTool;
import snap.view.*;

/**
 * A ViewTool subclass for editing Labels.
 */
public class TitleViewTool<T extends TitleView> extends ViewTool<T> {

    /**
     * Initialize UI.
     */
    protected void initUI()
    {
        // Set StyleComboBox values
        ComboBox styleComboBox = getView("StyleComboBox", ComboBox.class);
        TitleView.TitleStyle vals[] = TitleView.TitleStyle.values();
        styleComboBox.setItems((Object[]) vals);
    }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Get selected view
        T selView = getSelView();

        // Update StyleComboBox
        setViewValue("StyleComboBox", selView.getTitleStyle());

        // Update CollapsibleCheckBox, ExpandedCheckBox
        setViewValue("CollapsibleCheckBox", selView.isCollapsible());
        setViewValue("ExpandedCheckBox", selView.isExpanded());
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get selected view
        T selView = getSelView();

        // Handle StyleComboBox
        if (anEvent.equals("StyleComboBox")) {
            TitleView.TitleStyle style = (TitleView.TitleStyle) anEvent.getSelItem();
            selView.setTitleStyle(style);
        }

        // CollapsibleCheckBox, ExpandedCheckBox
        if (anEvent.equals("CollapsibleCheckBox"))
            selView.setCollapsible(anEvent.getBoolValue());
        if (anEvent.equals("ExpandedCheckBox"))
            selView.setExpanded(anEvent.getBoolValue());
    }

}