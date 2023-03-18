/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapbuild.app;
import snap.gfx.Border;
import snap.text.TextEditor;
import snap.util.ClassUtils;
import snap.util.Prefs;
import snap.util.URLUtils;
import snap.props.Undoer;
import snap.view.MenuBar;
import snap.view.ViewEvent;
import snap.view.ViewOwner;
import snap.view.ViewTheme;
import snap.web.RecentFiles;

/**
 * Menu bar for Editor pane.
 */
public class EditorPaneMenuBar extends ViewOwner {

    // The EditorPane
    private EditorPane _epane;

    /**
     * Creates a new editor pane menu bar.
     */
    public EditorPaneMenuBar(EditorPane anEP)
    {
        _epane = anEP;
    }

    /**
     * Returns the EditorPane.
     */
    public EditorPane getEditorPane()
    {
        return _epane;
    }

    /**
     * Returns the editor.
     */
    public Editor getEditor()
    {
        return _epane.getEditor();
    }

    /**
     * Override to return node as MenuBar.
     */
    public MenuBar getUI()
    {
        return (MenuBar) super.getUI();
    }

    /**
     * Initialize UI panel.
     */
    protected void initUI()
    {
    }

    /**
     * Updates the editor's UI.
     */
    protected void resetUI()
    {
        // Get the editor undoer
        Undoer undoer = getEditor().getUndoer();

        // Update UndoMenuItem
        String uTitle = undoer == null || undoer.getUndoSetLast() == null ? "Undo" : undoer.getUndoSetLast().getFullUndoTitle();
        setViewValue("UndoMenuItem", uTitle);
        setViewEnabled("UndoMenuItem", undoer != null && undoer.getUndoSetLast() != null);

        // Update RedoMenuItem
        String rTitle = undoer == null || undoer.getRedoSetLast() == null ? "Redo" : undoer.getRedoSetLast().getFullRedoTitle();
        setViewValue("RedoMenuItem", rTitle);
        setViewEnabled("RedoMenuItem", undoer != null && undoer.getRedoSetLast() != null);
    }

    /**
     * Handles changes to the editor's UI controls.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get editor pane
        EditorPane epane = getEditorPane();
        Editor editor = getEditor();

        // Handle NewMenuItem, NewButton: Get new editor pane and make visible
        if (anEvent.equals("NewMenuItem") || anEvent.equals("NewButton")) {
            EditorPane editorPane = ClassUtils.newInstance(epane).newDocument();
            editorPane.setWindowVisible(true);
        }

        // Handle OpenMenuItem, OpenButton: Get new editor pane from open panel and make visible (if created)
        if (anEvent.equals("OpenMenuItem") || anEvent.equals("OpenButton")) {
            EditorPane editorPane = ClassUtils.newInstance(epane).open(epane.getUI());
            if (editorPane != null)
                editorPane.setWindowVisible(true);
        }

        // Handle OpenRecentMenuItem
        if (anEvent.equals("OpenRecentMenuItem")) {
            String path = RecentFiles.showPathsPanel(epane.getUI());
            if (path == null) return;
            WelcomePanel.getShared().openFile(path); //file.getAbsolutePath());
        }

        // Handle CloseMenuItem
        if (anEvent.equals("CloseMenuItem")) epane.close();

        // Handle SaveMenuItem, SaveButton, SaveAsMenuItem, RevertMenuItem
        if (anEvent.equals("SaveMenuItem") || anEvent.equals("SaveButton"))
            epane.save();
        if (anEvent.equals("SaveAsMenuItem"))
            epane.saveAs();
        if (anEvent.equals("RevertMenuItem"))
            epane.revert();

        // Handle QuitMenuItem
        if (anEvent.equals("QuitMenuItem"))
            epane.quit();

        // Handle Edit menu items
        if (anEvent.equals("UndoMenuItem") || anEvent.equals("UndoButton")) editor.undo();
        if (anEvent.equals("RedoMenuItem") || anEvent.equals("RedoButton")) editor.redo();
        if (anEvent.equals("CutMenuItem") || anEvent.equals("CutButton")) editor.cut();
        if (anEvent.equals("CopyMenuItem") || anEvent.equals("CopyButton")) editor.copy();
        if (anEvent.equals("PasteMenuItem") || anEvent.equals("PasteButton")) editor.paste();
        if (anEvent.equals("SelectAllMenuItem")) editor.selectAll();

        // Edit -> CheckSpellingAsYouTypeMenuItem
        if (anEvent.equals("CheckSpellingAsYouTypeMenuItem")) {
            TextEditor.isSpellChecking = anEvent.getBooleanValue();
            Prefs.getDefaultPrefs().setValue("SpellChecking", TextEditor.isSpellChecking);
            editor.repaint();
        }

        // Edit -> HyphenateTextMenuItem
        if (anEvent.equals("HyphenateTextMenuItem")) {
            TextEditor.setHyphenating(anEvent.getBooleanValue());
            editor.repaint();
        }

        // Handle Format menu items (use name because anObj may come from popup menu)
        if (anEvent.equals("FontPanelMenuItem"))
            epane.getInspector().setVisibleForName(InspectorPane.STYLE_PANE);
        if (anEvent.equals("BoldMenuItem") || anEvent.equals("BoldButton"))
            editor.getStyler().setFontBold(!editor.getStyler().getFont().isBold());
        if (anEvent.equals("ItalicMenuItem") || anEvent.equals("ItalicButton"))
            editor.getStyler().setFontItalic(!editor.getStyler().getFont().isItalic());
        if (anEvent.equals("UnderlineMenuItem"))
            editor.getStyler().setUnderlined(!editor.getStyler().isUnderlined());
        if (anEvent.equals("OutlineMenuItem"))
            editor.getStyler().setTextBorder(Border.blackBorder());
//        if (anEvent.equals("AlignLeftMenuItem") || anEvent.equals("AlignLeftButton"))
//            editor.getStyler().setAlignX(HPos.LEFT);
//        if (anEvent.equals("AlignCenterMenuItem") || anEvent.equals("AlignCenterButton"))
//            editor.getStyler().setAlignX(HPos.CENTER);
//        if (anEvent.equals("AlignRightMenuItem") || anEvent.equals("AlignRightButton"))
//            editor.getStyler().setAlignX(HPos.RIGHT);
//        if (anEvent.equals("AlignFullMenuItem") || anEvent.equals("AlignFullButton"))
//            editor.getStyler().setJustify(true);
//        if (anEvent.equals("SuperscriptMenuItem"))
//            editor.getStyler().setSuperscript();
//        if (anEvent.equals("SubscriptMenuItem"))
//            editor.getStyler().setSubscript();

        // Handle Shapes menu items (use name because anObj may come from popup menu)
        String name = anEvent.getName();
        if (name.equals("GroupMenuItem")) EditorUtils.groupView(editor);
        if (name.equals("UngroupMenuItem")) EditorUtils.ungroupView(editor);

        // Handle Tools menu items
        if (anEvent.equals("GalleryPaneMenuItem"))
            epane.getInspector().setVisibleForName(InspectorPane.GALLERY_PANE);
        if (anEvent.equals("ViewPaneMenuItem"))
            epane.getInspector().setVisibleForName(InspectorPane.VIEW_PANE);
        if (anEvent.equals("StylePaneMenuItem"))
            epane.getInspector().setVisibleForName(InspectorPane.STYLE_PANE);

        // Handle SupportPageMenuItem, TutorialMenuItem
        if (anEvent.equals("SupportPageMenuItem")) URLUtils.openURL("https://reportmill.com/support");
        if (anEvent.equals("TutorialMenuItem")) URLUtils.openURL("https://reportmill.com/support/tutorial.pdf");

        // Handle Theme menus: StandardThemeMenuItem, LightThemeMenuItem, DarkThemeMenuItem, BlackAndWhiteThemeMenuItem
        if (anEvent.equals("StandardThemeMenuItem")) ViewTheme.setThemeForName("Standard");
        if (anEvent.equals("LightThemeMenuItem")) ViewTheme.setThemeForName("Light");
        if (anEvent.equals("DarkThemeMenuItem")) ViewTheme.setThemeForName("Dark");
        if (anEvent.equals("BlackAndWhiteThemeMenuItem")) ViewTheme.setThemeForName("BlackAndWhite");
    }
}