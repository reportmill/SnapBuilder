package snapbuild.app;
import snap.gfx.*;
import snap.view.*;

/**
 * A class to manage inspector.
 */
public class InspectorPane extends ViewOwner {

    // The EditorPane
    EditorPane           _epane;
    
    // The Title label
    Label                 _titleLabel;
    
    // The ViewButton
    ToggleButton          _viewBtn;
    
    // The ScrollView that holds UI for child inspectors
    ScrollView            _inspBox;
    
    // The child inspector current installed in inspector panel
    ViewOwner            _childInspector;
    
    // The inspector for view general
    ViewTool             _viewTool;
    
    // The inspector for view looks
    ViewLooks            _viewLooks;
    
/**
 * Returns the editor pane.
 */
public EditorPane getEditorPane()  { return _epane; }

/**
 * Returns the editor.
 */
public Editor getEditor()  { return getEditorPane().getEditor(); }
    
/**
 * Initializes UI panel for the inspector.
 */
public void initUI()
{
    // Get/configure TitleLabel
    _titleLabel = getView("TitleLabel", Label.class);
    _titleLabel.setTextFill(Color.GRAY);
    
    // Get ViewButton
    _viewBtn = getView("ViewSpecificButton", ToggleButton.class);
    
    // Get/configure ContentBox
    _inspBox = getView("ContentBox", ScrollView.class);
    _inspBox.setBorder(null);
    _inspBox.setBarSize(12);
    _inspBox.setFillWidth(true);
    
    // Get ViewTool
    _viewTool = _epane._viewTool;
    
    // Get ViewLooks
    _viewLooks = new ViewLooks();
    _viewLooks._epane = _epane;
}

/**
 * Refreshes the inspector for the current editor selection.
 */
public void resetUI()
{
    // Get editor (and just return if null) and tool for selected shapes
    EditorPane epane = getEditorPane();
    Editor editor = getEditor();
    View selView = editor.getSelView();
    ViewTool tool = epane.getToolForView(selView);
    
    // If ViewGeneralButton is selected, instal inspector
    if(getViewBoolValue("ViewGeneralButton"))
        setInspector(_viewTool);
    
    // If ViewSpecificButton is selected, instal inspector for current selection
    if(getViewBoolValue("ViewSpecificButton"))
        setInspector(tool);
    
    // If ViewLooksButton is selected, install ViewLooks inspector
    if(getViewBoolValue("ViewLooksButton"))
        setInspector(_viewLooks);

    // Get the inspector (owner)
    ViewOwner owner = getInspector();
    
    // Get inspector title from owner and set
    String title = "Inspector";
    if(owner instanceof ViewTool) title = selView.getClass().getSimpleName() + " Inspector";
    _titleLabel.setText(title);

    // If owner non-null, tell it to reset
    if(owner!=null)
        owner.resetLater();
    
    // Get image for current tool and set in ShapeSpecificButton
    //Image timage = tool.getImage();
    //getView("ViewSpecificButton", ButtonBase.class).setImage(timage);
}

/**
 * Handles changes to the inspector UI controls.
 */
public void respondUI(ViewEvent anEvent)
{
    // Handle ViewPlacementButton
    //if(anEvent.equals("ViewPlacementButton")) setInspector(_viewPlacement);
    
    // Handle ViewGeneralButton
    if(anEvent.equals("ViewGeneralButton"))
        setInspector(_viewTool);
}

/**
 * Returns the inspector (owner) of the inspector pane.
 */
protected ViewOwner getInspector()  { return _childInspector; }

/**
 * Sets the inspector in the inspector pane.
 */
protected void setInspector(ViewOwner anOwner)
{
    // Set new inspector
    _childInspector = anOwner;
    
    // Get content and it grows height
    View content = anOwner.getUI();
    boolean contentGrowHeight = content.isGrowHeight();
    
    // Set content and whether Inspector ScrollView sizes or scrolls content vertically
    _inspBox.setContent(content);
    _inspBox.setFillHeight(contentGrowHeight);
}

}