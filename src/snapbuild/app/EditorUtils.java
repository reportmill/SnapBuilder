package snapbuild.app;
import snap.view.*;
import snap.viewx.*;

/**
 * Utility methods for editor.
 */
public class EditorUtils {

    /**
     * Moves a view up/forward in a child list.
     */
    public static void moveViewUp(Editor anEditor)
    {
        View sview = anEditor.getSelView();
        ViewHost host = sview.getHost();
        if (host == null) {
            ViewUtils.beep();
            return;
        }

        int ind = sview.indexInParent();
        if (ind == 0) {
            ViewUtils.beep();
            return;
        }
        host.removeGuest(sview);
        host.addGuest(sview, ind - 1);
    }

    /**
     * Moves a view down/back in a child list.
     */
    public static void moveViewDown(Editor anEditor)
    {
        View sview = anEditor.getSelView();
        ViewHost host = sview.getHost();
        if (host == null) {
            ViewUtils.beep();
            return;
        }

        int ind = sview.indexInHost();
        if (ind + 1 >= host.getGuestCount()) {
            ViewUtils.beep();
            return;
        }
        host.removeGuest(sview);
        host.addGuest(sview, ind + 1);
    }

    /**
     * Moves a view out of a ViewHost.
     */
    public static void moveViewOut(Editor anEditor)
    {
        View sview = anEditor.getSelView();
        ViewHost host = sview.getHost();
        if (host == null) {
            ViewUtils.beep();
            return;
        }
        ViewHost hostHost = ((View) host).getHost();
        if (hostHost == null) {
            ViewUtils.beep();
            return;
        }

        int ind = sview.indexInParent();
        int ind2 = hostHost.indexOfGuest((View) host);
        host.removeGuest(sview);
        hostHost.addGuest(sview, ind2 + 1);
    }

    /**
     * Groups the selected view in another content view.
     */
    public static void groupView(Editor anEditor)
    {
        // Get new FormBuilder and configure
        FormBuilder form = new FormBuilder();
        form.setPadding(20, 5, 15, 5);
        form.setSpacing(10);
        form.addLabel("Select group view class:").setFont(new snap.gfx.Font("Arial", 24));

        // Define options
        Class opts[] = {TitleView.class, TabView.class, SplitView.class, ScrollView.class, BoxView.class,
                ColView.class, RowView.class, BorderView.class};

        // Add and configure radio buttons
        for (int i = 0; i < opts.length; i++) {
            Class opt = opts[i];
            form.addRadioButton("ViewClass", opt.getSimpleName(), i == 0);
        }

        // Run dialog panel (just return if cancelled)
        if (!form.showPanel(anEditor, "Group in View", DialogBox.infoImage)) return;

        // Get select class string, and class
        String cstr = form.getStringValue("ViewClass");
        int ind = 0;
        for (int i = 0; i < opts.length; i++) if (cstr.equals(opts[i].getSimpleName())) ind = i;
        Class cls = opts[ind];

        // Create group view
        ParentView groupView;
        try {
            groupView = (ParentView) cls.newInstance();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        ViewHpr.getHpr(groupView).configure(groupView);

        // Add group view to view parent
        View sview = anEditor.getSelView();
        ViewHost par = sview.getHost();
        if (par == null) {
            ViewUtils.beep();
            return;
        }
        par.addGuest(groupView, sview.indexInHost());

        ViewHost groupHost = groupView instanceof ViewHost ? (ViewHost) groupView : null;
        if (groupHost == null) return;
        groupHost.addGuest(sview);
        anEditor.setSelView(groupView);
    }

    /**
     * Ungroups the selected view.
     */
    public static void ungroupView(Editor anEditor)
    {
        // Get View
        View view = anEditor.getSelView();
        ViewHost par = view.getHost();
        if (par == null) {
            ViewUtils.beep();
            return;
        }
        ViewHost groupHost = view instanceof ViewHost ? (ViewHost) view : null;
        if (groupHost == null) {
            ViewUtils.beep();
            return;
        }

        // Add view guests to host view and remove old host
        for (View child : groupHost.getGuests())
            par.addGuest(child);
        par.removeGuest(view);

        // Select host
        anEditor.setSelView((View) par);
    }

    /**
     * Changes the selected ViewHost to another.
     */
    public static void changeHost(Editor anEditor)
    {
        // Get new FormBuilder and configure
        FormBuilder form = new FormBuilder();
        form.setPadding(20, 5, 15, 5);
        form.setSpacing(10);
        form.addLabel("Select new host view class:").setFont(new snap.gfx.Font("Arial", 24));

        // Define options
        Class opts[] = {TitleView.class, TabView.class, SplitView.class, ScrollView.class, BoxView.class,
                ColView.class, RowView.class, BorderView.class};

        // Add and configure radio buttons
        for (int i = 0; i < opts.length; i++) {
            Class opt = opts[i];
            form.addRadioButton("ViewClass", opt.getSimpleName(), i == 0);
        }

        // Run dialog panel (just return if cancelled)
        if (!form.showPanel(anEditor, "Group in View", DialogBox.infoImage)) return;

        // Get select class string, and class
        String cstr = form.getStringValue("ViewClass");
        int ind = 0;
        for (int i = 0; i < opts.length; i++) if (cstr.equals(opts[i].getSimpleName())) ind = i;
        Class cls = opts[ind];

        // Create new parent view
        ParentView parNew;
        try {
            parNew = (ParentView) cls.newInstance();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        ViewHpr.getHpr(parNew).configure(parNew);
        ViewHost hostNew = parNew instanceof ViewHost ? (ViewHost) parNew : null;
        if (hostNew == null) {
            ViewUtils.beep();
            return;
        }

        // Get old host and parent
        View sview = anEditor.getSelView();
        ViewHost hostOld = sview instanceof ViewHost ? (ViewHost) sview : null;
        if (hostOld == null) {
            ViewUtils.beep();
            return;
        }
        ViewHost par = sview.getHost();
        if (par == null) {
            ViewUtils.beep();
            return;
        }

        // Add new host
        par.addGuest(parNew, sview.indexInHost());
        for (View guest : hostOld.getGuests())
            hostNew.addGuest(guest);
        par.removeGuest(sview);
        anEditor.setSelView(parNew);
    }
}