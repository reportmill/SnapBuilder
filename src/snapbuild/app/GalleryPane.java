package snapbuild.app;
import snap.view.*;
import snap.viewx.*;

/**
 * Manages the UI for Gallery of views and arrange controls (move up/down, group/ungroup).
 */
public class GalleryPane extends ViewOwner {

    // The editor pane
    EditorPane        _epane;
    
    // The GalleryView
    GalleryView       _galleryView;
    
/**
 * Creates a new GalleryPane.
 */
public GalleryPane(EditorPane anEP)
{
    _epane = anEP;
}

/**
 * Returns the editor.
 */
public Editor getEditor()  { return _epane.getEditor(); }

/**
 * Initialize UI.
 */
protected void initUI()
{
    _galleryView = getView("GalleryView", GalleryView.class);
    _galleryView._galleryPane = this;
}

/**
 * Reset UI.
 */
protected void resetUI()
{
    setViewEnabled("ChangeHostButton", getEditor().getSelView() instanceof ViewHost);
}

/**
 * Respond to UI.
 */
protected void respondUI(ViewEvent anEvent)
{
    // Handle MoveUpButton, MoveDownButton, GroupInButton, UngroupButton, ChangeHostButton
    if(anEvent.equals("MoveUpButton")) moveViewUp();
    if(anEvent.equals("MoveDownButton")) moveViewDown();
    if(anEvent.equals("GroupInButton")) groupView();
    if(anEvent.equals("UngroupButton")) ungroupView();
    if(anEvent.equals("ChangeHostButton")) changeHost();
}

/**
 * Moves a view up/forward in a child list.
 */
public void moveViewUp()
{
    View sview = getEditor().getSelView();
    ViewHost host = sview.getHost(); if(host==null) { beep(); return; }
    
    int ind = sview.indexInParent(); if(ind==0) { beep(); return; }
    host.removeGuest(sview);
    host.addGuest(sview, ind-1);
}

/**
 * Moves a view down/back in a child list.
 */
public void moveViewDown()
{
    View sview = getEditor().getSelView();
    ViewHost host = sview.getHost(); if(host==null) { beep(); return; }
    
    int ind = sview.indexInHost(); if(ind+1>=host.getGuestCount()) { beep(); return; }
    host.removeGuest(sview);
    host.addGuest(sview, ind+1);
}

/**
 * Groups the selected view in another content view.
 */
public void groupView()
{
    // Get new FormBuilder and configure
    FormBuilder form = new FormBuilder(); form.setPadding(20, 5, 15, 5); form.setSpacing(10);
    form.addLabel("Select group view class:").setFont(new snap.gfx.Font("Arial", 24));
    
    // Define options
    Class opts[] = { TitleView.class, TabView.class, SplitView.class, ScrollView.class, BoxView.class,
        ColView.class, RowView.class, BorderView.class };
    
    // Add and configure radio buttons
    for(int i=0; i<opts.length; i++) { Class opt = opts[i];
        form.addRadioButton("ViewClass", opt.getSimpleName(), i==0); }

    // Run dialog panel (just return if cancelled)
    if(!form.showPanel(_epane.getUI(), "Group in View", DialogBox.infoImage)) return;
    
    // Get select class string, and class
    String cstr = form.getStringValue("ViewClass");
    int ind = 0; for(int i=0; i<opts.length; i++) if(cstr.equals(opts[i].getSimpleName())) ind = i;
    Class cls = opts[ind];
    
    // Create group view
    ParentView groupView = null; try { groupView = (ParentView)cls.newInstance(); }
    catch(Exception e) { throw new RuntimeException(e); }
    ViewHpr.getHpr(groupView).configure(groupView);
    
    // Add group view to view parent
    View sview = getEditor().getSelView();
    ViewHost par = sview.getHost(); if(par==null) { beep(); return; }
    par.addGuest(groupView, sview.indexInHost());
    
    ViewHost groupHost = groupView instanceof ViewHost? (ViewHost)groupView : null; if(groupHost==null) return;
    groupHost.addGuest(sview);
    getEditor().setSelView(groupView);
}

/**
 * Ungroups the selected view.
 */
public void ungroupView()
{
    // Get View
    View view = getEditor().getSelView();
    ViewHost par = view.getHost(); if(par==null) { beep(); return; }
    ViewHost groupHost = view instanceof ViewHost? (ViewHost)view : null; if(groupHost==null) { beep(); return; }
    
    // Add view guests to host view and remove old host
    for(View child : groupHost.getGuests())
        par.addGuest(child);
    par.removeGuest(view);
    
    // Select host
    getEditor().setSelView((View)par);
}

/**
 * Changes the selected ViewHost to another.
 */
public void changeHost()
{
    // Get new FormBuilder and configure
    FormBuilder form = new FormBuilder(); form.setPadding(20, 5, 15, 5); form.setSpacing(10);
    form.addLabel("Select new host view class:").setFont(new snap.gfx.Font("Arial", 24));
    
    // Define options
    Class opts[] = { TitleView.class, TabView.class, SplitView.class, ScrollView.class, BoxView.class,
        ColView.class, RowView.class, BorderView.class };
    
    // Add and configure radio buttons
    for(int i=0; i<opts.length; i++) { Class opt = opts[i];
        form.addRadioButton("ViewClass", opt.getSimpleName(), i==0); }

    // Run dialog panel (just return if cancelled)
    if(!form.showPanel(_epane.getUI(), "Group in View", DialogBox.infoImage)) return;
    
    // Get select class string, and class
    String cstr = form.getStringValue("ViewClass");
    int ind = 0; for(int i=0; i<opts.length; i++) if(cstr.equals(opts[i].getSimpleName())) ind = i;
    Class cls = opts[ind];
    
    // Create new parent view
    ParentView parNew = null; try { parNew = (ParentView)cls.newInstance(); }
    catch(Exception e) { throw new RuntimeException(e); }
    ViewHpr.getHpr(parNew).configure(parNew);
    ViewHost hostNew = parNew instanceof ViewHost? (ViewHost)parNew : null; if(hostNew==null) { beep(); return; }
    
    // Get old host and parent
    View sview = getEditor().getSelView();
    ViewHost hostOld = sview instanceof ViewHost? (ViewHost)sview : null; if(hostOld==null) { beep(); return; }
    ViewHost par = sview.getHost(); if(par==null) { beep(); return; }
    
    // Add new host
    par.addGuest(parNew, sview.indexInHost());
    for(View guest : hostOld.getGuests())
        hostNew.addGuest(guest);
    par.removeGuest(sview);
    getEditor().setSelView(parNew);
}

}