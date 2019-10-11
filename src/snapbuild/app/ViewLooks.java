package snapbuild.app;
import snap.gfx.*;
import snap.gfx.Border.*;
import snap.view.*;
import snap.viewx.ColorButton;
import snap.viewx.TextPane;

/**
 * An inspector to modify view looks.
 */
public class ViewLooks extends ViewOwner {

    // The EditorPane
    EditorPane           _epane;
    
/**
 * Returns the editor pane.
 */
public EditorPane getEditorPane()  { return _epane; }

/**
 * Returns the editor.
 */
public Editor getEditor()  { return getEditorPane().getEditor(); }
    
/**
 * Initialize UI.
 */
protected void initUI()
{
    // Set Font button images
    setViewItems("FontCombo", new String[] { "Arial", "ArialBold"});
    setViewSelItem("FontCombo", "Arial");
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
    
    // Update FontSizeText
    setViewValue("FontSizeText", selView.getFont().getSize());
    
    // Update Border Buttons
    Border bdr = selView.getBorder();
    setViewValue("NoBdrButton", bdr==null);
    setViewValue("LineBdrButton", bdr instanceof LineBorder);
    setViewValue("LowerBdrButton", bdr instanceof BevelBorder && ((BevelBorder)bdr).getType()==0);
    setViewValue("RaiseBdrButton", bdr instanceof BevelBorder && ((BevelBorder)bdr).getType()==1);
    setViewValue("EtchBdrButton", bdr instanceof EtchBorder);
    
    // Update BorderColorButton, BorderWidthSpinner
    //getView("BorderColorButton", ColorButton.class).setColor(bdr!=null? bdr.getColor() : null);
    setViewValue("BorderWidthSpinner", bdr!=null? bdr.getWidth() : 0);
    
    // Update FillColorButton, TextFillColorButton
    //Color color = selView.getFill() instanceof Color? (Color)selView.getFill() : null;
    //getView("FillColorButton", ColorButton.class).setColor(color);
    //getView("TextColorButton", ColorButton.class).setColor(bdr!=null? bdr.getColor() : null);
    
    // Update OpacitySlider, RotationThumb, VerticalCheckBox
    setViewValue("OpacitySlider", selView.getOpacity());
    setViewValue("RotationThumb", selView.getRotate());
}

/**
 * Respond UI.
 */
protected void respondUI(ViewEvent anEvent)
{
    // Get Editor and SelView
    Editor editor = _epane.getEditor();
    View selView = editor.getSelView();
    
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
    
    // Handle TextFillColorButton
    if(anEvent.equals("TextFillColorButton"))
        ViewHpr.getHpr(selView).setTextFill(selView, getView("TextFillColorButton", ColorButton.class).getColor());
    
    // Handle OpacitySlider, RotationThumb, VerticalCheckBox
    if(anEvent.equals("OpacitySlider")) selView.setOpacity(anEvent.getFloatValue());
    if(anEvent.equals("RotationThumb")) selView.setRotate(anEvent.getFloatValue());
}
    
}