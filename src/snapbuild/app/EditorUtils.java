package snapbuild.app;
import snap.util.ArrayUtils;
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
        // Get SelView and ParentView
        View selView = anEditor.getSelView();
        ParentView parentView = selView.getParent();
        if (parentView == null) {
            ViewUtils.beep();
            return;
        }

        // Get index in parent
        int indexInParent = selView.indexInParent();
        if (indexInParent == 0) {
            ViewUtils.beep();
            return;
        }

        // Remove/add child at new index
        ViewUtils.removeChild(parentView, selView);
        ViewUtils.addChild(parentView, selView, indexInParent - 1);
    }

    /**
     * Moves a view down/back in a child list.
     */
    public static void moveViewDown(Editor anEditor)
    {
        // Get SelView and ParentView
        View selView = anEditor.getSelView();
        ParentView parentView = selView.getParent();
        if (parentView == null) {
            ViewUtils.beep();
            return;
        }

        // Get index in parent
        int indexInParent = selView.indexInParent();
        if (indexInParent + 1 >= parentView.getChildCount()) {
            ViewUtils.beep();
            return;
        }

        // Remove/add child at new index
        ViewUtils.removeChild(parentView, selView);
        ViewUtils.addChild(parentView, selView, indexInParent + 1);
    }

    /**
     * Moves a view out of a ViewHost.
     */
    public static void moveViewOut(Editor anEditor)
    {
        // Get SelView and ParentView
        View selView = anEditor.getSelView();
        ParentView parentView = selView.getParent();
        if (parentView == null) {
            ViewUtils.beep();
            return;
        }

        // Get index in parent
        ParentView grandparentView = parentView.getParent();
        if (grandparentView == null) {
            ViewUtils.beep();
            return;
        }

        // Remove/add child to new parent
        int indexInGrandparent = parentView.indexInParent();
        ViewUtils.removeChild(parentView, selView);
        ViewUtils.addChild(grandparentView, selView, indexInGrandparent + 1);
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
        Class<?>[] viewClasses = new Class[] {
            ColView.class, RowView.class, BoxView.class, TitleView.class, CollapseView.class,
            TabView.class, SplitView.class, ScrollView.class, BorderView.class
        };

        // Add and configure radio buttons
        for (int i = 0; i < viewClasses.length; i++) {
            Class<?> viewClass = viewClasses[i];
            form.addRadioButton("ViewClass", viewClass.getSimpleName(), i == 0);
        }

        // Run dialog panel (just return if cancelled)
        if (!form.showPanel(anEditor, "Group in View", DialogBox.infoImage)) return;

        // Get select class string, and class
        String className = form.getStringValue("ViewClass");
        Class<?> viewClass = ArrayUtils.findMatch(viewClasses, cls -> className.equals(cls.getSimpleName()));

        // Create group view
        ParentView groupView;
        try {
            groupView = (ParentView) viewClass.newInstance();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        ViewHpr.getHpr(groupView).configure(groupView);

        // Add group view to view parent
        View selView = anEditor.getSelView();
        ViewHost parentView = selView.getHost();
        if (parentView == null) {
            ViewUtils.beep();
            return;
        }

        // Add GroupView at SelView index
        parentView.addGuest(groupView, selView.indexInHost());

        // Replace SelView with GroupView, add SelView to GroupView, select GroupView
        ViewHost grandparentView = groupView instanceof ViewHost ? (ViewHost) groupView : null;
        if (grandparentView != null)
            grandparentView.addGuest(selView);
        anEditor.setSelView(groupView);
    }

    /**
     * Ungroups the selected view.
     */
    public static void ungroupView(Editor anEditor)
    {
        // Get View
        View selView = anEditor.getSelView();
        ViewHost parentView = selView.getHost();
        if (parentView == null) {
            ViewUtils.beep();
            return;
        }

        // Get GroupHost
        ViewHost groupHost = selView instanceof ViewHost ? (ViewHost) selView : null;
        if (groupHost == null) {
            ViewUtils.beep();
            return;
        }

        // Add view guests to host view and remove old host
        for (View child : groupHost.getGuests())
            parentView.addGuest(child);
        parentView.removeGuest(selView);

        // Select host
        anEditor.setSelView((View) parentView);
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
        Class<?>[] viewClasses = {
            ColView.class, RowView.class, BoxView.class, TitleView.class, CollapseView.class,
            TabView.class, SplitView.class, ScrollView.class, BorderView.class
        };

        // Add and configure radio buttons
        for (int i = 0; i < viewClasses.length; i++) {
            Class<?> opt = viewClasses[i];
            form.addRadioButton("ViewClass", opt.getSimpleName(), i == 0);
        }

        // Run dialog panel (just return if cancelled)
        if (!form.showPanel(anEditor, "Group in View", DialogBox.infoImage))
            return;

        // Get select class string, and class
        String className = form.getStringValue("ViewClass");
        Class<?> viewClass = ArrayUtils.findMatch(viewClasses, cls -> className.equals(cls.getSimpleName()));

        // Create new parent view
        ParentView groupView;
        try {
            groupView = (ParentView) viewClass.newInstance();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        ViewHpr.getHpr(groupView).configure(groupView);
        ViewHost hostNew = groupView instanceof ViewHost ? (ViewHost) groupView : null;
        if (hostNew == null) {
            ViewUtils.beep();
            return;
        }

        // Get old host and parent
        View selView = anEditor.getSelView();
        ViewHost hostOld = selView instanceof ViewHost ? (ViewHost) selView : null;
        if (hostOld == null) {
            ViewUtils.beep();
            return;
        }
        ViewHost parentView = selView.getHost();
        if (parentView == null) {
            ViewUtils.beep();
            return;
        }

        // Add new host
        parentView.addGuest(groupView, selView.indexInHost());
        for (View guest : hostOld.getGuests())
            hostNew.addGuest(guest);
        parentView.removeGuest(selView);
        anEditor.setSelView(groupView);
    }

    /**
     * Creates a new default editor pane.
     */
    public static ParentView createNewDocView()
    {
        // Create ColView as root view
        ColView colView = new ColView();
        colView.setPrefSize(400, 400);
        colView.setPadding(20, 20, 20, 20);
        colView.setSpacing(4);

        // Create default RowView as default child
        RowView rowView = new RowView();
        rowView.setName("FirstFocus");
        rowView.setPadding(4, 4, 4, 4);
        rowView.setSpacing(4);
        rowView.setGrowWidth(true);
        colView.addChild(rowView);

        // Return
        return colView;
    }
}