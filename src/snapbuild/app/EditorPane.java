package snapbuild.app;
import java.util.*;

import snap.gfx.*;
import snap.props.Undoer;
import snap.util.*;
import snap.view.*;
import snap.viewx.*;
import snap.web.WebFile;
import snap.web.WebURL;
import snapbuild.apptools.*;

/**
 * A class to manage the Editor and controls.
 */
public class EditorPane extends ViewOwner {

    // The menu bar owner
    private EditorPaneMenuBar  _menuBar;

    // The Transform Pane
    private TransitionPane  _transPane;

    // The Editor SplitView (holds Editor and ViewTree)
    private SplitView  _editorSplitView;

    // The SplitView that holds inner SplitView and Gallery
    private SplitView  _gallerySplitView;

    // The Editor
    private Editor  _editor;

    // A box to hold selection path
    private RowView  _selPathBox;

    // The deepest view to show in the SelPathBox
    private View  _selPathDeep;

    // The InspectorPane
    private InspectorPane  _inspPane;

    // The ViewTree
    private TreeView<View>  _viewTree;

    // The GalleryPane
    private GalleryPane  _gallery = new GalleryPane(this);

    // The ViewTool
    protected ViewTool<?>  _viewTool = new ViewToolImpl<>();

    // The XML TextView
    private XMLTextPane _xmlTextPane = new XMLTextPane(this);

    // Map of tools
    private Map<Class<? extends View>,ViewTool<?>>  _tools = new HashMap<>();

    /**
     * Constructor.
     */
    public EditorPane()
    {
        super();
        _viewTool._editorPane = this;

        _editor = new Editor();
        _menuBar = new EditorPaneMenuBar(this);
        _inspPane = new InspectorPane();
        _inspPane._epane = this;
    }

    /**
     * Returns the editor.
     */
    public Editor getEditor()  { return _editor; }

    /**
     * Returns the SwingOwner for the menu bar.
     */
    public EditorPaneMenuBar getMenuBar()  { return _menuBar; }

    /**
     * Returns the document source.
     */
    protected WebURL getSourceURL()  { return _editor.getSourceURL(); }

    /**
     * Sets the source URL.
     */
    public void setSourceURL(WebURL aURL)  { _editor.setSourceURL(aURL); }

    /**
     * Returns the Editor.Content.
     */
    public View getContent()  { return _editor.getContent(); }

    /**
     * Returns the inspector.
     */
    public InspectorPane getInspector()  { return _inspPane; }

    /**
     * Called when SelPath is clicked.
     */
    protected void setSelViewKeepPath(View aView)
    {
        // Get whether given view is in current path
        boolean inPath = false;
        for (View v = _selPathDeep; v != null && v != getEditor().getContentBox(); v = v.getParent())
            if (v == aView) inPath = true;

        // Set SelView and restore SelPathDeep if view was in path
        View deep = _selPathDeep;
        getEditor().setSelView(aView);
        if (inPath) _selPathDeep = deep;
    }

    /**
     * Creates a new default editor pane.
     */
    public EditorPane newDocument()
    {
        // Create ColView as root view
        ParentView newDocView = EditorUtils.createNewDocView();
        View selView = newDocView.getChildForName("FirstFocus");
        selView.setName(null);

        // Set ColView as Editor content, select row and return
        Editor editor = getEditor();
        editor.setContent(newDocView);
        editor.setSelView(selView);
        return this;
    }

    /**
     * Creates a new editor window from an open panel.
     */
    public EditorPane showOpenPanel(View aView)
    {
        // Get file from open panel for supported file extensions
        WebFile file = FilePanel.showOpenFilePanel(aView, "Snap UI File", "snp");
        if (file == null)
            return null;

        // Open file and return
        return openSource(file);
    }

    /**
     * Creates a new editor window by opening the document from the given source.
     */
    public EditorPane openSource(Object aSource)
    {
        // Get source URL
        WebURL sourceURL = WebURL.getUrl(aSource);

        // Load document (if not found, just return)
        ParentView parentView = getParentView(aSource);
        if (parentView == null)
            return null;

        // Set document
        Editor editor = getEditor();
        editor.setContent(parentView);
        editor._url = sourceURL;

        // Hack for opening new doc in SnapCode
        View selView = parentView.getChildForName("FirstFocus");
        if (selView != null) {
            selView.setName(null);
            editor.setSelView(selView);
        }

        // Return the editor
        return this;
    }

    /**
     * Creates a ParentView from given source.
     */
    protected ParentView getParentView(Object aSource)
    {
        // If document source is null, just return null
        if (aSource == null || aSource instanceof ParentView)
            return (ParentView) aSource;

        // Get archiver and clear UseRealClass
        ViewArchiver archiver = new ViewArchiver();
        ViewArchiver.setUseRealClass(false);

        // Load document
        ParentView parentView = null;
        try { parentView = (ParentView) archiver.getViewForSource(aSource); }

        // If there was an XML parse error loading aSource, show error dialog
        catch (Exception e) {
            e.printStackTrace();
            String msg = "Error reading file:\n" + e.getMessage();
            runLater(() -> {
                DialogBox dialogBox = new DialogBox("Error Reading File");
                dialogBox.setErrorMessage(msg);
                dialogBox.showMessageDialog(getUI());
            });
        }

        // Restore UseRealClass
        ViewArchiver.setUseRealClass(true);

        // Return
        return parentView;
    }

    /**
     * Saves the current editor document, running the save panel.
     */
    public void saveAs()
    {
        // Get extensions - if there is an existing extension, make sure it's first in the exts array
        String[] extensions = { "snp" };
        WebURL sourceURL = getSourceURL();
        String extension = sourceURL != null ? sourceURL.getFileType() : null;
        if (extension != null)
            extensions = ArrayUtils.add(extensions, '.' + extension, 0);

        // Run save panel, set Document.Source to path and re-save (or just return if cancelled)
        WebFile file = FilePanel.showSaveFilePanel(getEditor(), "SnapKit UI file", extensions);
        if (file == null)
            return;

        // Set SourceURL and save
        setSourceURL(file.getUrl());
        save();
    }

    /**
     * Saves the current editor document, running the save panel if needed.
     */
    public void save()
    {
        // If can't save to current source, do SaveAs instead
        WebURL sourceURL = getSourceURL();
        if (sourceURL == null) {
            saveAs();
            return;
        }

        // Do actual save - if exception, print stack trace and set error string
        try { getEditor().save(); }
        catch (Throwable e) {
            e.printStackTrace();
            String msg = "The file " + sourceURL.getPath() + " could not be saved (" + e + ").";
            DialogBox dialogBox = new DialogBox("Error on Save");
            dialogBox.setErrorMessage(msg);
            dialogBox.showMessageDialog(getUI());
            return;
        }

        // Add URL.String to RecentFilesMenu and reset UI
        //if(getSourceURL()!=null) RecentFilesPanel.addRecentFile(getSourceURL().getString());
        resetLater();
    }

    /**
     * Reloads the current editor document from the last saved version.
     */
    public void revert()
    {
        // Get filename (just return if null)
        WebURL sourceURL = getSourceURL();
        if (sourceURL == null)
            return;

        // Run option panel for revert confirmation (just return if denied)
        String msg = "Revert to saved version of " + sourceURL.getFilename() + "?";
        DialogBox dialogBox = new DialogBox("Revert to Saved");
        dialogBox.setQuestionMessage(msg);
        if (!dialogBox.showConfirmDialog(getUI()))
            return;

        // Re-open filename
        WebFile sourceFile = sourceURL.getFile();
        sourceFile.reset();
        openSource(sourceURL);
    }

    /**
     * Closes this editor pane
     */
    public boolean close()
    {
        // Close window, called EditorClosed and return true to indicate we closed the window
        getWindow().hide();
        handleEditorClosed();
        return true;
    }

    /**
     * Shows the editor.
     */
    public void showEditor()
    {
        _transPane.setTransition(TransitionPane.MoveLeft);
        _transPane.setContent(_gallerySplitView);
    }

    /**
     * Shows the XML editor.
     */
    public void showXMLEditor()
    {
        // Configure TransPane to slide in appropriate direction based on current mode
        if (_transPane.getContent() == _gallerySplitView)
            _transPane.setTransition(TransitionPane.MoveRight);
        else _transPane.setTransition(TransitionPane.MoveLeft);

        // Set TransPane.Content to XMLText.UI
        _transPane.setContent(_xmlTextPane.getUI());
        _xmlTextPane.resetXML();
    }

    /**
     * Shows preview of UI.
     */
    public void showPreview()
    {
        // Create copy of content
        View content = getContent();
        View contentCopy = new ViewArchiver().copy(content);
        contentCopy.setGrowWidth(false);
        contentCopy.setGrowHeight(false);
        if (contentCopy.getFill() == null)
            contentCopy.setFill(ViewUtils.getBackFill());
        contentCopy.setEffect(new ShadowEffect());

        // Create BoxView to hold UI
        BoxView box = new BoxView(contentCopy, false, false);
        box.setFill(Editor.BACK_FILL.brighter());

        // Add to TransPane
        _transPane.setTransition(TransitionPane.MoveRight);
        _transPane.setContent(box);
    }

    /**
     * Shows the gallery.
     */
    public void showGallery()
    {
        // If already set, just return
        if (_gallery.isShowing()) return;

        // Add item
        _gallerySplitView.addItemWithAnim(_gallery.getUI(), 220, 1);

        // Update GalleryButton.Text
        getView("GalleryButton").setText("Hide Gallery");
    }

    /**
     * Hides the gallery.
     */
    public void hideGallery()
    {
        // If already set, just return
        if (!_gallery.isShowing()) return;

        // Remove item
        _gallerySplitView.removeItemWithAnim(_gallery.getUI());

        // Update GalleryButton.Text
        getView("GalleryButton").setText("Show Gallery");
    }

    /**
     * Shows/Hides Gallery.
     */
    public void toggleShowGallery()
    {
        if (_gallery.isShowing())
            hideGallery();
        else showGallery();
    }

    /**
     * Shows the Samples drawer.
     */
    public void showSamples()
    {
        hideGallery();
        new SamplesPane().showSamples(this);
    }

    /**
     * Shows/Hides ViewTree.
     */
    protected void toggleShowViewTree()
    {
        if (_viewTree.getParent() == null)
            _editorSplitView.addItemWithAnim(_viewTree, 160, 0);
        else _editorSplitView.removeItemWithAnim(_viewTree);
    }

    /**
     * Adds a new row view.
     */
    public void addRowView()
    {
        // Create/configure RowView and get selected view
        RowView newRow = new RowView();
        ViewHpr.getHpr(newRow).configure(newRow);
        View view = _editor.getSelView();

        // Handle special cases: Empty TitleView, Empty ScrollView, TabView, SplitView
        if (ViewHpr.getHpr(view).wantsView(view, newRow)) {
            ViewHost host = (ViewHost) view;
            host.addGuest(newRow);
            _editor.setSelView(newRow);
            return;
        }

        // Get parent ColView
        ColView colView = null;
        while (colView == null) {
            if (view.getParent() instanceof ColView)
                colView = (ColView) view.getParent();
            else if (view == getContent()) return;
            else view = view.getParent();
        }

        // Add new row to ColView and select it
        colView.addChild(newRow, view.indexInParent() + 1);
        _editor.setSelView(newRow);
    }

    /**
     * Adds a new col view.
     */
    public void addColView()
    {
        // Create/configure RowView and get selected view
        ColView newCol = new ColView();
        ViewHpr.getHpr(newCol).configure(newCol);
        View view = _editor.getSelView();

        // Handle special cases: Empty TitleView, Empty ScrollView, TabView, SplitView
        if (ViewHpr.getHpr(view).wantsView(view, newCol)) {
            ViewHost host = (ViewHost) view;
            host.addGuest(newCol);
            _editor.setSelView(newCol);
            return;
        }

        // Get parent RowView
        RowView rowView = null;
        while (rowView == null) {
            if (view == getContent())
                return;
            if (view.getParent() instanceof RowView)
                rowView = (RowView) view.getParent();
            else view = view.getParent();
        }

        // Add new col to RowView and select it
        rowView.addChild(newCol, view.indexInParent() + 1);
        _editor.setSelView(newCol);
    }

    /**
     * Returns the window title.
     */
    public String getWindowTitle()
    {
        // Get window title: Basic filename + optional "Doc edited asterisk + optional "Doc Scaled"
        String title = getSourceURL() != null ? getSourceURL().getPath() : null;
        if (title == null)
            title = "Untitled";

        // If has undos, add asterisk. If zoomed, add ZoomFactor
        if (getEditor().getUndoer() != null && getEditor().getUndoer().hasUndos())
            title = "* " + title;
        return title;
    }

    /**
     * Called when the app is about to exit to gracefully handle any open documents.
     */
    public void quit()
    {
        App.quitApp();
    }

    /**
     * Returns the EditorPane for given editor.
     */
    public static EditorPane getEditorPane(Editor anEditor)
    {
        ViewOwner owner = anEditor.getOwner();
        return owner instanceof EditorPane ? (EditorPane) owner : null;
    }

    /**
     * Returns the tool for given View.
     */
    public ViewTool<?> getToolForView(View aView)
    {
        Class<? extends View> cls = aView.getClass();
        return getToolForClass(cls);
    }

    /**
     * Returns the tool for given class.
     */
    public ViewTool<?> getToolForClass(Class<? extends View> viewClass)
    {
        // Get from Tools cache, just return if found
        ViewTool<?> viewTool = _tools.get(viewClass);
        if (viewTool != null)
            return viewTool;

        // Create ViewTool and add to Tools cache
        viewTool = createToolForViewClass(viewClass);
        viewTool._editorPane = this;
        _tools.put(viewClass, viewTool);

        // Return
        return viewTool;
    }

    /**
     * Creates the tool for given class.
     */
    protected ViewTool<?> createToolForViewClass(Class<?> viewClass)
    {
        // Handle known classes
        if (viewClass == ButtonBase.class) return new ButtonBaseTool<>();
        if (viewClass == ColView.class) return new ColViewTool<>();
        if (viewClass == ImageView.class) return new ImageViewTool<>();
        if (viewClass == Label.class) return new LabelTool<>();
        if (viewClass == RowView.class) return new RowViewTool<>();
        if (viewClass == TabView.class) return new TabViewTool<>();
        if (viewClass == TextField.class) return new TextFieldTool<>();
        if (viewClass == ThumbWheel.class) return new ThumbWheelTool<>();
        if (viewClass == TitleView.class) return new TitleViewTool<>();
        if (viewClass == View.class || viewClass == null) return new ViewTool<>();

        // Try again with superclass
        Class<?> superclass = viewClass.getSuperclass();
        return createToolForViewClass(superclass);
    }

    /**
     * Create UI.
     */
    protected View createUI()
    {
        // Do normal version
        View mainView = super.createUI();

        // Create ColView holding MenuBar and EditorPane UI (with key listener so MenuBar catches shortcut keys)
        MenuBar menuBar = getMenuBar().getUI();
        return MenuBar.createMenuBarView(menuBar, mainView);
    }

    /**
     * Initialize UI.
     */
    protected void initUI()
    {
        // Get GallerySplitView (holds SplitView and Gallery)
        _gallerySplitView = getView("GallerySplitView", SplitView.class);
        getView("GalleryButton").setVisible(false);

        // Get TransPane and add GallerySplitView
        _transPane = getView("TransPane", TransitionPane.class);
        _transPane.setContent(_gallerySplitView);

        // Get editor
        setFirstFocus(_editor);
        _editor.addPropChangeListener(pc -> handleEditorSelViewChange(), Editor.SelView_Prop);

        // Add to EditorScrollView
        ScrollView scrollView = getView("EditorScrollView", ScrollView.class);
        scrollView.setContent(_editor);

        // Get Editor SplitView
        _editorSplitView = getView("SplitView", SplitView.class);
        _editorSplitView.setBorder(null);

        // Get/configure ViewTree
        _viewTree = getView("ViewTree", TreeView.class);
        _viewTree.getCol(0).setAltRowColor(Editor.BACK_FILL.blend(Color.WHITE, .9));
        _viewTree.setResolver(new ViewTreeResolver());
        _viewTree.setOwner(this);
        _editorSplitView.removeItem(_viewTree);

        // Get EditorRowView and add Inspector UI
        RowView editorRowView = getView("EditorRowView", RowView.class);
        editorRowView.addChild(getInspector().getUI());

        // Get SelPathBox
        _selPathBox = getView("SelPathBox", RowView.class);
        updateSelPathBox();

        // Add action for ESCAPE key to pop selection
        addKeyActionHandler("EscapeAction", "ESCAPE");

        // Set Toolbar images
        getView("SaveButton", ButtonBase.class).setImage(Image.getImageForClassResource(TextPane.class, "pkg.images/File_Save.png"));
        getView("CutButton", ButtonBase.class).setImage(Image.getImageForClassResource(TextPane.class, "pkg.images/Edit_Cut.png"));
        getView("CopyButton", ButtonBase.class).setImage(Image.getImageForClassResource(TextPane.class, "pkg.images/Edit_Copy.png"));
        getView("PasteButton", ButtonBase.class).setImage(Image.getImageForClassResource(TextPane.class, "pkg.images/Edit_Paste.png"));
        getView("DeleteButton", ButtonBase.class).setImage(Image.getImageForClassResource(TextPane.class, "pkg.images/Edit_Delete.png"));
        getView("UndoButton", ButtonBase.class).setImage(Image.getImageForClassResource(TextPane.class, "pkg.images/Edit_Undo.png"));
        getView("RedoButton", ButtonBase.class).setImage(Image.getImageForClassResource(TextPane.class, "pkg.images/Edit_Redo.png"));
    }

    /**
     * Initialize window.
     */
    @Override
    protected void initWindow(WindowView aWindow)
    {
        aWindow.addEventHandler(e -> { close(); e.consume(); }, WinClose);

        // If WebVM, go full window
        if (SnapEnv.isWebVM)
            aWindow.setMaximized(true);
    }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Update UndoButton, RedoButton
        Undoer undoer = _editor.getUndoer();
        setViewEnabled("UndoButton", undoer != null && undoer.hasUndos());
        setViewEnabled("RedoButton", undoer != null && undoer.hasRedos());

        // Update SelPathBox
        updateSelPathBox();

        // Update ViewTree
        if (_viewTree.isShowing()) {
            _viewTree.setItems(ListUtils.of(getContent()));
            _viewTree.setSelItem(null);
            _viewTree.collapseItem(getContent());
            _viewTree.expandItem(getContent());
            for (View v = _editor.getSelView(); v != getContent(); v = v.getParent())
                _viewTree.expandItem(v);
            _viewTree.setSelItem(_editor.getSelView());
        }

        // If title has changed, update window title
        if (isWindowSet()) {
            WindowView window = getWindow();
            String title = getWindowTitle();
            window.setTitle(title);
            window.setDocURL(getSourceURL());
        }

        // Reset Inspector, MenuBar
        getInspector().resetLater();
        if (!ViewUtils.isMouseDown())
            getMenuBar().resetLater();
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        String eventName = anEvent.getName();

        switch (eventName) {

            // Handle Edit CutButton, CopyButton, PasteButton, DeleteButton
            case "CutButton": _editor.cut(); break;
            case "CopyButton": _editor.copy(); break;
            case "PasteButton": _editor.paste(); break;
            case "DeleteButton": _editor.delete(); break;

            // Handle Edit UndoButton, RedoButton
            case "UndoButton": _editor.undo(); break;
            case "RedoButton": _editor.redo(); break;

            // Handle SamplesButton
            case "SamplesButton": showSamples(); break;

            // Handle EditButton, XMLButton, PreviewButton
            case "EditButton": showEditor(); break;
            case "XMLButton": showXMLEditor(); break;
            case "PreviewButton": showPreview(); break;

            // Handle ShowViewTreeButton
            case "ShowViewTreeButton": toggleShowViewTree(); break;

            // Handle GalleryButton
            case "GalleryButton": toggleShowGallery(); break;

            // Handle AddRowButton, AddColButton
            case "AddRowButton": addRowView(); break;
            case "AddColButton": addColView(); break;

            // Handle ViewTree
            case "ViewTree":
                View view = _viewTree.getSelItem();
                _editor.setSelView(view);
                break;

            // Handle EscapeAction
            case "EscapeAction":
                View selView = _editor.getSelView();
                View selViewParent = selView.getParent();
                if (selView != getContent())
                    _editor.setSelView(selViewParent);
                else {
                    _editor.getCopyPaster().copy(); // This is for me - quick top level copy
                    beep();
                }
                break;

            // Handle SaveMenuItem, SaveButton, SaveAsMenuItem, SaveAsPDFMenuItem, RevertMenuItem
            case "SaveMenuItem": case "SaveButton": save(); break;
            case "SaveAsMenuItem": saveAs(); break;
            case "RevertMenuItem": revert(); break;
        }
    }

    /**
     * Called to update SelPathBox.
     */
    private void updateSelPathBox()
    {
        _selPathBox.removeChildren();
        if (_selPathDeep == null)
            _selPathDeep = _editor.getSelView();

        View selView = _editor.getSelView();
        View contentView = getContent();

        // Iterate from SelPathDeepView to parent
        for (View view = _selPathDeep; view != null; view = view.getParent()) {
            View view2 = view;
            Label label = new Label(view.getClass().getSimpleName());
            label.setPadding(2, 2, 2, 2);
            if (view == selView)
                label.setFill(Color.LIGHTGRAY);
            label.addEventHandler(e -> handleSelPathItemMouseRelease(view2), MouseRelease);
            _selPathBox.addChild(label, 0);
            if (_selPathBox.getChildCount() > 1)
                _selPathBox.addChild(new Label(" \u2022 "), 1);
            if (view == contentView)
                break;
        }
    }

    /**
     * Called when Editor.SelView changes.
     */
    private void handleEditorSelViewChange()
    {
        _selPathDeep = _editor.getSelView();
        resetLater();
        _xmlTextPane.handleEditorSelViewChange();
    }

    /**
     * Called when SelPath is clicked.
     */
    private void handleSelPathItemMouseRelease(View aView)
    {
        View deep = _selPathDeep;
        getEditor().setSelView(aView);
        _selPathDeep = deep;
    }

    /**
     * Called when editor is closed.
     */
    protected void handleEditorClosed()
    {
        // If another open editor is available focus on it, otherwise run WelcomePanel
        EditorPane editorPane = WindowView.getOpenWindowOwner(EditorPane.class);
        if (editorPane != null)
            editorPane.getEditor().requestFocus();
        else WelcomePanel.getShared().showPanel();
    }

    /**
     * A resolver for Views.
     */
    public class ViewTreeResolver extends TreeResolver<View> {

        /**
         * Returns the parent of given item.
         */
        public View getParent(View anItem)
        {
            return anItem != getContent() ? anItem.getParent() : null;
        }

        /**
         * Whether given object is a parent (has children).
         */
        public boolean isParent(View anItem)
        {
            if (!(anItem instanceof ParentView))
                return false;
            if (anItem instanceof Label || anItem instanceof ButtonBase || anItem instanceof Spinner ||
                    anItem instanceof ArrowView || anItem instanceof TextField) return false;
            if (anItem instanceof ComboBox || anItem instanceof ListView) return false;
            return ((ParentView) anItem).getChildCount() > 0;
        }

        /**
         * Returns the children.
         */
        public List<View> getChildren(View aParent)
        {
            ParentView parentView = (ParentView) aParent;
            if (parentView instanceof ScrollView) {
                ScrollView scrollView = (ScrollView) parentView;
                return scrollView.getContent() != null ? Collections.singletonList(scrollView.getContent()) : Collections.emptyList();
            }
            return parentView.getChildren();
        }

        /**
         * Returns the text to be used for given item.
         */
        public String getText(View anItem)
        {
            String str = anItem.getClass().getSimpleName();
            //String name = anItem.getName(); if(name!=null) str += " - " + name;
            //String text = anItem.getText(); if(text!=null) str += " \"" + text + "\" ";
            return str;
        }

        /**
         * Return the image to be used for given item.
         */
        public View getGraphic(View anItem)
        {
            return null;
        }
    }
}