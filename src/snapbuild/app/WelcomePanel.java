package snapbuild.app;
import snap.util.*;
import snap.view.*;
import snap.web.RecentFiles;
import snap.web.*;

/**
 * An implementation of a panel to manage/open user Snap sites (projects).
 */
public class WelcomePanel extends ViewOwner {

    // The selected file
    private WebFile  _selFile;
    
    // Whether welcome panel should exit on hide
    private boolean  _exit;
    
    // The Runnable to be called when app quits
    private Runnable  _onQuit;

    // The RecentFiles
    private WebFile[]  _recentFiles;

    // The shared instance
    private static WelcomePanel  _shared;

    /**
     * Constructor.
     */
    private WelcomePanel()
    {
        super();
    }

    /**
     * Returns the shared instance.
     */
    public static WelcomePanel getShared()
    {
        if(_shared != null) return _shared;
        return _shared = new WelcomePanel();
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
        Prefs.getDefaultPrefs().flush();    // Flush preferences
        if(_exit) quitApp(); // If exit requested, quit app
    }

    /**
     * Returns the selected file.
     */
    public WebFile getSelFile()  { return _selFile; }

    /**
     * Sets the selected file.
     */
    public void setSelFile(WebFile aFile)  { _selFile = aFile; }

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
        // Add WelcomePaneAnim view
        DocView anim = getAnimView();
        getUI(ChildView.class).addChild(anim, 0);
        anim.playAnimDeep();

        // Configure SitesTable
        TableView<WebFile> sitesTable = getView("SitesTable", TableView.class);
        sitesTable.setRowHeight(24);
        sitesTable.getCol(0).setItemTextFunction(i -> i.getName());

        // Enable SitesTable MouseReleased
        WebFile[] recentFiles = getRecentFiles();
        if (recentFiles.length > 0)
            _selFile = recentFiles[0];
        enableEvents(sitesTable, MouseRelease);

        // Hide ProgressBar
        getView("ProgressBar").setVisible(false);

        // Configure Window: Add WindowListener to indicate app should exit when close button clicked
        WindowView win = getWindow(); win.setTitle("Welcome");
        win.setResizable(false);
        enableEvents(win, WinClose);
        getView("OpenButton", Button.class).setDefaultButton(true);
    }

    /**
     * Resets UI.
     */
    public void resetUI()
    {
        setViewEnabled("OpenButton", getSelFile() != null);
        setViewItems("SitesTable", getRecentFiles());
        setViewSelItem("SitesTable", getSelFile());
    }

    /**
     * Responds to UI changes.
     */
    public void respondUI(ViewEvent anEvent)
    {
        // Handle SamplesButton
        if (anEvent.equals("SamplesButton"))
            openSamples();

        // Handle SitesTable
        if (anEvent.equals("SitesTable"))
            setSelFile((WebFile)anEvent.getSelItem());

        // Handle NewButton
        if (anEvent.equals("NewButton")) {
            newFile();
        }

        // Handle OpenPanelButton
        if (anEvent.equals("OpenPanelButton"))
            showOpenPanel();

        // Handle OpenButton or SitesTable double-click
        if (anEvent.equals("OpenButton") || anEvent.equals("SitesTable") && anEvent.getClickCount()>1) {
            WebFile file = (WebFile)getViewSelItem("SitesTable");
            openFile(file);
        }

        // Handle QuitButton
        if (anEvent.equals("QuitButton")) {
            _exit = true; hide(); }

        // Handle WinClosing
        if (anEvent.isWinClose()) {
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
     * Opens the Samples.
     */
    protected void openSamples()
    {
        EditorPane dpane = new EditorPane().newDocument();
        dpane.setWindowVisible(true);
        hide();
        runLaterDelayed(300, () -> dpane.showSamples());
    }

    /**
     * Runs the open panel.
     */
    public void showOpenPanel()
    {
        // Have editor run open panel (if no document opened, just return)
        EditorPane epane = new EditorPane().showOpenPanel(getUI());
        if (epane == null)
            return;

        // Make editor window visible and hide welcome panel
        epane.setWindowVisible(true);
        hide();

        // Add URL to RecentFiles
        WebURL sourceURL = epane.getSourceURL();
        RecentFiles.addURL(sourceURL);
    }

    /**
     * Opens selected file.
     */
    public void openFile(Object aSource)
    {
        // Have editor run open panel (if no document opened, just return)
        EditorPane epane = new EditorPane().open(aSource); if(epane==null) return;

        // Make editor window visible and hide welcome panel
        epane.setWindowVisible(true);
        hide();

        // Add URL to RecentFiles
        WebURL sourceURL = epane.getSourceURL();
        RecentFiles.addURL(sourceURL);
    }

    /**
     * Returns the list of the recent documents as a list of strings.
     */
    public WebFile[] getRecentFiles()
    {
        // If already set, just return
        if (_recentFiles!=null) return _recentFiles;

        WebFile[] recentFiles = RecentFiles.getFiles();
        return _recentFiles = recentFiles;
    }

    /** Loads the WelcomePaneAnim.snp DocView. */
    DocView getAnimView()
    {
        // Unarchive WelcomePaneAnim.snp as DocView
        WebURL url = WebURL.getURL(getClass(), "WelcomePanelAnim.snp");
        DocView doc = (DocView)new ViewArchiver().getViewForSource(url);

        // Get page and clear border/shadow
        PageView page = doc.getPage();
        page.setBorder(null);
        page.setEffect(null);

        // Set BuildText and JavaText
        View bt = page.getChildForName("BuildText");
        View jt = page.getChildForName("JVMText");
        bt.setText("Build: " + SnapUtils.getBuildInfo());
        jt.setText("JVM: " + System.getProperty("java.runtime.version"));

        // Return doc
        return doc;
    }
}