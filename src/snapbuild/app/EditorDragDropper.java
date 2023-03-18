/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapbuild.app;
import snap.geom.Point;
import snap.geom.Rect;
import snap.geom.Shape;
import snap.gfx.Color;
import snap.gfx.Image;
import snap.gfx.Painter;
import snap.view.*;

import java.util.List;

/**
 * Handles editor methods specific to drag and drop operations.
 */
public class EditorDragDropper {

    // The editor that this class is working for
    private Editor _editor;

    // The view that current drag and drop event is over
    private View _dragOverView;

    // The index in DragOverView that current drop would hit
    private int _dragOverViewIndex;

    /**
     * Creates a new editor drop target listener.
     */
    public EditorDragDropper(Editor anEditor)
    {
        _editor = anEditor;
    }

    /**
     * Returns the editor.
     */
    public Editor getEditor()
    {
        return _editor;
    }

    /**
     * Implemented by views that can handle drag & drop.
     */
    public boolean acceptsDrag(View aView, ViewEvent anEvent)
    {
        // Handle file drag - really just want to check for images here, but can't ask for transferable contents yet
        if (anEvent.getClipboard().hasFiles())
            return true;

        // Return true in any case if accepts children
        return false;
    }

    /**
     * Drop target listener method.
     */
    public void dragEnter(ViewEvent anEvent)
    {
        _dragOverView = null;
        dragOver(anEvent);
    }

    /**
     * Drop target listener method.
     */
    public void dragOver(ViewEvent anEvent)
    {
        // Windows calls this method continuously, as long as the mouse is held down
        //if(anEvent.getPoint().equals(_lastDragPoint)) return; _lastDragPoint = anEvent.getPoint();

        // Accept drag
        anEvent.acceptDrag(); //DnDConstants.ACTION_COPY);

        // Get view at drag point - if new DragOverView, update and request repaint
        EditorSel.Tuple<View, Integer> viewIndex = _editor.getSel().getHostViewAndIndexForPoint(anEvent.getPoint());
        View view = viewIndex.getA();
        int index = viewIndex.getB();
        if (view != _dragOverView || index != _dragOverViewIndex) {
            _dragOverView = view;
            _dragOverViewIndex = index;
            _editor.repaint();
        }
    }

    /**
     * Drop target listener method.
     */
    public void dragExit(ViewEvent anEvent)
    {
        _editor.repaint();
        _dragOverView = null;
    }

    /**
     * Drop target listener method.
     */
    public void dragDrop(ViewEvent anEvent)
    {
        // Formally accept drop
        anEvent.acceptDrag();

        // Order window front (for any getMainEditor calls, but really should be true anyway)
        _editor.getWindow().toFront();

        // Call real dragDrop
        dragDropImpl(anEvent);

        // Formally complete drop
        anEvent.dropComplete();

        // Clear DragShape (which may have been set during dragOver)
        _editor.repaint();
        _dragOverView = null;
    }

    /**
     * Called to drop string.
     */
    private void dragDropImpl(ViewEvent anEvent)
    {
        // Handle File drop - get list of dropped files and add individually
        Clipboard cb = anEvent.getClipboard();
        if (cb.hasFiles())
            dropFiles(anEvent);
    }

    /**
     * Called to handle dropping a file.
     */
    public void dropFiles(ViewEvent anEvent)
    {
        List<ClipboardData> filesList = anEvent.getClipboard().getFiles();
        for (ClipboardData file : filesList)
            dropFile(file, anEvent.getPoint());
    }

    /**
     * Called to handle a file drop on the editor.
     */
    private void dropFile(ClipboardData aFile, Point aPoint)
    {
        // If file not loaded, come back when it is
        if (!aFile.isLoaded()) {
            aFile.addLoadListener(f -> dropFile(aFile, aPoint));
            return;
        }

        // Get path and extension (set to empty string if null)
        String ext = aFile.getExtension();
        if (ext == null) return;
        ext = ext.toLowerCase();

        // If image file, add image view
        if (Image.canRead(ext))
            dropImageFile(aFile, aPoint);

            // Handle .snp file
        else if (ext.equals("snp"))
            dropSnapFile(aFile);
    }

    /**
     * Called to handle an image drop on the editor.
     */
    private void dropImageFile(ClipboardData aFile, Point aPoint)
    {
        // Get image source and image
        Object imgSrc = aFile.getSourceURL() != null ? aFile.getSourceURL() : aFile.getBytes();
        Image image = Image.get(imgSrc);

        // Create ImageView and add at point
        ImageView iview = new ImageView(image);
        _editor.addViewAtPoint(iview, aPoint);
    }

    /**
     * Handle drop of .snp UI file on the editor.
     */
    private void dropSnapFile(ClipboardData aFile)
    {
        // Get EditorPane
        EditorPane editorPane = EditorPane.getEditorPane(_editor);
        if (editorPane == null)
            return;

        // Get file source and open
        Object fileSrc = aFile.getSourceURL() != null ? aFile.getSourceURL() : aFile.getBytes();
        editorPane.open(fileSrc);
    }

    /**
     * Paints the drag.
     */
    public void paintDrag(Painter aPntr)
    {
        // If no DragOverView, just return
        if (_dragOverView == null) return;

        // Paint rect around DrawOverView
        Rect bounds = _dragOverView.getBoundsLocal();
        Shape dragShape = _dragOverView.localToParent(bounds, _editor);
        aPntr.setColor(new Color(0, .6, 1, .5));
        aPntr.setStrokeWidth(3);
        aPntr.draw(dragShape);

        // Paint insertion point
        Shape selShape = _editor.getSel().getViewIndexShape(_dragOverView, _dragOverViewIndex);
        aPntr.draw(selShape);
    }

    /**
     * Dispatches event to appropriate drag method.
     */
    public void processDragEvent(ViewEvent anEvent)
    {
        switch (anEvent.getType()) {
            case DragEnter:
                dragEnter(anEvent);
                break;
            case DragOver:
                dragOver(anEvent);
                break;
            case DragExit:
                dragExit(anEvent);
                break;
            case DragDrop:
                dragDrop(anEvent);
                break;
            default:
                throw new RuntimeException("DragDropper.processDragEvent: Unknown event type: " + anEvent.getType());
                //case DragActionChanged: anEvent.acceptDrag(DnDConstants.ACTION_COPY);
        }
    }
}