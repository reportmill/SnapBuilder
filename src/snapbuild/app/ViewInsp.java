package snapbuild.app;
import snap.gfx.*;
import snap.util.StringUtils;
import snap.view.*;

/**
 * An inspector for View properties.
 */
public class ViewInsp extends ViewOwner {

    // The EditorPane
    EditorPane         _epane;
    
/**
 * Creates a ViewInsp for EditorPane.
 */
public ViewInsp(EditorPane anEP)  { _epane = anEP; }

/**
 * Initialize UI.
 */
protected void initUI()
{
    //Spinner pws = getView("PrefWidthSpinner", Spinner.class), phs = getView("PrefHeightSpinner", Spinner.class);
}

/**
 * ResetUI.
 */
protected void resetUI()
{
    // Get Editor and SelView
    Editor editor = _epane.getEditor();
    View selView = editor.getSelView();
    
    // Update NameText, TextText
    setViewText("NameText", selView.getName());
    setViewText("TextText", selView.getText());
    
    // Update PrefWidthSpinner, PrefHeightSpinner
    Spinner pws = getView("PrefWidthSpinner", Spinner.class), phs = getView("PrefHeightSpinner", Spinner.class);
    pws.setValue(selView.getPrefWidth());
    phs.setValue(selView.getPrefHeight());
    pws.getTextField().setTextFill(selView.isPrefWidthSet()? Color.BLACK : Color.GRAY);
    phs.getTextField().setTextFill(selView.isPrefHeightSet()? Color.BLACK : Color.GRAY);
    
    // Update MinWidthSpinner, MinHeightSpinner
    Spinner mws = getView("MinWidthSpinner", Spinner.class), mhs = getView("MinHeightSpinner", Spinner.class);
    mws.setValue(selView.getMinWidth());
    mhs.setValue(selView.getMinHeight());
    mws.getTextField().setTextFill(selView.isMinWidthSet()? Color.BLACK : Color.GRAY);
    mhs.getTextField().setTextFill(selView.isMinHeightSet()? Color.BLACK : Color.GRAY);
    
    // Update PaddingText, SpacingSpinner
    setViewValue("PaddingText", selView.getPadding().getString());
    setViewValue("SpacingSpinner", selView.getSpacing());
    
    // Update LeanX, LeanY
    setViewValue("LeanX0", selView.getLeanX()==HPos.LEFT);
    setViewValue("LeanX1", selView.getLeanX()==HPos.CENTER);
    setViewValue("LeanX2", selView.getLeanX()==HPos.RIGHT);
    setViewValue("LeanX3", selView.getLeanX()==null);
    setViewValue("LeanY0", selView.getLeanY()==VPos.TOP);
    setViewValue("LeanY1", selView.getLeanY()==VPos.CENTER);
    setViewValue("LeanY2", selView.getLeanY()==VPos.BOTTOM);
    setViewValue("LeanY3", selView.getLeanY()==null);
    
    // Update GrowWidthCheckBox, GrowHeightCheckBox
    setViewValue("GrowWidthCheckBox", selView.isGrowWidth());
    setViewValue("GrowHeightCheckBox", selView.isGrowHeight());
    
    // Update Align
    Pos align = selView.getAlign();
    setViewValue("Align" + align.ordinal(), true);
}

/**
 * ResetUI.
 */
protected void respondUI(ViewEvent anEvent)
{
    // Get Editor and SelView
    Editor editor = _epane.getEditor();
    View selView = editor.getSelView();
    
    // Handle NameText, TextText
    if(anEvent.equals("NameText")) selView.setName(anEvent.getStringValue());
    if(anEvent.equals("TextText")) selView.setText(anEvent.getStringValue());
    
    // Handle PrefWidthSpinner, PrefHeightSpinner, MinWidthSpinner, MinHeightSpinner
    if(anEvent.equals("PrefWidthSpinner")) selView.setPrefWidth(anEvent.getFloatValue());
    if(anEvent.equals("PrefHeightSpinner")) selView.setPrefHeight(anEvent.getFloatValue());
    if(anEvent.equals("MinWidthSpinner")) selView.setMinWidth(anEvent.getFloatValue());
    if(anEvent.equals("MinHeightSpinner")) selView.setMinHeight(anEvent.getFloatValue());
    
    // Handle PaddingText, SpacingSpinner
    if(anEvent.equals("PaddingText")) selView.setPadding(Insets.get(anEvent.getStringValue()));
    if(anEvent.equals("SpacingSpinner")) selView.setSpacing(anEvent.getFloatValue());
    
    // Handle LeanX, LeanY
    if(anEvent.equals("LeanX0")) selView.setLeanX(HPos.LEFT);
    if(anEvent.equals("LeanX1")) selView.setLeanX(HPos.CENTER);
    if(anEvent.equals("LeanX2")) selView.setLeanX(HPos.RIGHT);
    if(anEvent.equals("LeanX3")) selView.setLeanX(null);
    if(anEvent.equals("LeanY0")) selView.setLeanY(VPos.TOP);
    if(anEvent.equals("LeanY1")) selView.setLeanY(VPos.CENTER);
    if(anEvent.equals("LeanY2")) selView.setLeanY(VPos.BOTTOM);
    if(anEvent.equals("LeanY3")) selView.setLeanY(null);
    
    // Handle GrowWidthCheckBox, GrowHeightCheckBox
    if(anEvent.equals("GrowWidthCheckBox")) selView.setGrowWidth(anEvent.getBoolValue());
    if(anEvent.equals("GrowHeightCheckBox")) selView.setGrowHeight(anEvent.getBoolValue());
    
    // Handle AlignX
    String name = anEvent.getName();
    if(name.startsWith("Align")) {
        int val = StringUtils.intValue(name);
        Pos pos = Pos.values()[val];
        selView.setAlign(pos);
    }
}

}