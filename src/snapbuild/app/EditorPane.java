package snapbuild.app;
import java.util.*;
import snap.gfx.*;
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
    EditorPaneMenuBar _menuBar;

    // The Transform Pane
    TransitionPane   _transPane;
    
    // The Editor SplitView (holds Editor and ViewTree)
    SplitView        _editorSplit;
    
    // The SplitView that holds inner SplitView and Gallery
    SplitView        _gallerySplit;
    
    // The Editor
    Editor           _editor, _realEditor;
    
    // A box to hold selection path
    RowView          _selPathBox;
    
    // The deepest view to show in the SelPathBox
    View             _selPathDeep;
    
    // The InspectorPane
    InspectorPane    _inspPane;
    
    // The ViewTree
    TreeView <View>  _viewTree;
    
    // The GalleryPane
    GalleryPane      _gallery = new GalleryPane(this);
    
    // The ViewTool
    ViewTool         _viewTool = new ViewToolImpl();

    // The XML TextView
    XMLText          _xmlText = new XMLText(this);
    
    // Map of tools
    Map <Class,ViewTool>  _tools = new HashMap();

    // The Editor listener
    PropChangeListener _editorLsnr = pce -> editorSelViewChange();

    /**
     * Creates a new EditorPane.
     */
    public EditorPane()
    {
        //getUI();
        _viewTool._epane = this;
    }

    /**
     * Returns the editor.
     */
    public Editor getEditor()
    {
        if (_editor==null) getUI();
        return _editor;
    }

    /**
     * Sets the viewer for this viewer pane.
     */
    protected void setEditor(Editor anEditor)
    {
        // Stop listening to PropChanges on old
        if(_editor!=null) _editor.removePropChangeListener(_editorLsnr);

        // Set Viewer
        _editor = anEditor;
        ScrollView scroll = getView("EditorScrollView", ScrollView.class);
        scroll.setContent(_editor);

        // Start listening to PropChanges
        _editor.addPropChangeListener(_editorLsnr);
    }

    /**
     * Creates the editor.
     */
    protected Editor createEditor()
    {
        return new Editor();
    }

    /**
     * Returns the SwingOwner for the menu bar.
     */
    public EditorPaneMenuBar getMenuBar()
    {
        if (_menuBar!=null) return _menuBar;
        return _menuBar = new EditorPaneMenuBar(this);
    }

    /**
     * Returns the document source.
     */
    protected WebURL getSourceURL()  { return getEditor().getSourceURL(); }

    /**
     * Sets the source URL.
     */
    public void setSourceURL(WebURL aURL)  { getEditor().setSourceURL(aURL); }

    /**
     * Returns the Editor.Content.
     */
    public View getContent()  { return _editor.getContent(); }

    /**
     * Returns the inspector.
     */
    public InspectorPane getInspector()
    {
        if (_inspPane!=null) return _inspPane;
        _inspPane = new InspectorPane();
        _inspPane._epane = this;
        return _inspPane;
    }

    /**
     * Called when SelPath is clicked.
     */
    protected void setSelViewKeepPath(View aView)
    {
        // Get whether given view is in current path
        boolean inPath = false;
        for (View v=_selPathDeep;v!=null && v!=getEditor().getContentBox();v=v.getParent())
            if (v==aView) inPath = true;

        // Set SelView and restore SelPathDeep if view was in path
        View deep = _selPathDeep;
        getEditor().setSelView(aView);
        if(inPath) _selPathDeep = deep;
    }

    /**
     * Creates a new default editor pane.
     */
    public EditorPane newDocument()
    {
        RowView row = new RowView();
        row.setPadding(4,4,4,4);
        row.setSpacing(4);
        row.setGrowWidth(true);
        ColView col = new ColView();
        col.setPadding(4,4,4,4);
        col.setSpacing(4);
        col.addChild(row);
        Editor editor = getEditor();
        editor.setContent(col);
        editor.setSelView(row);
        //BoxView box0 = new BoxView(); box0.setPadding(4,4,4,4); box0.setSpacing(4); box0.setVertical(true);
        //BoxView box1 = new BoxView(); box1.setPadding(4,4,4,4); box1.setSpacing(4); box1.setGrowWidth(true);
        //box0.addGuest(box1); getEditor().setContent(box0); getEditor().setSelView(box1);
        return this;
    }

    /**
     * Creates a new editor window from an open panel.
     */
    public EditorPane showOpenPanel(View aView)
    {
        // Get path from open panel for supported file extensions
        String path = FilePanel.showOpenPanel(aView, "Snap UI File", "snp");
        return open(path);
    }

    /**
     * Creates a new editor window by opening the document from the given source.
     */
    public EditorPane open(Object aSource)
    {
        // If source is already opened, return editor pane
        WebURL url = WebURL.getURL(aSource);
        //if(!SnapUtils.equals(url, getSourceURL())) {
        //    EditorPane epanes[] = WindowView.getOpenWindowOwners(EditorPane.class);
        //    for(EditorPane epane : epanes) if(SnapUtils.equals(url, epane.getSourceURL())) return epane; }

        // Load document (if not found, just return)
        ParentView view = getParentView(aSource); if(view==null) return null;

        // Set document
        getEditor().setContent(view);
        getEditor()._url = url;

        // If source is string, add to recent files menu
        //if(url!=null) RecentFilesPanel.addRecentFile(url.getString());

        // Return the editor
        return this;
    }

    /**
     * Creates a ParentView from given source.
     */
    protected ParentView getParentView(Object aSource)
    {
        // If document source is null, just return null
        if(aSource==null || aSource instanceof ParentView) return (ParentView)aSource;

        // Load document
        ViewArchiver archiver = new ViewArchiver(); ViewArchiver.setUseRealClass(false);
        ParentView view = null;
        try { view = archiver.getParentView(aSource); }

        // If there was an XML parse error loading aSource, show error dialog
        catch (Exception e) {
            e.printStackTrace();
            String msg = StringUtils.wrap("Error reading file:\n" + e.getMessage(), 40);
            runLater(() -> {
                DialogBox dbox = new DialogBox("Error Reading File"); dbox.setErrorMessage(msg);
                dbox.showMessageDialog(getUI()); });
        }
        ViewArchiver.setUseRealClass(true);
        return view;
    }

    /**
     * Saves the current editor document, running the save panel.
     */
    public void saveAs()
    {
        // Make sure editor isn't previewing
        //setEditing(true);

        // Get extensions - if there is an existing extension, make sure it's first in the exts array
        String exts[] = { "snp" };
        if (getSourceURL()!=null && FilePathUtils.getExtension(getSourceURL().getPath())!=null) {
            List ex = new ArrayList(Arrays.asList(exts));
            ex.add(0, "." + FilePathUtils.getExtension(getSourceURL().getPath()));
            exts = (String[])ex.toArray(new String[ex.size()]);
        }

        // Run save panel, set Document.Source to path and re-save (or just return if cancelled)
        WebFile file = FilePanel.showSavePanelWeb(getEditor(), "SnapKit UI file", exts); if (file==null) return;
        setSourceURL(file.getURL());
        save();
    }

    /**
     * Saves the current editor document, running the save panel if needed.
     */
    public void save()
    {
        // If can't save to current source, do SaveAs instead
        if (getSourceURL()==null) { saveAs(); return; }

        // Make sure editor isn't previewing
        //setEditing(true);

        // Do actual save - if exception, print stack trace and set error string
        try { getEditor().save(); }
        catch (Throwable e) {
            e.printStackTrace();
            String msg = "The file " + getSourceURL().getPath() + " could not be saved (" + e + ").";
            DialogBox dbox = new DialogBox("Error on Save"); dbox.setErrorMessage(msg);
            dbox.showMessageDialog(getUI());
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
        WebURL surl = getSourceURL(); if (surl==null) return;

        // Run option panel for revert confirmation (just return if denied)
        String msg = "Revert to saved version of " + surl.getPathName() + "?";
        DialogBox dbox = new DialogBox("Revert to Saved"); dbox.setQuestionMessage(msg);
        if (!dbox.showConfirmDialog(getUI())) return;

        // Re-open filename
        getSourceURL().getFile().reload();
        open(getSourceURL());
    }

    /**
     * Closes this editor pane
     */
    public boolean close()
    {
        // Make sure editor isn't previewing
        //setEditing(true);

        // If unsaved changes, run panel to request save
        /*if (getEditor().undoerHasUndos()) {
            String filename = getSourceURL()==null? "untitled document" : getSourceURL().getPathName();
            DialogBox dbox = new DialogBox("Unsaved Changes");
            dbox.setWarningMessage("Save changes to " + filename + "?"); dbox.setOptions("Save", "Don't Save", "Cancel");
            switch(dbox.showOptionDialog(getUI(), "Save")) {
                case 0: save();
                case 1: break;
                default: return false;
            }
        }*/

        // Deactive current tool, so it doesn't reference this editor
        //getEditor().getCurrentTool().deactivateTool();

        // Close window, called EditorClosed and return true to indicate we closed the window
        getWindow().hide();
        editorClosed();
        return true;
    }

    /**
     * Called when editor is closed.
     */
    protected void editorClosed()
    {
        // If another open editor is available focus on it, otherwise run WelcomePanel
        EditorPane epane = WindowView.getOpenWindowOwner(EditorPane.class);
        if (epane!=null)
            epane.getEditor().requestFocus();
        else WelcomePanel.getShared().showPanel();
    }

    /**
     * Returns whether editor is really doing editing.
     */
    public boolean isEditing()  { return _realEditor==null; } //getEditor().isEditing(); }

    /**
     * Sets whether editor is really doing editing.
     */
    public void setEditing(boolean aValue)
    {
        // If already set, just return
        if (aValue==isEditing()) return;

        // If not yet previewing, store current template then generate report and swap it in
        if (!aValue) {

            // Cache current editor and flush any current editing
            _realEditor = getEditor(); //_realEditor.flushEditingChanges();

            // Reload content
            View content = new ViewArchiver().copy(getContent());

            // Create new editor, set editing to false and set report document
            Editor editor = new Editor();
            editor.setEditing(false);
            editor.setContent(content);
            editor.setSize(_realEditor.getSize());

            // Set new editor
            setEditor(editor);
        }

        // If turning preview off, restore real editor
        else {
            setEditor(_realEditor);
            _realEditor = null;
        }

        // Focus on editor
        requestFocus(getEditor());
        resetLater();
    }

    /**
     * Returns the tool for given View.
     */
    public ViewTool getToolForView(View aView)
    {
        Class cls = aView.getClass();
        return getToolForClass(cls);
    }

    /**
     * Returns the tool for given class.
     */
    public ViewTool getToolForClass(Class aClass)
    {
        ViewTool tool = _tools.get(aClass);
        if (tool==null) {
            tool = createTool(aClass);  tool._epane = this;
            _tools.put(aClass, tool);
        }
        return tool;
    }

    /**
     * Creates the tool for given class.
     */
    protected ViewTool createTool(Class aClass)
    {
        // Handle known classes
        if (aClass==ButtonBase.class) return new ButtonBaseTool();
        if (aClass==ColView.class) return new ColViewTool();
        if (aClass==Label.class) return new LabelTool();
        if (aClass==RowView.class) return new RowViewTool();
        if (aClass==TabView.class) return new TabViewTool();
        if (aClass==TextField.class) return new TextFieldTool();
        if (aClass==TitleView.class) return new TitleViewTool();
        if (aClass==View.class || aClass==null) return new ViewTool();

        // Try again with superclass
        Class scls = aClass.getSuperclass();
        return createTool(scls);
    }

    /**
     * Create UI.
     */
    protected View createUI()
    {
        // Do normal version
        View mainView = super.createUI();

        // Create ColView holding MenuBar and EditorPane UI (with key listener so MenuBar catches shortcut keys)
        View mbarView = MenuBar.createMenuBarView(getMenuBar().getUI(), mainView);
        return mbarView;
    }

    /**
     * Initialize UI.
     */
    protected void initUI()
    {
        // Get GallerySplitView (holds SplitView and Gallery)
        _gallerySplit = getView("GallerySplitView", SplitView.class);
        getView("GalleryButton").setVisible(false);

        // Get TransPane and add GallerySplitView
        _transPane = getView("TransPane", TransitionPane.class);
        _transPane.setContent(_gallerySplit);

        // Get editor
        _editor = createEditor();
        _editor.addPropChangeListener(_editorLsnr, Editor.SelView_Prop);

        // Add to EditorScrollView
        ScrollView scrollView = getView("EditorScrollView", ScrollView.class);
        scrollView.setContent(_editor);

        // Get Editor SplitView
        _editorSplit = getView("SplitView", SplitView.class);
        _editorSplit.setBorder(null);

        // Get/configure ViewTree
        _viewTree = getView("ViewTree", TreeView.class);
        _viewTree.getCol(0).setAltPaint(Editor.BACK_FILL.blend(Color.WHITE, .9));
        _viewTree.setResolver(new ViewTreeResolver());
        _viewTree.setOwner(this);
        _editorSplit.removeItem(_viewTree);

        // Get EditorRowView and add Inspector UI
        RowView editorRowView = getView("EditorRowView", RowView.class);
        editorRowView.addChild(getInspector().getUI());

        // Get SelPathBox
        _selPathBox = getView("SelPathBox", RowView.class);
        updateSelPathBox();

        // Add action for ESCAPE key to pop selection
        addKeyActionHandler("EscapeAction", "ESCAPE");

        // Set Toolbar images
        getView("SaveButton", ButtonBase.class).setImage(Image.get(TextPane.class, "pkg.images/File_Save.png"));
        getView("CutButton", ButtonBase.class).setImage(Image.get(TextPane.class, "pkg.images/Edit_Cut.png"));
        getView("CopyButton", ButtonBase.class).setImage(Image.get(TextPane.class, "pkg.images/Edit_Copy.png"));
        getView("PasteButton", ButtonBase.class).setImage(Image.get(TextPane.class, "pkg.images/Edit_Paste.png"));
        getView("DeleteButton", ButtonBase.class).setImage(Image.get(TextPane.class, "pkg.images/Edit_Delete.png"));
        getView("UndoButton", ButtonBase.class).setImage(Image.get(TextPane.class, "pkg.images/Edit_Undo.png"));
        getView("RedoButton", ButtonBase.class).setImage(Image.get(TextPane.class, "pkg.images/Edit_Redo.png"));

        // Configure window
        WindowView win = getWindow();
        enableEvents(win, WinClose);

        // If TeaVM, go full window
        if (SnapUtils.isTeaVM)
            getWindow().setMaximized(true);
    }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Update UndoButton, RedoButton
        Undoer undoer = _editor.getUndoer();
        setViewEnabled("UndoButton", undoer!=null && undoer.hasUndos()); //undoer.getUndoSetLast()!=null
        setViewEnabled("RedoButton", undoer!=null && undoer.hasRedos()); //undoer.getRedoSetLast()!=null

        // Update SelPathBox
        updateSelPathBox();

        // Update ViewTree
        if (_viewTree.isShowing()) {
            _viewTree.setItems(getContent());
            _viewTree.setSelItem(null);
            _viewTree.collapseItem(getContent());
            _viewTree.expandItem(getContent());
            for(View v=_editor.getSelView();v!=getContent();v=v.getParent())
                _viewTree.expandItem(v);
            _viewTree.setSelItem(_editor.getSelView());
        }

        // If title has changed, update window title
        String title = getWindowTitle();
        getWindow().setTitle(title);
        getWindow().setDocURL(getSourceURL());

        // Reset Inspector, MenuBar
        getInspector().resetLater();
        if (!ViewUtils.isMouseDown()) getMenuBar().resetLater();
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle Edit CutButton, CopyButton, PasteButton, DeleteButton
        if (anEvent.equals("CutButton")) _editor.cut();
        if (anEvent.equals("CopyButton")) _editor.copy();
        if (anEvent.equals("PasteButton")) _editor.paste();
        if (anEvent.equals("DeleteButton")) _editor.delete();

        // Handle Edit UndoButton, RedoButton
        if (anEvent.equals("UndoButton")) _editor.undo();
        if (anEvent.equals("RedoButton")) _editor.redo();

        // Handle SamplesButton
        if (anEvent.equals("SamplesButton")) showSamples();

        // Handle EditButton, XMLButton, PreviewButton
        if (anEvent.equals("EditButton")) showEditor();
        if (anEvent.equals("XMLButton")) showXMLEditor();
        if (anEvent.equals("PreviewButton")) showPreview();

        // Handle ShowViewTreeButton
        if (anEvent.equals("ShowViewTreeButton"))
            toggleShowViewTree();

        // Handle GalleryButton
        if (anEvent.equals("GalleryButton")) toggleShowGallery();

        // Handle AddRowButton, AddColButton
        if (anEvent.equals("AddRowButton")) addRowView();
        if (anEvent.equals("AddColButton")) addColView();

        // Handle ViewTree
        if (anEvent.equals(_viewTree)) {
            View view = _viewTree.getSelItem();
            _editor.setSelView(view);
        }

        // Handle EscapeAction
        if (anEvent.equals("EscapeAction")) {
            View sview = _editor.getSelView();
            View par = sview.getParent();
            if(sview!=getContent())
                _editor.setSelView(par);
            else beep();
        }

        // Handle SaveMenuItem, SaveButton, SaveAsMenuItem, SaveAsPDFMenuItem, RevertMenuItem
        if (anEvent.equals("SaveMenuItem") || anEvent.equals("SaveButton")) save();
        if (anEvent.equals("SaveAsMenuItem")) saveAs();
        if (anEvent.equals("RevertMenuItem")) revert();

        // Handle WinClosing
        if (anEvent.isWinClose()) {
            close(); anEvent.consume(); }
    }

    /**
     * Shows the editor.
     */
    public void showEditor()
    {
        _transPane.setTransition(TransitionPane.MoveLeft);
        _transPane.setContent(_gallerySplit);
    }

    /**
     * Shows the XML editor.
     */
    public void showXMLEditor()
    {
        // Configure TransPane to slide in appropriate direction based on current mode
        if(_transPane.getContent()==_gallerySplit) _transPane.setTransition(TransitionPane.MoveRight);
        else _transPane.setTransition(TransitionPane.MoveLeft);

        // Set TransPane.Content to XMLText.UI
        _transPane.setContent(_xmlText.getUI());
        _xmlText.updateXMLText();
    }

    /**
     * Shows preview of UI.
     */
    public void showPreview()
    {
        // Create copy of content
        View content = new ViewArchiver().copy(getContent());
        content.setGrowWidth(false);
        content.setGrowHeight(false);
        if(content.getFill()==null)
            content.setFill(ViewUtils.getBackFill());
        content.setEffect(new ShadowEffect());

        // Create BoxView to hold UI
        BoxView box = new BoxView(content, false, false);
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
        if(_gallery.isShowing()) return;

        // Add item
        _gallerySplit.addItemWithAnim(_gallery.getUI(), 220, 1);

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
        _gallerySplit.removeItemWithAnim(_gallery.getUI());

        // Update GalleryButton.Text
        getView("GalleryButton").setText("Show Gallery");
    }

    /**
     * Shows/Hides Gallery.
     */
    public void toggleShowGallery()
    {
        if (_gallery.isShowing()) hideGallery();
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
        if (_viewTree.getParent()==null)
            _editorSplit.addItemWithAnim(_viewTree, 160, 0);
        else _editorSplit.removeItemWithAnim(_viewTree);
    }

    /**
     * Called to update SelPathBox.
     */
    protected void updateSelPathBox()
    {
        _selPathBox.removeChildren(); if (_selPathDeep==null) _selPathDeep = _editor.getSelView();
        View sview = _editor.getSelView();
        View cview = getContent();
        View view = _selPathDeep;
        while (view!=null) { View view2 = view;
            Label label = new Label(view.getClass().getSimpleName());
            label.setPadding(2,2,2,2);
            if (view==sview) label.setFill(Color.LIGHTGRAY);
            label.addEventHandler(e -> selPathItemClicked(view2), MouseRelease);
            _selPathBox.addChild(label, 0);
            if (_selPathBox.getChildCount()>1)
                _selPathBox.addChild(new Label(" \u2022 "),1);
            if (view==cview) break;
            view = view.getParent();
        }
    }

    /**
     * Called when Editor.SelView changes.
     */
    protected void editorSelViewChange()
    {
        _selPathDeep = _editor.getSelView();
        resetLater();
        _xmlText.updateXMLTextSel();
    }

    /**
     * Called when SelPath is clicked.
     */
    protected void selPathItemClicked(View aView)
    {
        View deep = _selPathDeep;
        getEditor().setSelView(aView);
        _selPathDeep = deep;
    }

    /**
     * Override to show gallery.
     */
    protected void showingChanged()
    {
        super.showingChanged();
        //if (isShowing()) runLater(() -> showGallery());
    }

    /**
     * Adds a new row view.
     */
    public void addRowView()
    {
        // Create/configure RowView and get selected view
        RowView newRow = new RowView(); ViewHpr.getHpr(newRow).configure(newRow);
        View view = _editor.getSelView();

        // Handle special cases: Empty TitleView, Empty ScrollView, TabView, SplitView
        if (ViewHpr.getHpr(view).wantsView(view, newRow)) {
            ViewHost host = (ViewHost)view;
            host.addGuest(newRow);
            _editor.setSelView(newRow);
            return;
        }

        // Get parent ColView
        ColView colView = null;
        while (colView==null) {
            if(view.getParent() instanceof ColView) colView = (ColView)view.getParent();
            else if(view==getContent()) return;
            else view = view.getParent();
        }

        // Add new row to ColView and select it
        colView.addChild(newRow, view!=null? view.indexInParent()+1 : colView.getChildCount());
        _editor.setSelView(newRow);
    }

    /**
     * Adds a new col view.
     */
    public void addColView()
    {
        // Create/configure RowView and get selected view
        ColView newCol = new ColView(); ViewHpr.getHpr(newCol).configure(newCol);
        View view = _editor.getSelView();

        // Handle special cases: Empty TitleView, Empty ScrollView, TabView, SplitView
        if (ViewHpr.getHpr(view).wantsView(view, newCol)) {
            ViewHost host = (ViewHost)view;
            host.addGuest(newCol);
            _editor.setSelView(newCol);
            return;
        }

        // Get parent RowView
        RowView rowView = null;
        while (rowView==null) {
            if(view==getContent()) return;
            if(view.getParent() instanceof RowView) rowView = (RowView)view.getParent();
            else view = view.getParent();
        }

        // Add new col to RowView and select it
        rowView.addChild(newCol, view!=null? view.indexInParent()+1 : rowView.getChildCount());
        _editor.setSelView(newCol);
    }

    /**
     * Returns the window title.
     */
    public String getWindowTitle()
    {
        // Get window title: Basic filename + optional "Doc edited asterisk + optional "Doc Scaled"
        String title = getSourceURL()!=null ? getSourceURL().getPath() : null; if(title==null) title = "Untitled";

        // If has undos, add asterisk. If zoomed, add ZoomFactor
        if (!isEditing()) title += "(preview)";
        else if (getEditor().getUndoer()!=null && getEditor().getUndoer().hasUndos()) title = "* " + title;
        return title;
    }

    /**
     * Called when the app is about to exit to gracefully handle any open documents.
     */
    public void quit()  { App.quitApp(); }

    /**
     * A resolver for Views.
     */
    public class ViewTreeResolver extends TreeResolver <View> {

        /** Returns the parent of given item. */
        public View getParent(View anItem)  { return anItem!=getContent()? anItem.getParent() : null; }

        /** Whether given object is a parent (has children). */
        public boolean isParent(View anItem)
        {
            if(!(anItem instanceof ParentView)) return false;
            if(anItem instanceof Label || anItem instanceof ButtonBase || anItem instanceof Spinner ||
                anItem instanceof ArrowView || anItem instanceof TextField) return false;
            if(anItem instanceof ComboBox || anItem instanceof ListView) return false;
            return ((ParentView)anItem).getChildCount()>0;
        }

        /** Returns the children. */
        public View[] getChildren(View aParent)
        {
            ParentView par = (ParentView)aParent;
            if(par instanceof ScrollView) { ScrollView sp = (ScrollView)par;
                return sp.getContent()!=null? new View[] { sp.getContent() } : new View[0]; }
            return par.getChildren();
        }

        /** Returns the text to be used for given item. */
        public String getText(View anItem)
        {
            String str = anItem.getClass().getSimpleName();
            //String name = anItem.getName(); if(name!=null) str += " - " + name;
            //String text = anItem.getText(); if(text!=null) str += " \"" + text + "\" ";
            return str;
        }

        /** Return the image to be used for given item. */
        public View getGraphic(View anItem)  { return null; }
    }
}