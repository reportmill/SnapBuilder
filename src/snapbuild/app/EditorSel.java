package snapbuild.app;
import snap.geom.Point;
import snap.geom.Rect;
import snap.geom.Shape;
import snap.gfx.Color;
import snap.gfx.Painter;
import snap.gfx.Stroke;
import snap.view.*;

/**
 * A class to represent the editor selection.
 */
public class EditorSel {

    // The editor
    private Editor _editor;

    // The Selected View
    private View _selView;

    // The Super Selected View
    private View  _superSelView;

    // The relative spot view (if selection is a spot)
    private View  _spotView;

    // The relaive spot order
    private Editor.Order _spotOrder;

    // The timer to handle spot painting
    private ViewTimer _spotTimer;

    // Whether to suppress spot painting
    private boolean  _hideSpot;

    /**
     * Constructor.
     */
    public EditorSel(Editor anEditor)  { _editor = anEditor; }

    /**
     * Returns the selected view.
     */
    public View getSelView()  { return _selView; }

    /**
     * Sets the selected view.
     */
    public void setSelView(View aView)
    {
        // If already set, just return
        if (aView==getSelView()) return;

        // Set value
        View old = _selView;
        _selView = aView;
        _spotView = null;
        _spotOrder = null;

        // Set SuperSelView
        View par = _selView!=null ? _selView.getParent() : null;
        if (par!=null)
            setSuperSelView(par);

        // FirePropChange and repaint
        //firePropChange(SelView_Prop, old, _selView);
        _editor.repaint();
        setSpotAnim();
    }

    /**
     * Sets the super selected view.
     */
    public View getSuperSelView()  { return _superSelView; }

    /**
     * Sets the super selected view.
     */
    public void setSuperSelView(View aView)
    {
        // If value already set, just return
        if (aView==getSuperSelView()) return;

        // Set value
        View old = _superSelView;
        _superSelView = aView;

        // FirePropChange and repaint
        //firePropChange(SuperSelView_Prop, old, _superSelView);
        _editor.repaint();
    }

    /**
     * Returns the selected or super selected view.
     */
    public View getSelOrSuperSelView()
    {
        return _selView!=null ? _selView : _superSelView;
    }

    /**
     * Returns whether selection is really just a spot.
     */
    public boolean isSelSpot()  { return _spotView!=null; }

    /**
     * Returns the spot selection view.
     */
    public View getSelSpot()  { return _spotView; }

    /**
     * Sets a selected spot relative to given view (before = -1, on = 0, after = 1).
     */
    public void setSelSpot(View aView, Editor.Order anOrder)
    {
        // If already set, just return
        if (aView==_spotView && anOrder==_spotOrder) return;

        // Set value(s)
        View old = _spotView; _spotView = aView;
        _spotOrder = anOrder;

        // Clear SelView
        _selView = null;

        // Set SuperSelView
        View par = anOrder== Editor.Order.ON ? _spotView : _spotView!=null ? _spotView.getParent() : null;
        if (par!=null)
            setSuperSelView(par);

        // Set Value, FirePropChange and repaint
        //_editor.firePropChange(SelSpot_Prop, old, _spotView);
        _editor.repaint();
        setSpotAnim();
    }

    /**
     * Returns the spot order.
     */
    public Editor.Order getSelSpotOrder()  { return _spotOrder; }

    /**
     * Returns the counterpart to the SelSpot View.
     */
    private View getSelSpotOther()
    {
        if (_spotView==null || _spotOrder==null || _spotOrder== Editor.Order.ON) return null;
        ParentView par = _spotView.getParent(); if (par==null) return null;
        int ind = _spotView.indexInParent();
        if (_spotOrder== Editor.Order.BEFORE)
            return ind>0 ? par.getChild(ind-1) : null;
        return ind+1<par.getChildCount() ? par.getChild(ind+1) : null;
    }

    /**
     * Paint selection.
     */
    public void paintSel(Painter aPntr)
    {
        if (_editor.isFocused() && isSelSpot() && !_hideSpot) {
            Shape shape = getSelSpotShape();
            aPntr.setPaint(Color.BLACK); aPntr.setStroke(Stroke.Stroke1);
            aPntr.draw(shape);
        }
    }

    /**
     * Returns the Shape used to paint SelSpot.
     */
    private Shape getSelSpotShape()
    {
        View sview = _spotView;
        Rect bnds = sview.localToParent(sview.getBoundsShape(), _editor).getBounds();
        double x = _spotOrder== Editor.Order.BEFORE ? bnds.x - 2 : bnds.getMaxX() + 2;
        Rect rect = new Rect(x, bnds.y-1, 0, bnds.height+2);
        return rect;
    }

    /**
     * Returns the view and index that any new view should be added to.
     */
    public Tuple<View,Integer> getAddViewAndIndex()
    {
        // Get selected view
        View sview = getSelOrSuperSelView();

        // Get index
        int index = sview instanceof ViewHost ? ((ViewHost)sview).getGuestCount() : 0;
        if (isSelSpot()) {
            sview = getSelSpot();
            index = sview.indexInHost();
            if (getSelSpotOrder()== Editor.Order.AFTER) index++;
        }

        // If selected view parent is host, add to it
        if(!(sview instanceof ViewHost) && sview.getHost()!=null)
            sview = (View)sview.getHost();
        return new Tuple(sview, index);
    }

    /**
     * Returns the guest view closest to given point.
     */
    public void setSelectionForPoint(Point aPnt)
    {
        // Get event point in ContentBox
        View cbox = _editor.getContentBox();
        View content = _editor.getContent();
        Point pnt = cbox.parentToLocal(aPnt.x, aPnt.y, _editor);
        View view = ViewUtils.getDeepestChildAt(cbox, pnt.x, pnt.y);

        // If not found, constrain to Editor.Content
        if (view==null || view==cbox)
            view = content;

        // Get deepest guest view (child of ViewHost)
        else view = getViewOrParentThatIsGuest(view, content);

        // If point close to edge, setSpotView
        Point pnt2 = view.parentToLocal(pnt.x, pnt.y, cbox);
        if (pnt2.x>view.getWidth()-6)
            setSelSpot(view, Editor.Order.AFTER);
        else if (pnt2.x<6)
            setSelSpot(view, Editor.Order.BEFORE);

        // Select new view
        else setSelView(view);
    }

    /**
     * Sets the caret animation to whether it's needed.
     */
    private void setSpotAnim()
    {
        boolean isNeeded = _editor.isFocused() && isSelSpot() && _editor.isShowing();
        setSpotAnim(isNeeded);
    }

    /**
     * Returns whether ProgressBar is animating.
     */
    private boolean isSpotAnim()  { return _spotTimer!=null; }

    /**
     * Sets anim.
     */
    private void setSpotAnim(boolean aValue)
    {
        if (aValue==isSpotAnim()) return;
        if (aValue) {
            _spotTimer = new ViewTimer(500, t -> toggleShowSpot());
            _spotTimer.start();
        }
        else { _spotTimer.stop(); _spotTimer = null; _hideSpot = false; _editor.repaint(); }
    }

    /**
     * Called to make spot show/hid.
     */
    private void toggleShowSpot()
    {
        _hideSpot = !_hideSpot;
        _editor.repaint();
    }

    /**
     * Returns given view or parent that is guest.
     */
    private static View getViewOrParentThatIsGuest(View aView, View aTopView)
    {
        if (aView==aTopView) return aView;
        if (isGuestAllTheWayUP(aView, aTopView))
            return aView;
        View par = aView.getParent();
        return getViewOrParentThatIsGuest(par, aTopView);
    }

    /**
     * Returns whether given view is guest all the way up to given top view.
     */
    private static boolean isGuestAllTheWayUP(View aView, View aTopView)
    {
        boolean guest = true;
        for (View v=aView; v!=aTopView && guest; v=v.getParent()) guest &= v.isGuest();
        return guest;
    }

    /**
     * A tuple to hold a pair of values of two different types.
     */
    public static class Tuple<A,B> {
        A _a;
        B _b;
        public Tuple(A anA, B aB)
        {
            _a = anA;
            _b = aB;
        }

        public A getA()  { return _a; }
        public B getB()  { return _b; }
    }
}
