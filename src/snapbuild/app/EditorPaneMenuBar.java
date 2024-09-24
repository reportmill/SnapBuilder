/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapbuild.app;
import snap.gfx.Border;
import snap.util.Prefs;
import snap.util.URLUtils;
import snap.props.Undoer;
import snap.view.*;

/**
 * Menu bar for Editor pane.
 */
public class EditorPaneMenuBar extends ViewOwner {

    // The EditorPane
    private EditorPane  _editorPane;

    /**
     * Creates a new editor pane menu bar.
     */
    public EditorPaneMenuBar(EditorPane anEP)
    {
        _editorPane = anEP;
    }

    /**
     * Returns the EditorPane.
     */
    public EditorPane getEditorPane()
    {
        return _editorPane;
    }

    /**
     * Returns the editor.
     */
    public Editor getEditor()
    {
        return _editorPane.getEditor();
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
        EditorPane editorPane = getEditorPane();
        Editor editor = getEditor();

        // Handle NewMenuItem, NewButton: Get new editor pane and make visible
        if (anEvent.equals("NewMenuItem") || anEvent.equals("NewButton")) {
            EditorPane editorPane2 = new EditorPane().newDocument();
            editorPane2.setWindowVisible(true);
        }

        // Handle OpenMenuItem, OpenButton: Get new editor pane from open panel and make visible (if created)
        if (anEvent.equals("OpenMenuItem") || anEvent.equals("OpenButton")) {
            EditorPane editorPane2 = new EditorPane().openSource(editorPane.getUI());
            if (editorPane2 != null)
                editorPane2.setWindowVisible(true);
        }

        // Handle CloseMenuItem
        if (anEvent.equals("CloseMenuItem")) editorPane.close();

        // Handle SaveMenuItem, SaveButton, SaveAsMenuItem, RevertMenuItem
        if (anEvent.equals("SaveMenuItem") || anEvent.equals("SaveButton"))
            editorPane.save();
        if (anEvent.equals("SaveAsMenuItem"))
            editorPane.saveAs();
        if (anEvent.equals("RevertMenuItem"))
            editorPane.revert();

        // Handle QuitMenuItem
        if (anEvent.equals("QuitMenuItem"))
            editorPane.quit();

        // Handle Edit menu items
        if (anEvent.equals("UndoMenuItem") || anEvent.equals("UndoButton"))
            editor.undo();
        if (anEvent.equals("RedoMenuItem") || anEvent.equals("RedoButton"))
            editor.redo();
        if (anEvent.equals("CutMenuItem") || anEvent.equals("CutButton"))
            editor.cut();
        if (anEvent.equals("CopyMenuItem") || anEvent.equals("CopyButton"))
            editor.copy();
        if (anEvent.equals("PasteMenuItem") || anEvent.equals("PasteButton"))
            editor.paste();
        if (anEvent.equals("SelectAllMenuItem"))
            editor.selectAll();

        // Edit -> CheckSpellingAsYouTypeMenuItem
        if (anEvent.equals("CheckSpellingAsYouTypeMenuItem")) {
            TextArea.isSpellChecking = anEvent.getBooleanValue();
            Prefs.getDefaultPrefs().setValue("SpellChecking", TextArea.isSpellChecking);
            editor.repaint();
        }

        // Edit -> HyphenateTextMenuItem
        if (anEvent.equals("HyphenateTextMenuItem")) {
            TextArea.setHyphenating(anEvent.getBooleanValue());
            editor.repaint();
        }

        // Handle Format menu items (use name because anObj may come from popup menu)
        if (anEvent.equals("FontPanelMenuItem"))
            editorPane.getInspector().setVisibleForName(InspectorPane.STYLE_PANE);
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
            editorPane.getInspector().setVisibleForName(InspectorPane.GALLERY_PANE);
        if (anEvent.equals("ViewPaneMenuItem"))
            editorPane.getInspector().setVisibleForName(InspectorPane.VIEW_PANE);
        if (anEvent.equals("StylePaneMenuItem"))
            editorPane.getInspector().setVisibleForName(InspectorPane.STYLE_PANE);

        // Handle SupportPageMenuItem, TutorialMenuItem
        if (anEvent.equals("SupportPageMenuItem"))
            URLUtils.openURL("https://reportmill.com/support");
        if (anEvent.equals("TutorialMenuItem"))
            URLUtils.openURL("https://reportmill.com/support/tutorial.pdf");

        // Handle Theme menus: StandardThemeMenuItem, LightThemeMenuItem, DarkThemeMenuItem, BlackAndWhiteThemeMenuItem
        if (anEvent.equals("StandardThemeMenuItem")) ViewTheme.setThemeForName("Standard");
        if (anEvent.equals("LightThemeMenuItem")) ViewTheme.setThemeForName("Light");
        if (anEvent.equals("DarkThemeMenuItem")) ViewTheme.setThemeForName("Dark");
        if (anEvent.equals("BlackAndWhiteThemeMenuItem")) ViewTheme.setThemeForName("BlackAndWhite");
    }
}