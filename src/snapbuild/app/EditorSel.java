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

    // The selected index, if SelView is host
    private int  _selIndex;

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

        // FirePropChange and repaint
        _editor.fireSelPropChange(Editor.SelView_Prop, old, _selView);
        _editor.repaint();
        setSpotAnim();
    }

    /**
     * Returns whether selection is really just a spot.
     */
    public boolean isSelSpot()
    {
        return getSelView() instanceof ViewHost && _selIndex>=0;
    }

    /**
     * Returns the selected index if SelView is host.
     */
    public int getSelIndex()  { return _selIndex; }

    /**
     * Sets a selected spot relative to given view (before = -1, on = 0, after = 1).
     */
    public void setSelViewAndIndex(View aView, int anIndex)
    {
        // If already set, just return
        if (aView==_selView && anIndex==_selIndex) return;

        // Set values
        setSelView(aView);
        _selIndex = anIndex;

        // Set Value, FirePropChange and repaint
        _editor.repaint();
        setSpotAnim();
    }

    /**
     * Returns the most appropriate view for the SelIndex (either ViewBefore, ViewAfter or ViewHost).
     */
    private View getSelIndexView()
    {
        View before = getSelIndexViewBefore();
        if (before!=null)
            return before;
        View after = getSelIndexViewAfter();
        if (after!=null)
            return after;
        View selView = getSelView();
        return selView instanceof ViewHost ? selView : null;
    }

    /**
     * Returns the view before the SelIndex.
     */
    private View getSelIndexViewBefore()
    {
        int ind = getSelIndex();
        if (ind<=0)
            return null;
        View hostView = getSelView();
        ViewHost host = (ViewHost)hostView;
        if (ind-1<host.getGuestCount())
            return host.getGuest(ind-1);
        return null;
    }

    /**
     * Returns the view after the SelIndex.
     */
    private View getSelIndexViewAfter()
    {
        int ind = getSelIndex();
        if (ind<0)
            return null;
        View hostView = getSelView();
        ViewHost host = (ViewHost)hostView;
        if (ind<host.getGuestCount())
            return host.getGuest(ind);
        return null;
    }

    /**
     * Paint selection.
     */
    public void paintSel(Painter aPntr)
    {
        if (_editor.isFocused() && isSelSpot() && !_hideSpot) {
            Shape shape = getSelIndexShape();
            aPntr.setPaint(Color.BLACK); aPntr.setStroke(Stroke.Stroke1);
            aPntr.draw(shape);
        }
    }

    /**
     * Returns the Shape used to paint SelSpot.
     */
    private Shape getSelIndexShape()
    {
        // Get view for index
        View indexView = getSelIndexView();
        Rect bnds = indexView.localToParent(indexView.getBoundsShape(), _editor).getBounds();
        View hostView = indexView instanceof ViewHost ? indexView : indexView.getParent();
        boolean isBeforeView = indexView==getSelIndexViewBefore();
        boolean isAfterView = indexView==getSelIndexViewAfter();

        // Handle host horizontal: Return vertical line
        if (hostView.isHorizontal()) {
            double x = isBeforeView ? (bnds.getMaxX() + 2) : isAfterView ? (bnds.x - 2) : (bnds.x + 2);
            return new Rect(x, bnds.y - 1, 0, bnds.height + 2);
        }

        // Handle host vertical: Return horizontal line
        double y = isBeforeView ? (bnds.getMaxY() + 2) : isAfterView ? (bnds.y - 2) : (bnds.y + 2);
        return new Rect(bnds.x - 1, y, bnds.width + 2,  0);
    }

    /**
     * Returns the view and index that any new view should be added to.
     */
    public Tuple<View,Integer> getAddViewAndIndex()
    {
        // If SelView is host, return SelView and insertion index
        View selView = getSelView();
        if (selView instanceof ViewHost) { ViewHost host = (ViewHost)selView;
            int ind = getSelIndex();
            int gcount = host.getGuestCount();
            int ind2 = ind>=0 && ind<gcount ? ind : gcount;
            return new Tuple<>(selView, ind2);
        }

        // Otherwise get host and return end
        ViewHost host = selView.getHost();
        if (host!=null) {
            int ind = selView.indexInHost() + 1;
            return new Tuple<>((View) host, ind);
        }

        // Return null (can this happen?)
        return null;
    }

    /**
     * Returns the guest view closest to given point.
     */
    public Tuple<View,Integer> getSelForPoint(Point aPnt)
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
    public Tuple<View,Integer> getSelForHostPoint(ViewHost aHost, Point aPnt)
    {
        // If no children, just return index 0
        View hostView = (View)aHost;
        if (aHost.getGuestCount()==0)
            return new Tuple<>(hostView, 0);

        // Iterate over children
        for (int i=0, iMax=aHost.getGuestCount(); i<iMax; i++) {
            View v1 = aHost.getGuest(i);
            View v2 = i+1<iMax ? aHost.getGuest(i+1) : null;

            // Handle horizontal: Check before first, On first, or before second
            if (hostView.isHorizontal()) {
                if (aPnt.x<v1.getX()+6)
                    return new Tuple<>(hostView, i);
                if (aPnt.x<v1.getMaxX()-6)
                    return new Tuple<>(v1, null);
                if (v2==null || aPnt.x<v2.getX()+6)
                    return new Tuple<>(hostView, i+1);
            }

            // Handle vertical: Check before first, On first, or before second
            else {
                if (aPnt.y<v1.getY()+6)
                    return new Tuple<>(hostView, i);
                if (aPnt.y<v1.getMaxY()-6)
                    return new Tuple<>(v1, null);
                if (v2==null || aPnt.y<v2.getY()+6)
                    return new Tuple<>(hostView, i+1);
            }
        }

        // Otherwise it's after last view
        return new Tuple<>(hostView, aHost.getGuestCount());
    }

    /**
     * Returns the guest view closest to given point.
     */
    public void setSelForPoint(Point aPnt)
    {
        // Get view/order for selection at point
        Tuple <View,Integer> sel = getSelForPoint(aPnt);
        View selView = sel.getA();
        Integer index = sel.getB();

        // Either select view or spot
        if (index==null)
            setSelView(selView);
        else setSelViewAndIndex(selView, index);
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
        // If already set
        if (aValue==isSpotAnim()) {
            if (aValue) {
                _spotTimer.stop();
                _spotTimer.start();
                _hideSpot = false;
            }
            return;
        }

        // Turn timer on
        if (aValue) {
            _spotTimer = new ViewTimer(500, t -> toggleShowSpot());
            _spotTimer.start();
        }

        // Turn off
        else {
            _spotTimer.stop();
            _spotTimer = null; _hideSpot = false;
            _editor.repaint();
        }
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
