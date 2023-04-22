package snapbuild.app;
import java.text.DecimalFormat;
import snap.geom.*;
import snap.gfx.*;
import snap.props.PropChange;
import snap.util.*;
import snap.view.*;

/**
 * An class to manage UI editing of a View.
 */
public class ViewToolImpl<T extends View> extends ViewTool<T> {

    // The ScrollView that holds UI for child inspectors
    private ColView _inspBox;

    // The child inspector current installed in inspector panel
    private ViewOwner _subInsp;

    /**
     * Returns the inspector (owner) of the inspector pane.
     */
    protected ViewOwner getInspector()
    {
        return _subInsp;
    }

    /**
     * Sets the inspector in the inspector pane.
     */
    protected void setInspector(ViewOwner anOwner)
    {
        // If already set, just return
        if (anOwner == _subInsp) return;

        // Set new inspector
        _subInsp = anOwner;

        // Get content, content and set
        if (_inspBox.getChildCount() > 0)
            _inspBox.removeChild(0);
        _inspBox.addChild(_subInsp.getUI(), 0);

        // Set label
        Editor editor = _editorPane.getEditor();
        View selView = editor.getSelView();
        String subclassCollapseViewTitle = selView.getClass().getSimpleName() + " Settings";
        setViewText("SubclassCollapseView", subclassCollapseViewTitle);
    }

    /**
     * Initialize UI.
     */
    protected void initUI()
    {
        // Add Collapser for ViewLabel and ViewPropsView
        View viewPropsView = getView("ViewPropsView");
        CollapseView.replaceViewWithCollapseView(viewPropsView, "View Settings");

        // Add Collapser for SubclassLabel and SubclassPropsView
        View subclassPropsView = getView("SubclassPropsView");
        CollapseView.replaceViewWithCollapseView(subclassPropsView, "Subclass");

        // Add Collapser for ViewLabel and ViewPropsView
        //Label viewLabel = getView("ViewLabel", Label.class);
        //View viewPropsView = getView("ViewPropsView");
        //new Collapser(viewPropsView, viewLabel);
        //viewLabel.getStringView().setGrowWidth(true);
        //viewLabel.setTextFill(Color.GRAY);

        // Add Collapser for SubclassLabel and SubclassPropsView
        //Label subclassLabel = getView("SubclassLabel", Label.class);
        //View subclassPropsView = getView("SubclassPropsView");
        //new Collapser(subclassPropsView, subclassLabel);
        //subclassLabel.getStringView().setGrowWidth(true);
        //subclassLabel.setTextFill(Color.GRAY);

        // Register MarginText, PadText to update
        getView("MarginText").addPropChangeListener(pc -> insetsTextFieldChanged(pc),
                TextField.Sel_Prop, View.Focused_Prop);
        getView("PadText").addPropChangeListener(pc -> insetsTextFieldChanged(pc),
                TextField.Sel_Prop, View.Focused_Prop);

        // Get SubclassContainer
        _inspBox = getView("SubclassContainer", ColView.class);
    }

    /**
     * ResetUI.
     */
    protected void resetUI()
    {
        // Get Editor and SelView
        Editor editor = _editorPane.getEditor();
        View selView = editor.getSelView();

        // Update NameText, TextText, ToolTipText
        setViewText("NameText", selView.getName());
        setViewText("TextText", selView.getText());
        setViewText("ToolTipText", selView.getToolTip());

        // Update PrefWidthSpinner, PrefHeightSpinner, PrefWidthThumb, PrefHeightThumb
        Spinner pws = getView("PrefWidthSpinner", Spinner.class);
        Spinner phs = getView("PrefHeightSpinner", Spinner.class);
        pws.setValue(selView.getPrefWidth());
        phs.setValue(selView.getPrefHeight());
        pws.getTextField().setTextFill(selView.isPrefWidthSet() ? Color.BLACK : Color.GRAY);
        phs.getTextField().setTextFill(selView.isPrefHeightSet() ? Color.BLACK : Color.GRAY);
        setViewValue("PrefWidthThumb", selView.getPrefWidth());
        setViewValue("PrefHeightThumb", selView.getPrefHeight());

        // Update MinWidthSpinner, MinHeightSpinner, MinWidthThumb, MinHeightThumb
        Spinner mws = getView("MinWidthSpinner", Spinner.class);
        Spinner mhs = getView("MinHeightSpinner", Spinner.class);
        mws.setValue(selView.getMinWidth());
        mhs.setValue(selView.getMinHeight());
        mws.getTextField().setTextFill(selView.isMinWidthSet() ? Color.BLACK : Color.GRAY);
        mhs.getTextField().setTextFill(selView.isMinHeightSet() ? Color.BLACK : Color.GRAY);
        setViewValue("MinWidthThumb", selView.getMinWidth());
        setViewValue("MinHeightThumb", selView.getMinHeight());

        // Update BoundsText
        Rect bnds = selView.getBounds();
        setViewText("BoundsText", fmt(bnds.x) + ", " + fmt(bnds.y) + ", " + fmt(bnds.width) + ", " + fmt(bnds.height));

        // Update MarginLabel, MarginText
        setViewValue("MarginLabel", getInsetsSelStringForTextFieldName("MarginText"));
        setViewValue("MarginText", getInsetsString(selView.getMargin()));

        // Update PadLabel, PadText
        setViewValue("PadLabel", getInsetsSelStringForTextFieldName("PadText"));
        setViewValue("PadText", getInsetsString(selView.getPadding()));

        // Update SpaceText
        setViewValue("SpaceText", Convert.stringValue(selView.getSpacing()));

        // Update LeanX, LeanY
        setViewValue("LeanX0", selView.getLeanX() == HPos.LEFT);
        setViewValue("LeanX1", selView.getLeanX() == HPos.CENTER);
        setViewValue("LeanX2", selView.getLeanX() == HPos.RIGHT);
        setViewValue("LeanY0", selView.getLeanY() == VPos.TOP);
        setViewValue("LeanY1", selView.getLeanY() == VPos.CENTER);
        setViewValue("LeanY2", selView.getLeanY() == VPos.BOTTOM);

        // Update GrowWidthCheckBox, GrowHeightCheckBox, VerticalCheckBox
        setViewValue("GrowWidthCheckBox", selView.isGrowWidth());
        setViewValue("GrowHeightCheckBox", selView.isGrowHeight());
        setViewValue("VerticalCheckBox", selView.isVertical());

        // Update Align
        Pos align = selView.getAlign();
        setViewValue("Align" + align.ordinal(), true);

        // Make sure that SubclassContainer has child tool
        ViewTool tool = _editorPane.getToolForView(selView);
        setInspector(tool);
        tool.resetLater();
    }

    /**
     * Respond UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get Editor and SelView
        Editor editor = _editorPane.getEditor();
        View selView = editor.getSelView();

        // Handle NameText, TextText, ToolTipText
        if (anEvent.equals("NameText")) selView.setName(anEvent.getStringValue());
        if (anEvent.equals("TextText")) selView.setText(anEvent.getStringValue());
        if (anEvent.equals("ToolTipText")) selView.setToolTip(anEvent.getStringValue());

        // Handle PrefWidthSpinner, PrefHeightSpinner, PrefWidthThumb, PrefHeightThumb
        if (anEvent.equals("PrefWidthSpinner")) selView.setPrefWidth(anEvent.getFloatValue());
        if (anEvent.equals("PrefHeightSpinner")) selView.setPrefHeight(anEvent.getFloatValue());
        if (anEvent.equals("PrefWidthThumb")) selView.setPrefWidth(anEvent.getIntValue());
        if (anEvent.equals("PrefHeightThumb")) selView.setPrefHeight(anEvent.getIntValue());

        // Handle MinWidthSpinner, MinHeightSpinner, MinWidthThumb, MinHeightThumb
        if (anEvent.equals("MinWidthSpinner")) selView.setMinWidth(anEvent.getFloatValue());
        if (anEvent.equals("MinHeightSpinner")) selView.setMinHeight(anEvent.getFloatValue());
        if (anEvent.equals("MinWidthThumb")) selView.setMinWidth(anEvent.getIntValue());
        if (anEvent.equals("MinHeightThumb")) selView.setMinHeight(anEvent.getIntValue());

        // Handle PWResetButton, PHResetButton
        if (anEvent.equals("PWResetButton")) selView.setPrefWidth(-1);
        if (anEvent.equals("PHResetButton")) selView.setPrefHeight(-1);

        // Handle MWResetButton, MHResetButton
        if (anEvent.equals("MWResetButton")) selView.setMinWidth(-1);
        if (anEvent.equals("MHResetButton")) selView.setMinHeight(-1);

        // Handle MarginText, MarginAdd1Button, MarginSub1Button, MarginResetButton
        if (anEvent.equals("MarginText")) selView.setMargin(Insets.get(anEvent.getStringValue()));
        if (anEvent.equals("MarginAdd1Button")) adjustMargin(selView, 1);
        if (anEvent.equals("MarginSub1Button")) adjustMargin(selView, -1);
        if (anEvent.equals("MarginResetButton"))
            selView.setMargin((Insets) selView.getPropDefault(View.Margin_Prop));

        // Handle PadText, PadAdd1Button, PadSub1Button, PadResetButton
        if (anEvent.equals("PadText")) selView.setPadding(Insets.get(anEvent.getStringValue()));
        if (anEvent.equals("PadAdd1Button")) adjustPadding(selView, 1);
        if (anEvent.equals("PadSub1Button")) adjustPadding(selView, -1);
        if (anEvent.equals("PadResetButton"))
            selView.setPadding((Insets) selView.getPropDefault(View.Padding_Prop));

        // Handle SpaceText, SpaceAdd5Button, SpaceResetButton
        if (anEvent.equals("SpaceText")) selView.setSpacing(anEvent.getFloatValue());
        if (anEvent.equals("SpaceAdd1Button")) selView.setSpacing(selView.getSpacing() + 1);
        if (anEvent.equals("SpaceSub1Button")) selView.setSpacing(selView.getSpacing() - 1);
        if (anEvent.equals("SpaceResetButton")) selView.setSpacing(0);

        // Handle LeanX, LeanY
        if (anEvent.equals("LeanX0")) selView.setLeanX(HPos.LEFT);
        if (anEvent.equals("LeanX1")) selView.setLeanX(HPos.CENTER);
        if (anEvent.equals("LeanX2")) selView.setLeanX(HPos.RIGHT);
        if (anEvent.equals("LeanXReset")) selView.setLeanX(null);
        if (anEvent.equals("LeanY0")) selView.setLeanY(VPos.TOP);
        if (anEvent.equals("LeanY1")) selView.setLeanY(VPos.CENTER);
        if (anEvent.equals("LeanY2")) selView.setLeanY(VPos.BOTTOM);
        if (anEvent.equals("LeanYReset")) selView.setLeanY(null);

        // Handle GrowWidthCheckBox, GrowHeightCheckBox, VerticalCheckBox
        if (anEvent.equals("GrowWidthCheckBox")) selView.setGrowWidth(anEvent.getBoolValue());
        if (anEvent.equals("GrowHeightCheckBox")) selView.setGrowHeight(anEvent.getBoolValue());
        if (anEvent.equals("VerticalCheckBox")) selView.setVertical(anEvent.getBoolValue());

        // Handle AlignX
        String name = anEvent.getName();
        if (name.startsWith("Align")) {
            int val = StringUtils.intValue(name);
            Pos pos = Pos.values()[val];
            selView.setAlign(pos);
        }
    }

    /**
     * Adjust margin for given view by given amount (based on textfield selection).
     */
    private void adjustMargin(View aView, double aVal)
    {
        Insets ins = getAdjustedInsetsForTextFieldName("MarginText", aVal);
        aView.setMargin(ins);
    }

    /**
     * Adjust padding for given view by given amount (based on textfield selection).
     */
    private void adjustPadding(View aView, double aVal)
    {
        Insets ins = getAdjustedInsetsForTextFieldName("PadText", aVal);
        aView.setPadding(ins);
    }

    /**
     * Returns the adjusted insets for given TextField name and value.
     */
    private Insets getAdjustedInsetsForTextFieldName(String aName, double aVal)
    {
        // Get TextField and insets
        TextField text = getView(aName, TextField.class);
        Insets ins = Insets.get(text.getText());

        // Get range
        Range range = getInsetsSelRangeForTextFieldName(aName);
        if (range == null) range = new Range(0, 3);

        // Adjust range
        for (int i = range.start; i <= range.end; i++) {
            switch (i) {
                case 0:
                    ins.top = Math.max(ins.top + aVal, 0);
                    break;
                case 1:
                    ins.right = Math.max(ins.right + aVal, 0);
                    break;
                case 2:
                    ins.bottom = Math.max(ins.bottom + aVal, 0);
                    break;
                case 3:
                    ins.left = Math.max(ins.left + aVal, 0);
                    break;
            }
        }

        // Return insets
        return ins;
    }

    /**
     * Returns the selected range for insets string.
     */
    private Range getInsetsSelRangeForTextFieldName(String aName)
    {
        // Get text field (just return null if not focused)
        TextField text = getView(aName, TextField.class);
        if (!text.isFocused()) return null;

        // Get string and textfield sel start/end
        String str = text.getText();
        int selStart = text.getSelStart(), selEnd = text.getSelEnd();

        // Using commas as landmarks for insets fields return range of selection
        int start = countCommas(str, selStart);
        int end = countCommas(str, selEnd);
        return new Range(start, end);
    }

    /**
     * Returns the selected range for insets string.
     */
    private String getInsetsSelStringForTextFieldName(String aName)
    {
        Range range = getInsetsSelRangeForTextFieldName(aName);
        if (range == null) return "";
        StringBuffer sb = new StringBuffer();
        for (int i = range.start; i <= range.end; i++) {
            switch (i) {
                case 0:
                    sb.append("top, ");
                    break;
                case 1:
                    sb.append("right, ");
                    break;
                case 2:
                    sb.append("bottom, ");
                    break;
                case 3:
                    sb.append("left, ");
                    break;
            }
        }
        sb.delete(sb.length() - 2, sb.length());
        return sb.toString();
    }

    /**
     * Returns a string representation of this Insets.
     */
    private String getInsetsString(Insets anIns)
    {
        String t = StringUtils.toString(anIns.top), r = StringUtils.toString(anIns.right);
        String b = StringUtils.toString(anIns.bottom), l = StringUtils.toString(anIns.left);
        return t + ", " + r + ", " + b + ", " + l;
    }

    /**
     * Called when MarginText or PadText change focus or selection to update mini-label.
     */
    private void insetsTextFieldChanged(PropChange aPC)
    {
        TextField text = (TextField) aPC.getSource();
        String name = text.getName(), name2 = name.replace("Text", "Label");
        Label label = getView(name2, Label.class);
        label.setText(getInsetsSelStringForTextFieldName(name));
    }

    /**
     * Returns the number of commas in given string up to given index.
     */
    private int countCommas(String aStr, int anInd)
    {
        int cc = 0;
        for (int i = 0; i < anInd; i++) if (aStr.charAt(i) == ',') cc++;
        return cc;
    }

    /**
     * Returns the name.
     */
    public String getName()
    {
        return "View Props";
    }

    // Format
    private static String fmt(double aVal)
    {
        return _fmt.format(aVal);
    }

    private static DecimalFormat _fmt = new DecimalFormat("#.##");
}