package snapbuild.app;
import snap.geom.Point;
import snap.geom.Rect;
import snap.geom.Shape;
import snap.gfx.Color;
import snap.gfx.Painter;
import snap.gfx.Stroke;
import snap.props.PropChangeListener;
import snap.view.*;

/**
 * A class to represent the editor selection.
 */
public class EditorSel {

    // The editor
    protected Editor _editor;

    // The Selected View
    private View _selView;

    // The selected index, if SelView is host
    private int _selIndex;

    // The timer to handle spot painting
    private ViewTimer _spotTimer;

    // Whether to suppress spot painting
    private boolean _hideSpot;

    // A PropChangeListener to enable/disable caret when window loses focus
    private PropChangeListener _windowFocusedChangedLsnr;

    // A pointer to window this TextArea is showing in so we can remove WindowFocusChangedLsnr
    private WindowView _showingWindow;

    /**
     * Constructor.
     */
    public EditorSel(Editor anEditor)
    {
        _editor = anEditor;
        _editor.addPropChangeListener(pc -> setSpotAnim(), View.Focused_Prop);
        _editor.addPropChangeListener(pc -> handleEditorShowingChange(), View.Showing_Prop);
    }

    /**
     * Returns the selected view.
     */
    public View getSelView()
    {
        return _selView;
    }

    /**
     * Sets the selected view.
     */
    public void setSelView(View aView)
    {
        // If already set, just return
        if (aView == _selView) return;

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
        return _selView instanceof ViewHost && _selIndex >= 0;
    }

    /**
     * Returns the selected index if SelView is host.
     */
    public int getSelIndex()
    {
        return _selIndex;
    }

    /**
     * Sets a selected spot relative to given view (before = -1, on = 0, after = 1).
     */
    public void setSelViewAndIndex(View aView, int anIndex)
    {
        // If already set, just return
        if (aView == _selView && anIndex == _selIndex) return;

        // Set values
        setSelView(aView);
        _selIndex = anIndex;

        // Set Value, FirePropChange and repaint
        _editor.repaint();
        setSpotAnim();
    }

    /**
     * Paint selection.
     */
    public void paintSel(Painter aPntr)
    {
        if (_editor.isFocused() && isSelSpot() && !_hideSpot) {
            Shape shape = getViewIndexShape(getSelView(), getSelIndex());
            aPntr.setPaint(Color.BLACK);
            aPntr.setStroke(Stroke.Stroke1);
            aPntr.draw(shape);
        }
    }

    /**
     * Returns the Shape used to paint SelSpot.
     */
    public Shape getViewIndexShape(View aHostView, int anIndex)
    {
        // Get view for index
        ViewPair indexViews = getViewIndexViews(aHostView, anIndex);
        View viewBefore = indexViews.getA();
        View viewAfter = indexViews.getB();
        View indexView = viewBefore != null ? viewBefore : viewAfter != null ? viewAfter : aHostView;
        Rect bnds = indexView.localToParent(indexView.getBoundsShape(), _editor).getBounds();
        boolean isBeforeView = indexView == viewBefore;
        boolean isAfterView = indexView == viewAfter;

        // Handle host horizontal: Return vertical line
        if (aHostView.isHorizontal()) {
            double x = isBeforeView ? (bnds.getMaxX() + 2) : isAfterView ? (bnds.x - 2) : (bnds.x + 2);
            return new Rect(x, bnds.y - 1, 0, bnds.height + 2);
        }

        // Handle host vertical: Return horizontal line
        double y = isBeforeView ? (bnds.getMaxY() + 2) : isAfterView ? (bnds.y - 2) : (bnds.y + 2);
        return new Rect(bnds.x - 1, y, bnds.width + 2, 0);
    }

    /**
     * Returns the views before and after the SelIndex.
     */
    private ViewPair getViewIndexViews(View aHostView, int anIndex)
    {
        ViewHost host = (ViewHost) aHostView;
        View viewBefore = anIndex > 0 && anIndex - 1 < host.getGuestCount() ? host.getGuest(anIndex - 1) : null;
        View viewAfter = anIndex < host.getGuestCount() ? host.getGuest(anIndex) : null;
        return new ViewPair(viewBefore, viewAfter);
    }

    /**
     * Returns the view and index that any new view should be added to.
     */
    public ViewIndex getSelHostViewAndIndex()
    {
        // If SelView is host, return SelView and insertion index
        View selView = getSelView();
        if (selView instanceof ViewHost) {
            ViewHost host = (ViewHost) selView;
            int ind = getSelIndex();
            int gcount = host.getGuestCount();
            int ind2 = ind >= 0 && ind < gcount ? ind : gcount;
            return new ViewIndex(selView, ind2);
        }

        // Otherwise get host and return end
        ViewHost host = selView.getHost();
        if (host != null) {
            int ind = selView.indexInHost() + 1;
            return new ViewIndex((View) host, ind);
        }

        // Return null (can this happen?)
        return null;
    }

    /**
     * Returns the guest view closest to given point.
     */
    public ViewIndex getViewAndIndexForPoint(Point aPnt)
    {
        // Get event point in ContentBox
        View contentBox = _editor.getContentBox();
        View content = _editor.getContent();
        Point pointInContentBox = contentBox.parentToLocal(aPnt.x, aPnt.y, _editor);
        View hitView = ViewUtils.getDeepestChildAt(contentBox, pointInContentBox.x, pointInContentBox.y);

        // If not found, constrain to Editor.Content
        if (hitView == null || hitView == contentBox) {
            ViewHost host = (ViewHost) content;
            return new ViewIndex(content, host.getGuestCount());
        }

        // Get deepest guest view (child of ViewHost)
        hitView = getViewOrParentThatIsGuest(hitView, content);

        ViewHost host = hitView instanceof ViewHost ? (ViewHost) hitView : hitView.getHost();
        View hostView = (View) host;
        Point pointInHostView = hostView.parentToLocal(pointInContentBox.x, pointInContentBox.y, contentBox);
        return getViewAndIndexForHostPoint(host, pointInHostView);
    }

    /**
     * Returns the guest view closest to given point.
     */
    private ViewIndex getViewAndIndexForHostPoint(ViewHost aHost, Point aPnt)
    {
        // If no children, just return index 0
        View hostView = (View) aHost;
        if (aHost.getGuestCount() == 0)
            return new ViewIndex(hostView, 0);

        // Iterate over children
        for (int i = 0, iMax = aHost.getGuestCount(); i < iMax; i++) {
            View v1 = aHost.getGuest(i);
            View v2 = i + 1 < iMax ? aHost.getGuest(i + 1) : null;
            double v1Marg = Math.max(Math.round(hostView.isHorizontal() ? v1.getWidth() * .1 : v1.getHeight() * .1), 6);
            double v2Marg = v2 != null ? Math.max(Math.round(hostView.isHorizontal() ? v2.getWidth() * .1 : v2.getHeight() * .1), 6) : 0;

            // Handle horizontal: Check before first, On first, or before second
            if (hostView.isHorizontal()) {
                if (aPnt.x < v1.getX() + v1Marg)
                    return new ViewIndex(hostView, i);
                if (aPnt.x < v1.getMaxX() - v1Marg)
                    return new ViewIndex(v1, null);
                if (v2 == null || aPnt.x < v2.getX() + v2Marg)
                    return new ViewIndex(hostView, i + 1);
            }

            // Handle vertical: Check before first, On first, or before second
            else {
                if (aPnt.y < v1.getY() + v1Marg)
                    return new ViewIndex(hostView, i);
                if (aPnt.y < v1.getMaxY() - v1Marg)
                    return new ViewIndex(v1, null);
                if (v2 == null || aPnt.y < v2.getY() + v2Marg)
                    return new ViewIndex(hostView, i + 1);
            }
        }

        // Otherwise it's after last view
        return new ViewIndex(hostView, aHost.getGuestCount());
    }

    /**
     * Returns a HostView for point.
     */
    public View getHostViewForPoint(Point aPoint)
    {
        ViewIndex viewIndex = getHostViewAndIndexForPoint(aPoint);
        return viewIndex.view();
    }

    /**
     * Returns a HostView for point.
     */
    public ViewIndex getHostViewAndIndexForPoint(Point aPoint)
    {
        // Get normal view+index for point
        ViewIndex viewIndex = getViewAndIndexForPoint(aPoint);

        //  If View is ViewHost, just return
        View view = viewIndex.view();
        if (view instanceof ViewHost) {

            // If index if valid, just return
            if (viewIndex.index() != null)
                return viewIndex;

            // Otherwise, use View.Host and View.indexInHost (adjusted if needed)
            ViewHost host = (ViewHost) view;
            return new ViewIndex(view, host.getGuestCount());
        }

        // Otherwise get ViewHost
        int index = view.indexInHost();
        view = (View) view.getHost();
        return new ViewIndex(view, index);
    }

    /**
     * Returns the guest view closest to given point.
     */
    public void setSelForPoint(Point aPnt)
    {
        // Get view/order for selection at point
        ViewIndex viewIndex = getViewAndIndexForPoint(aPnt);
        if (viewIndex == null) {
            View hostView = _editor.getContent();
            ViewHost host = (ViewHost) _editor.getContent();
            setSelViewAndIndex(hostView, host.getGuestCount());
            return;
        }

        // Get SelView, SelIndex
        View selView = viewIndex.view();
        Integer index = viewIndex.index();

        // Either select view or spot
        if (index == null)
            setSelView(selView);
        else setSelViewAndIndex(selView, index);
    }

    /**
     * Sets the caret animation to whether it's needed.
     */
    private void setSpotAnim()
    {
        // Get criteria
        boolean editorFocused = _editor.isFocused();
        boolean isSelSpot = isSelSpot();
        boolean editorShowing = _editor.isShowing();
        WindowView editorWindow = _editor.getWindow();
        boolean editorWindowFocused = editorWindow != null && editorWindow.isFocused();

        // Get whether anim is needed and set
        boolean isNeeded = editorFocused && isSelSpot && editorShowing && editorWindowFocused;
        setSpotAnim(isNeeded);
    }

    /**
     * Returns whether ProgressBar is animating.
     */
    private boolean isSpotAnim()
    {
        return _spotTimer != null;
    }

    /**
     * Sets anim.
     */
    private void setSpotAnim(boolean aValue)
    {
        // If already set, just return
        if (aValue == isSpotAnim()) return;

        // Turn timer on
        if (aValue) {
            _spotTimer = new ViewTimer(this::toggleShowSpot, 500);
            _spotTimer.start();
        }

        // Turn off
        else {
            _spotTimer.stop();
            _spotTimer = null;
            _hideSpot = false;
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
     * Called when Editor.Showing changes.
     */
    private void handleEditorShowingChange()
    {
        // Update anim
        setSpotAnim();

        // Manage listener for Window.Focus changes
        updateWindowFocusChangedLsnr();
    }

    /**
     * Updates WindowFocusChangedLsnr when Showing prop changes to update caret showing.
     */
    private void updateWindowFocusChangedLsnr()
    {
        // Handle Showing: Set ShowingWindow, add WindowFocusChangedLsnr and reset caret
        if (_editor.isShowing()) {
            _showingWindow = _editor.getWindow();
            if (_showingWindow != null) {
                _windowFocusedChangedLsnr = e -> setSpotAnim();
                _showingWindow.addPropChangeListener(_windowFocusedChangedLsnr, View.Focused_Prop);
            }
        }

        // Handle not Showing: Remove WindowFocusChangedLsnr and clear
        else {
            if (_showingWindow != null)
                _showingWindow.removePropChangeListener(_windowFocusedChangedLsnr);
            _showingWindow = null;
            _windowFocusedChangedLsnr = null;
        }
    }

    /**
     * Returns given view or parent that is guest.
     */
    private static View getViewOrParentThatIsGuest(View aView, View aTopView)
    {
        if (aView == aTopView) return aView;
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
        for (View v = aView; v != aTopView; v = v.getParent())
            if (!v.isGuest())
                return false;
        return true;
    }

    /**
     * A class to hold a view and index for a point.
     */
    public static class ViewIndex {

        View _a;
        Integer _b;

        /** Constructor. */
        public ViewIndex(View anA, Integer aB)
        {
            _a = anA;
            _b = aB;
        }

        public View view()  { return _a; }
        public Integer index()  { return _b; }
    }

    /**
     * A class to hold a pair of views.
     */
    public static class ViewPair {

        View _a, _b;

        /** Constructor. */
        public ViewPair(View anA, View aB)  { _a = anA; _b = aB; }

        public View getA()  { return _a; }
        public View getB()  { return _b; }
    }
}
