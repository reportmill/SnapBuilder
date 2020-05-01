package snapbuild.app;
import rmdraw.editors.Styler;
import snap.geom.HPos;
import snap.geom.Pos;
import snap.gfx.*;
import snap.text.TextFormat;
import snap.view.View;
import java.util.List;

/**
 *
 */
public class EditorStyler extends Styler {

    // The editor
    private Editor _editor;

    /**
     * Creates EditorStyler.
     */
    public EditorStyler(Editor anEditor)
    {
        _editor = anEditor;
    }

    /**
     * Returns the currently selected border.
     */
    public Border getBorder()
    {
        return getSelView().getBorder();
    }

    /**
     * Sets the currently selected border.
     */
    public void setBorder(Border aBorder)
    {
        getSelView().setBorder(aBorder);
    }

    /**
     * Returns the fill of currently selected view.
     */
    public Paint getFill()
    {
        return getSelView().getFill();
    }

    /**
     * Sets the fill of currently selected views.
     */
    public void setFill(Paint aPaint)
    {
        getSelView().setFill(aPaint);
    }

    /**
     * Returns the font of editor's selected shape.
     */
    public Font getFont()
    {
        return getSelView().getFont();
    }

    /**
     * Sets the current font.
     */
    public void setFont(Font aFont)
    {
        getSelView().setFont(aFont);
    }

    /**
     * Returns the current effect.
     */
    public Effect getEffect()
    {
        return getSelView().getEffect();
    }

    /**
     * Sets the current effect.
     */
    public void setEffect(Effect anEffect)
    {
        getSelView().setEffect(anEffect);
    }

    /**
     * Returns the current opacity.
     */
    public double getOpacity()
    {
        return getSelView().getOpacity();
    }

    /**
     * Sets the currently selected opacity.
     */
    public void setOpacity(double aValue)
    {
        setUndoTitle("Transparency Change");
        getSelView().setOpacity(aValue);
    }

    /**
     * Returns the client View.
     */
    public View getClientView()  { return _editor; }

    /**
     * Returns the Selected View.
     */
    private View getSelView()
    {
        return _editor.getSelView();
    }

    /**
     * Sets undo title.
     */
    public void setUndoTitle(String aTitle)
    {
        _editor.getUndoer().setUndoTitle(aTitle);
    }
}
