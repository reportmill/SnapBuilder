package snapbuild.app;
import java.util.*;
import snap.util.*;
import snap.view.*;
import snap.web.*;

/**
 * An implementation of a panel to manage/open user Snap sites (projects).
 */
public class WelcomePanel extends ViewOwner {

    // The list of files
    List <WebFile>          _files;

    // The selected file
    WebFile                 _selFile;
    
    // Whether welcome panel should exit on hide
    boolean                 _exit;
    
    // The Runnable to be called when app quits
    Runnable                _onQuit;

    // The shared instance
    static WelcomePanel     _shared;

/**
 * Returns the shared instance.
 */
public static WelcomePanel getShared()
{
    if(_shared!=null) return _shared;
    return _shared!=null? _shared : (_shared = new WelcomePanel());
}

/**
 * Shows the welcome panel.
 */
public void showPanel()
{
    getUI(); // This is bogus - if this isn't called, Window node get reset
    getWindow().setVisible(true); //getTimeline().play();
    resetLater();
}

/**
 * Hides the welcome panel.
 */
public void hide()
{
    // Hide window and stop animation
    getWindow().setVisible(false); //getTimeline().stop();
    
    // Write current list of sites, flush prefs and mayb exit
    //writeSites();         // Write data file for open/selected sites
    Prefs.get().flush();    // Flush preferences
    if(_exit) quitApp(); // If exit requested, quit app
}

/**
 * Returns the number of files.
 */
public int getFileCount()  { return getFiles().size(); }

/**
 * Returns the file at given index.
 */
public WebFile getFile(int anIndex)  { return getFiles().get(anIndex); }

/**
 * Returns the list of files.
 */
public List <WebFile> getFiles()  { return _files!=null? _files : (_files=new ArrayList()); }

/**
 * Returns the selected file.
 */
public WebFile getSelectedFile()  { return _selFile; }

/**
 * Sets the selected file.
 */
public void setSelectedFile(WebFile aFile)  { _selFile = aFile; }

/**
 * Returns the Runnable to be called to quit app.
 */
public Runnable getOnQuit()  { return _onQuit; }

/**
 * Sets the Runnable to be called to quit app.
 */
public void setOnQuit(Runnable aRunnable)  { _onQuit = aRunnable; }

/**
 * Called to quit app.
 */
public void quitApp()  { _onQuit.run(); }

/**
 * Initialize UI panel.
 */
protected void initUI()
{
    // Add WelcomePaneAnim node
    WelcomePanelAnim anim = new WelcomePanelAnim();
    getUI(ChildView.class).addChild(anim.getUI(), 0); anim.getUI().playAnimDeep();
    
    // Enable SitesTable MouseReleased
    TableView sitesTable = getView("SitesTable", TableView.class);
    sitesTable.setRowHeight(24);
    if(getRecentFiles().size()>0) _selFile = getRecentFiles().get(0);
    enableEvents(sitesTable, MouseRelease);
    
    // Set preferred size
    getUI().setPrefSize(400,480);
    
    // Configure Window: Add WindowListener to indicate app should exit when close button clicked
    WindowView win = getWindow(); win.setTitle("Welcome"); win.setResizable(false);
    enableEvents(win, WinClose);
    getView("OpenButton", Button.class).setDefaultButton(true);
}

/**
 * Resets UI.
 */
public void resetUI()
{
    //setViewEnabled("OpenButton", getSelectedFile()!=null);
    setViewItems("SitesTable", getRecentFiles());
    setViewSelectedItem("SitesTable", getSelectedFile());
}

/**
 * Responds to UI changes.
 */
public void respondUI(ViewEvent anEvent)
{
    // Handle NewButton
    if(anEvent.equals("NewButton")) {
        newFile();
    }
    
    // Handle OpenPanelButton
    if(anEvent.equals("OpenPanelButton"))
        showOpenPanel();
    
    // Handle OpenButton or SitesTable double-click
    if(anEvent.equals("OpenButton") || anEvent.equals("SitesTable") && anEvent.getClickCount()>1) {
        WebFile file = (WebFile)getViewSelectedItem("SitesTable");
        openFile(file);
    }
    
    // Handle QuitButton
    if(anEvent.equals("QuitButton")) {
        _exit = true; hide(); }
        
    // Handle WinClosing
    if(anEvent.isWinClose()) {
        _exit = true; hide(); }
}

/**
 * Creates a new file.
 */
protected void newFile()
{
    EditorPane epane = new EditorPane().newDocument();
    epane.setWindowVisible(true);
    hide();
}

/**
 * Runs the open panel.
 */
public void showOpenPanel()
{
    // Have editor run open panel (if no document opened, just return)
    EditorPane epane = new EditorPane().showOpenPanel(getUI()); if(epane==null) return;
    
    // Make editor window visible, show doc inspector, and order front after delay to get focus back from inspector
    epane.setWindowVisible(true);
    hide();
    addRecentFile(epane.getSourceURL().getPath());
}

/**
 * Opens selected file.
 */
public void openFile(Object aSource)
{
    // Have editor run open panel (if no document opened, just return)
    EditorPane epane = new EditorPane().open(aSource); if(epane==null) return;
    
    // Make editor window visible, show doc inspector, and order front after delay to get focus back from inspector
    epane.setWindowVisible(true);
    hide();
    addRecentFile(epane.getSourceURL().getPath());
}

/**
 * Returns the list of the recent documents as a list of strings.
 */
public static List <WebFile> getRecentFiles()
{
    // Get prefs for RecentDocuments (just return if missing)
    Prefs prefs = Prefs.get().getChild("RecentDocuments");
    
    // Add to the list only if the file is around and readable
    List list = new ArrayList();
    for(int i=0; ; i++) {
        String fname = prefs.get("index"+i, null); if(fname==null) break;
        WebURL url = WebURL.getURL(fname);
        WebFile file = url.getFile();
        if(file!=null)
            list.add(file);
    }
    
    // Return list
    return list;
}

/**
 * Adds a new file to the list and updates the users preferences.
 */
public static void addRecentFile(String aPath)
{
    // Get the doc list from the preferences
    WebURL url = WebURL.getURL(aPath);
    WebFile file = url.getFile(); if(file==null) return;
    List <WebFile> docs = getRecentFiles();
    
    // Remove the path (if it was there) and add to front of list
    docs.remove(file); docs.add(0, file);
    
    // Add at most 10 files to the preferences list
    Prefs prefs = Prefs.get().getChild("RecentDocuments");
    for(int i=0; i<docs.size() && i<10; i++) 
        prefs.set("index"+i, docs.get(i).getPath());
    try { prefs.flush(); } catch(Exception e)  { System.err.println(e); }
}

/**
 * Clears recent documents from preferences.
 */
public void clearRecentFiles()  { Prefs.get().getChild("RecentDocuments").clear(); }

/**
 * A viewer owner to load/view WelcomePanel animation from WelcomePanelAnim.snp.
 */
private static class WelcomePanelAnim extends ViewOwner {

    /** Initialize some fields. */
    protected void initUI()
    {
        setViewText("BuildText", "Build: " + SnapUtils.getBuildInfo());
        setViewText("JVMText", "JVM: " + System.getProperty("java.runtime.version"));
        DocView doc = getUI(DocView.class);
        PageView page = doc.getPage();
        page.setEffect(null); page.setBorder(null);
    }
}

}