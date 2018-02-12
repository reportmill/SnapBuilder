package snapbuild.app;
import snap.gfx.Color;
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
    if(anEvent.equals("TextText"))
        selView.setText(anEvent.getStringValue());
    
    // Handle PrefWidthSpinner, PrefHeightSpinner
    if(anEvent.equals("PrefWidthSpinner"))
        selView.setPrefWidth(anEvent.getFloatValue());
    if(anEvent.equals("PrefHeightSpinner"))
        selView.setPrefHeight(anEvent.getFloatValue());
}

}