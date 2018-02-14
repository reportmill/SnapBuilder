package snapbuild.app;
import snap.gfx.*;
import snap.gfx.Border.*;
import snap.util.StringUtils;
import snap.view.*;
import snap.viewx.*;

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
    // Set Font button images
    setViewItems("FontCombo", new String[] { "Arial", "ArialBold"});
    setViewSelectedItem("FontCombo", "Arial");
    getView("FontPlusButton", ButtonBase.class).setImage(Image.get(TextPane.class, "pkg.images/Font_Increase.png"));
    getView("FontMinusButton", ButtonBase.class).setImage(Image.get(TextPane.class, "pkg.images/Font_Decrease.png"));
    
    // Configure Borders
    Label l0 = getView("NoBdrButton", ButtonBase.class).getLabel(); l0.setPrefSize(16,16);
    l0.setBorder(Color.LIGHTGRAY,1);
    Label l1 = getView("LineBdrButton", ButtonBase.class).getLabel(); l1.setPrefSize(16,16);
    l1.setBorder(Color.BLACK,1);
    Label l2 = getView("LowerBdrButton", ButtonBase.class).getLabel(); l2.setPrefSize(16,16);
    l2.setBorder(new BevelBorder(0));
    Label l3 = getView("RaiseBdrButton", ButtonBase.class).getLabel(); l3.setPrefSize(16,16);
    l3.setBorder(new BevelBorder(1));
    Label l4 = getView("EtchBdrButton", ButtonBase.class).getLabel(); l4.setPrefSize(16,16);
    l4.setBorder(new EtchBorder());
}

/**
 * ResetUI.
 */
protected void resetUI()
{
    // Get Editor and SelView
    Editor editor = _epane.getEditor();
    View selView = editor.getSelView();
    
    // Update NameText, TextText, ToolTipText
    setViewText("NameText", selView.getName());
    setViewText("TextText", selView.getText());
    setViewText("ToolTipText", selView.getToolTip());
    
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
    
    // Update FontSizeText
    setViewValue("FontSizeText", selView.getFont().getSize());
    
    // Update Border Buttons
    Border bdr = selView.getBorder();
    setViewValue("NoBdrButton", bdr==null);
    setViewValue("LineBdrButton", bdr instanceof LineBorder);
    setViewValue("LowerBdrButton", bdr instanceof BevelBorder && ((BevelBorder)bdr).getType()==0);
    setViewValue("RaiseBdrButton", bdr instanceof BevelBorder && ((BevelBorder)bdr).getType()==1);
    setViewValue("EtchBdrButton", bdr instanceof EtchBorder);
    
    // Update BorderWidthSpinner
    setViewValue("BorderWidthSpinner", bdr!=null? bdr.getWidth() : 0);
    
    // Update OpacitySlider, RotationThumb
    setViewValue("OpacitySlider", selView.getOpacity());
    setViewValue("RotationThumb", selView.getRotate());
}

/**
 * ResetUI.
 */
protected void respondUI(ViewEvent anEvent)
{
    // Get Editor and SelView
    Editor editor = _epane.getEditor();
    View selView = editor.getSelView();
    
    // Handle NameText, TextText, ToolTipText
    if(anEvent.equals("NameText")) selView.setName(anEvent.getStringValue());
    if(anEvent.equals("TextText")) selView.setText(anEvent.getStringValue());
    if(anEvent.equals("ToolTipText")) selView.setToolTip(anEvent.getStringValue());
    
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
    
    // Handle FontSizeText, FontPlusButton, FontMinusButton
    if(anEvent.equals("FontSizeText")) { Font font = selView.getFont();
        selView.setFont(font.deriveFont(anEvent.getFloatValue())); }
    if(anEvent.equals("FontPlusButton")) { Font font = selView.getFont();
        selView.setFont(font.deriveFont(font.getSize()+1)); }
    if(anEvent.equals("FontMinusButton")) { Font font = selView.getFont();
        selView.setFont(font.deriveFont(font.getSize()-1)); }
    if(anEvent.equals("FontResetButton")) selView.setFont(null);
    
    // Handle NoBdrButton, LineBdrButton, LowerBdrButton, RaiseBdrButton, EtchBdrButton
    if(anEvent.equals("NoBdrButton")) selView.setBorder(null);
    if(anEvent.equals("LineBdrButton")) selView.setBorder(Color.BLACK, 1);
    if(anEvent.equals("LowerBdrButton")) selView.setBorder(new BevelBorder(0));
    if(anEvent.equals("RaiseBdrButton")) selView.setBorder(new BevelBorder(1));
    if(anEvent.equals("EtchBdrButton")) selView.setBorder(new EtchBorder());
    
    // Handle BorderColorButton
    if(anEvent.equals("BorderColorButton")) {
        Border bdr = selView.getBorder();
        Color color = getView("BorderColorButton", ColorButton.class).getColor();
        double w = bdr instanceof LineBorder? ((LineBorder)bdr).getWidth() : 1;
        selView.setBorder(color, w);
    }
    
    // Handle BorderWidthSpinner
    if(anEvent.equals("BorderWidthSpinner")) {
        Border bdr = selView.getBorder();
        Color color = bdr!=null? bdr.getColor() : Color.BLACK;
        selView.setBorder(color, anEvent.getFloatValue());
    }
    
    // Handle FillColorButton
    if(anEvent.equals("FillColorButton"))
        selView.setFill(getView("FillColorButton", ColorButton.class).getColor());
    
    // Handle OpacitySlider
    if(anEvent.equals("OpacitySlider")) selView.setOpacity(anEvent.getFloatValue());
    if(anEvent.equals("RotationThumb")) selView.setRotate(anEvent.getFloatValue());
}

}