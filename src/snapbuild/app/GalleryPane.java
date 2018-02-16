package snapbuild.app;
import snap.view.*;

/**
 * A custom class.
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
 * Respond to UI.
 */
protected void respondUI(ViewEvent anEvent)
{
    // Handle MoveUpButton, MoveDownButton, GroupInButton, UngroupButton
    if(anEvent.equals("MoveUpButton")) moveViewUp();
    if(anEvent.equals("MoveDownButton")) moveViewDown();
    if(anEvent.equals("GroupInButton")) groupView();
    if(anEvent.equals("UngroupButton")) ungroupView();
}

/**
 * Moves a view up/forward in a child list.
 */
public void moveViewUp()
{
    View sview = getEditor().getSelView();
    ParentView par = sview.getParent();
    
    if(par instanceof ChildView) { ChildView cview = (ChildView)par;
        int ind = sview.indexInParent(); if(ind==0) { beep(); return; }
        cview.removeChild(sview);
        cview.addChild(sview, ind-1);
    }
}

/**
 * Moves a view down/back in a child list.
 */
public void moveViewDown()
{
    View sview = getEditor().getSelView();
    ParentView par = sview.getParent();
    
    if(par instanceof ChildView) { ChildView cview = (ChildView)par;
        int ind = sview.indexInParent(); if(ind+1>=cview.getChildCount()) { beep(); return; }
        cview.removeChild(sview);
        cview.addChild(sview, ind+1);
    }
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
    ParentView group = null; try { group = (ParentView)cls.newInstance(); }
    catch(Exception e) { throw new RuntimeException(e); }
    ViewHpr.getHpr(group).configure(group);
    
    // Add group view to view parent
    View sview = getEditor().getSelView();
    ParentView par = sview.getParent();
    ViewHpr.getHpr(par).addChild(par, group, sview.indexInParent());
    ViewHpr.getHpr(group).addView(group, sview);
    getEditor().setSelView(group);
}

/**
 * Ungroups the selected view.
 */
public void ungroupView()
{
    // Get View
    View view = getEditor().getSelView();
    ParentView par = view.getParent();
    
    if(view instanceof ParentView) { ParentView group = (ParentView)view;
        for(View child : group.getChildren())
            ViewHpr.getHpr(par).addView(par, child);
        if(par instanceof ChildView)
            ((ChildView)par).removeChild(group);
        getEditor().setSelView(par);
    }
}

}