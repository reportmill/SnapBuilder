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
    private Order  _spotOrder;

    // The timer to handle spot painting
    private ViewTimer _spotTimer;

    // Whether to suppress spot painting
    private boolean  _hideSpot;

    // Constants for ordering
    public enum Order { BEFORE, ON, AFTER }

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
        _editor.fireSelPropChange(Editor.SelView_Prop, old, _selView);
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
        _superSelView = aView;
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
    public void setSelSpot(View aView, Order anOrder)
    {
        // If already set, just return
        if (aView==_spotView && anOrder==_spotOrder) return;

        // Set value(s)
        View old = _spotView; _spotView = aView;
        _spotOrder = anOrder;

        // Clear SelView
        _selView = null;

        // Set SuperSelView
        View par = anOrder== Order.ON ? _spotView : _spotView!=null ? _spotView.getParent() : null;
        if (par!=null)
            setSuperSelView(par);

        // Set Value, FirePropChange and repaint
        _editor.fireSelPropChange(Editor.SelView_Prop, old, _spotView);
        _editor.repaint();
        setSpotAnim();
    }

    /**
     * Returns the spot order.
     */
    public Order getSelSpotOrder()  { return _spotOrder; }

    /**
     * Returns the counterpart to the SelSpot View.
     */
    private View getSelSpotOther()
    {
        if (_spotView==null || _spotOrder==null || _spotOrder== Order.ON) return null;
        ParentView par = _spotView.getParent(); if (par==null) return null;
        int ind = _spotView.indexInParent();
        if (_spotOrder== Order.BEFORE)
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
        View hostView = _spotOrder==Order.ON ? sview : sview.getParent();

        if (hostView.isHorizontal()) {
            double x;
            switch (_spotOrder) {
                case BEFORE: x = bnds.x - 2; break;
                case ON: x = bnds.x + 2; break;
                default: x = bnds.getMaxX() + 2; break;
            }
            return new Rect(x, bnds.y - 1, 0, bnds.height + 2);
        }
        double y;
        switch (_spotOrder) {
            case BEFORE: y = bnds.y - 2; break;
            case ON: y = bnds.y + 2; break;
            default: y = bnds.getMaxY() + 2; break;
        }
        return new Rect(bnds.x - 1, y, bnds.width + 2,  0);
    }

    /**
     * Returns the view and index that any new view should be added to.
     */
    public Tuple<View,Integer> getAddViewAndIndex()
    {
        // Get selected view
        View sview = getSelOrSuperSelView();

        // Get index
        int index;
        if (isSelSpot()) {
            View spotView = getSelSpot();
            index = spotView.indexInHost();
            if (getSelSpotOrder()== Order.AFTER) index++;
        }
        else if (sview instanceof ViewHost)
            index = ((ViewHost)sview).getGuestCount();
        else {
            index = sview.indexInHost() + 1;
            sview = (View)sview.getHost();
        }

        // If selected view parent is host, add to it
        return new Tuple<>(sview, index);
    }

    /**
     * Returns the guest view closest to given point.
     */
    public Tuple<View,Order> getSelForPoint(Point aPnt)
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

        ViewHost host = view instanceof ViewHost ? (ViewHost)view : view.getHost();
        View hostView = (View)host;
        Point pnt2 = hostView.parentToLocal(pnt.x, pnt.y, cbox);
        return getSelForHostPoint(host, pnt2);
    }

    /**
     * Returns the guest view closest to given point.
     */
    public Tuple<View,Order> getSelForHostPoint(ViewHost aHost, Point aPnt)
    {
        // If no children, just return host + ON
        View hostView = (View)aHost;
        if (aHost.getGuestCount()==0)
            return new Tuple<>(hostView, Order.ON);

        // Iterate over children
        for (int i=0, iMax=aHost.getGuestCount(); i<iMax; i++) {
            View v1 = aHost.getGuest(i);
            View v2 = i+1<iMax ? aHost.getGuest(i+1) : null;

            // Handle horizontal: Check before first, On first, or before second
            if (hostView.isHorizontal()) {
                if (aPnt.x<v1.getX()+6)
                    return new Tuple<>(v1, Order.BEFORE);
                if (aPnt.x<v1.getMaxX()-6)
                    return new Tuple<>(v1, Order.ON);
                if (v2==null || aPnt.x<v2.getX()+6)
                    return new Tuple<>(v1, Order.AFTER);
            }

            // Handle vertical: Check before first, On first, or before second
            else {
                if (aPnt.y<v1.getY()+6)
                    return new Tuple<>(v1, Order.BEFORE);
                if (aPnt.y<v1.getMaxY()-6)
                    return new Tuple<>(v1, Order.ON);
                if (v2==null || aPnt.y<v2.getY()+6)
                    return new Tuple<>(v1, Order.AFTER);
            }
        }

        // Otherwise it's after last view
        return new Tuple<>(aHost.getGuest(aHost.getGuestCount()-1), Order.AFTER);
    }

    /**
     * Returns the guest view closest to given point.
     */
    public void setSelForPoint(Point aPnt)
    {
        // Get view/order for selection at point
        Tuple <View,Order> sel = getSelForPoint(aPnt);
        View selView = sel.getA();
        Order order = sel.getB();

        // Either select view or spot
        if (order==Order.ON && !(selView instanceof ViewHost))
            setSelView(selView);
        else setSelSpot(selView, order);
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
        for (View v=aView; v!=aTopView; v=v.getParent())
            if (!v.isGuest())
                return false;
        return true;
    }

    /**
     * A tuple to hold a pair of values of two different types.
     */
    public static class Tuple<A,B> {

        // The A/B components
        A _a; B _b;

        /** Constructor. */
        public Tuple(A anA, B aB)
        {
            _a = anA;
            _b = aB;
        }

        /** Returns the A/B components. */
        public A getA()  { return _a; }
        public B getB()  { return _b; }
    }
}
