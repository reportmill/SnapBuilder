package snapbuild.app;
import rmdraw.editors.StylerPane;
import snap.gfx.*;
import snap.view.*;

/**
 * A class to manage inspector.
 */
public class InspectorPane extends ViewOwner {

    // The EditorPane
    protected EditorPane  _epane;
    
    // The Title label
    private Label  _titleLabel;
    
    // The ScrollView that holds UI for child inspectors
    private ScrollView  _inspBox;
    
    // The child inspector current installed in inspector panel
    private ViewOwner  _childInspector;

    // The Gallery pane
    private GalleryPane  _gallery;
    
    // The inspector for view general
    private ViewTool  _viewTool;
    
    // The inspector for View Fill, Border, Effect
    private StylerPane  _stylerPane;

    // Constants for the Inspectors
    public static final String GALLERY_PANE = "Gallery";
    public static final String VIEW_PANE = "View";
    public static final String STYLE_PANE = "STYLE";

    
    /**
     * Returns the editor pane.
     */
    public EditorPane getEditorPane()  { return _epane; }

    /**
     * Returns the editor.
     */
    public Editor getEditor()  { return getEditorPane().getEditor(); }

    /**
     * Sets the given named inspector visible.
     */
    public void setVisibleForName(String aName)
    {
        switch (aName) {
            case GALLERY_PANE: ViewUtils.fireActionEvent(getView("GalleryButton"), null); break;
            case VIEW_PANE: ViewUtils.fireActionEvent(getView("ViewGeneralButton"), null); break;
            case STYLE_PANE: ViewUtils.fireActionEvent(getView("ViewStyleButton"), null); break;
            default: System.err.println("InspectorPane.setVisibleForName: Unknown name: " + aName);
        }
    }

    /**
     * Initializes UI panel for the inspector.
     */
    public void initUI()
    {
        // Get/configure TitleLabel
        _titleLabel = getView("TitleLabel", Label.class);
        _titleLabel.setTextFill(Color.GRAY);

        // Get/configure ContentBox
        _inspBox = getView("ContentBox", ScrollView.class);
        _inspBox.setBorder(null);
        _inspBox.setBarSize(12);
        _inspBox.setFillWidth(true);

        // Get ViewTool
        _gallery = new GalleryPane(_epane);

        // Get ViewTool
        _viewTool = _epane._viewTool;

        // Get Styler
        _stylerPane = new StylerPane(_epane.getEditor().getStyler());
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

        // If GalleryButton is selected, install inspector
        if (getViewBoolValue("GalleryButton"))
            setInspector(_gallery);

        // If ViewGeneralButton is selected, instal inspector
        if (getViewBoolValue("ViewGeneralButton"))
            setInspector(_viewTool);

        // If ViewSpecificButton is selected, instal inspector for current selection
        //ViewTool tool = epane.getToolForView(selView);
        //if (getViewBoolValue("ViewSpecificButton")) setInspector(tool);

        // If ViewStyleButton is selected, install StylerPane
        if (getViewBoolValue("ViewStyleButton"))
            setInspector(_stylerPane);

        // Get the inspector (owner)
        ViewOwner owner = getInspector();

        // Get inspector title from owner and set
        String title = "Inspector";
        if (owner==_gallery) title = "Gallery";
        else if (owner==_viewTool) title = "View Inspector";
        else if (owner==_stylerPane) title = "Style Inspector";
        else if (owner instanceof ViewTool) title = selView.getClass().getSimpleName() + " Inspector";
        _titleLabel.setText(title);

        // If owner non-null, tell it to reset
        if (owner!=null)
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
        if (anEvent.equals("ViewGeneralButton"))
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