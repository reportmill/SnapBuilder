package snapbuild.apptools;
import snap.view.ThumbWheel;
import snap.view.ViewEvent;
import snapbuild.app.ViewTool;

/**
 * A ViewTool subclass for editing ThumbWheel.
 */
public class ThumbWheelTool<T extends ThumbWheel> extends ViewTool<T> {

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Get selected view
        T selView = getSelView();

        // Update VisibleMinText, VisibleMaxText, AbsoluteMinText, AbsoluteMaxText
        setViewValue("VisibleMinText", selView.getVisibleMin());
        setViewValue("VisibleMaxText", selView.getVisibleMax());
        setViewValue("AbsoluteMinText", selView.getAbsoluteMin());
        setViewValue("AbsoluteMaxText", selView.getAbsoluteMax());

        // Update LinearButton, RadialButton
        setViewValue("LinearButton", selView.isLinear());
        setViewValue("RadialButton", selView.isRadial());
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get selected view
        T selView = getSelView();

        // Handle VisibleMinText, VisibleMaxText, AbsoluteMinText, AbsoluteMaxText
        if (anEvent.equals("VisibleMinText"))
            selView.setVisibleMin(anEvent.getFloatValue());
        if (anEvent.equals("VisibleMaxText"))
            selView.setVisibleMax(anEvent.getFloatValue());
        if (anEvent.equals("AbsoluteMinText"))
            selView.setAbsoluteMin(anEvent.getFloatValue());
        if (anEvent.equals("AbsoluteMaxText"))
            selView.setAbsoluteMax(anEvent.getFloatValue());

        // Handle LinearButton, RadialButton
        if (anEvent.equals("LinearButton"))
            selView.setType(ThumbWheel.TYPE_LINEAR);
        if (anEvent.equals("RadialButton"))
            selView.setType(ThumbWheel.TYPE_RADIAL);
    }

}