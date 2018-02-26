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
    setViewEnabled("ChangeHostButton", getEditor().getSelView() instanceof HostView);
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
    HostView par = sview.getHost(); if(par==null) { beep(); return; }
    
    int ind = sview.indexInParent(); if(ind==0) { beep(); return; }
    par.removeGuest(sview);
    par.addGuest(sview, ind-1);
}

/**
 * Moves a view down/back in a child list.
 */
public void moveViewDown()
{
    View sview = getEditor().getSelView();
    HostView par = sview.getHost(); if(par==null) { beep(); return; }
    
    int ind = sview.indexInHost(); if(ind+1>=par.getGuestCount()) { beep(); return; }
    par.removeGuest(sview);
    par.addGuest(sview, ind+1);
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
    HostView group = null; try { group = (HostView)cls.newInstance(); }
    catch(Exception e) { throw new RuntimeException(e); }
    ViewHpr.getHpr(group).configure(group);
    
    // Add group view to view parent
    View sview = getEditor().getSelView();
    HostView par = sview.getHost(); if(par==null) { beep(); return; }
    par.addGuest(group, sview.indexInHost());
    group.addGuest(sview);
    getEditor().setSelView(group);
}

/**
 * Ungroups the selected view.
 */
public void ungroupView()
{
    // Get View
    View view = getEditor().getSelView();
    HostView group = view instanceof HostView? (HostView)view : null; if(group==null) { beep(); return; }
    HostView par = group.getHost(); if(par==null) { beep(); return; }
    
    for(View child : group.getGuests())
        par.addGuest(child);
    par.removeGuest(group);
    getEditor().setSelView(par);
}

/**
 * Changes the selected HostView to another HostView view.
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
    
    // Create new HostView view
    HostView hostNew = null; try { hostNew = (HostView)cls.newInstance(); }
    catch(Exception e) { throw new RuntimeException(e); }
    ViewHpr.getHpr(hostNew).configure(hostNew);
    
    // Get old host and parent
    View sview = getEditor().getSelView();
    HostView hostOld = sview instanceof HostView? (HostView)sview : null; if(hostOld==null) { beep(); return; }
    HostView par = sview.getHost(); if(par==null) { beep(); return; }
    
    // Add new host
    par.addGuest(hostNew, sview.indexInHost());
    for(View guest : hostOld.getGuests()) hostNew.addGuest(guest);
    par.removeGuest(hostOld);
    getEditor().setSelView(hostNew);
}

}